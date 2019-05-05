package io.github.norbipeti.audiospectrum;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import static jouvieje.bass.Bass.*;
import static jouvieje.bass.defines.BASS_MUSIC.BASS_MUSIC_RAMP;
import static jouvieje.bass.defines.BASS_DATA.BASS_DATA_FFT2048;
import static jouvieje.bass.utils.BufferUtils.newByteBuffer;
import static jouvieje.bass.utils.BufferUtils.SIZEOF_FLOAT;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Timer;
import java.util.TimerTask;

import jouvieje.bass.BassInit;
import jouvieje.bass.exceptions.BassException;
import jouvieje.bass.structures.HMUSIC;
import jouvieje.bass.structures.HSTREAM;

public class Analyzer //Based on NativeBass example 'Spectrum'
{
	/* display error messages */
	private final void error(String text, CommandSender sender)
	{
		System.out.println(text + " - " + BASS_ErrorGetCode());
	}

	private final void printfExit(String format, Object... args)
	{
		String s = String.format(format, args);
		System.out.println(s);
		stop();
		Bukkit.getPluginManager().disablePlugin(PluginMain.getPlugin(PluginMain.class));
	}

	private boolean init = false;
	private boolean deinit = false;

	private int chan;

	public FloatBuffer init()
	{
		/*
		 * NativeBass Init
		 */
		try
		{
			BassInit.DEBUG=true;
			BassInit.loadLibraries();
		} catch (BassException e)
		{
			printfExit("NativeBass error! %s\n", e.getMessage());
			return null;
		}

		/*
		 * Checking NativeBass version
		 */
		if (BassInit.NATIVEBASS_LIBRARY_VERSION() != BassInit.NATIVEBASS_JAR_VERSION())
		{
			printfExit("Error!  NativeBass library version (%08x) is different to jar version (%08x)\n",
					BassInit.NATIVEBASS_LIBRARY_VERSION(), BassInit.NATIVEBASS_JAR_VERSION());
			return null;
		}

		final int size = 1024 * SIZEOF_FLOAT;
		if (buffer == null || buffer.capacity() < size)
			buffer = newByteBuffer(size);
		FloatBuffer floats = buffer.asFloatBuffer();

		/* ================================================== */

		init = true;
		return floats;
	}

	private volatile ByteBuffer buffer;
	private TimerTask tt;

	public boolean run(CommandSender sender)
	{
		if (!init)
		{
			return false;
		}

		// check the correct BASS was loaded
		if (((BASS_GetVersion() & 0xFFFF0000) >> 16) != BassInit.BASSVERSION())
		{
			printfExit("An incorrect version of BASS.DLL was loaded");
			return false;
		}

		// initialize BASS
		if (!BASS_Init(-1, 44100, 0, null, null))
		{
			error("Can't initialize device", sender);
			stop();
			return false;
		}
		return true;
	}

	public boolean start(CommandSender sender, String file)
	{
		if (playing)
			stopPlaying();
		if (!playFile(sender, file))
			return false;
		// setup update timer (50hz)
		timer.scheduleAtFixedRate(tt = new TimerTask()
		{
			@Override
			public void run()
			{
				BASS_ChannelGetData(chan, buffer, BASS_DATA_FFT2048); //Get the FFT data
			}
		}, 25, 25);
		return true;
	}

	public boolean isRunning()
	{
		return deinit;
	}

	public void stop()
	{
		if (!init || deinit)
		{
			return;
		}
		deinit = true;
		if (playing)
			stopPlaying();
		BASS_Free();
	}

	private boolean playing = false;

	private boolean playFile(CommandSender sender, String file)
	{
		if (!new File(file).exists())
		{
			sender.sendMessage("§cFile not found: " + file);
			return false;
		}
		HSTREAM stream = null;
		HMUSIC music = null;
		if ((stream = BASS_StreamCreateFile(false, file, 0, 0, 0)) == null //No loop
				&& (music = BASS_MusicLoad(false, file, 0, 0, BASS_MUSIC_RAMP, 0)) == null) //No loop
		{
			error("Can't play file", sender);
			return false; // Can't load the file
		}

		chan = (stream != null) ? stream.asInt() : ((music != null) ? music.asInt() : 0);

		BASS_ChannelPlay(chan, false);
		return playing = true;
	}

	private Timer timer = new Timer();

	public boolean stopPlaying()
	{
		return !(playing = !(BASS_ChannelStop(chan) && tt.cancel()));
	}

	public boolean playing()
	{
		return playing;
	}
}
