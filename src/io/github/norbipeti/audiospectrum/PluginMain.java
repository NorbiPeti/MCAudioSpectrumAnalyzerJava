package io.github.norbipeti.audiospectrum;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin
{
	@SuppressWarnings("unused") //Assignment in method call isn't counted as use
	private volatile FloatBuffer bars;
	private BarsRenderer br;
	private Analyzer an;

	// Fired when plugin is first enabled
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		try
		{
			//System.setProperty("org.jouvieje.libloader.debug", "true");
			Bukkit.getConsoleSender().sendMessage("§bInitializing analyzer...");
			an = new Analyzer();
			URL dirURL = getClassLoader().getResource("res");
			String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
			JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements())
			{
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory() || !entry.getName().startsWith("res/"))
					continue;
				File file = new File(getDataFolder(), entry.getName().substring(entry.getName().indexOf('/') + 1));
				file.getParentFile().mkdirs();
				if (!file.exists())
				{
					InputStream link = jar.getInputStream(entry);
					Files.copy(link, file.getAbsoluteFile().toPath());
				}
			}
			jar.close();
			for (File f : getDataFolder().listFiles())
				addLibraryPath(f.getAbsolutePath());
			br = new BarsRenderer(bars = an.init());
			for (short i = 0; i < 4; i++)
			{
				MapView map = Bukkit.getMap(i);
				if (map == null)
					map = Bukkit.createMap(Bukkit.getWorlds().get(0));
				map.getRenderers().clear();
				map.addRenderer(br);
			}
			Bukkit.getConsoleSender().sendMessage("§bDone!");
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable()
	{
		an.stop();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equalsIgnoreCase("barcount"))
		{
			if (args.length < 1)
				return false;
			try
			{
				byte c = Byte.parseByte(args[0]);
				sender.sendMessage("Bar count set to " + br.setBarCount(c));
				return true;

			} catch (Exception e)
			{
				return false;
			}
		} else if (command.getName().equalsIgnoreCase("singlemap"))
		{
			sender.sendMessage("Single map toggled, now " + br.toggleSingle());
			return true;
		} else if (command.getName().equalsIgnoreCase("play"))
		{
			if (an.run(sender, Arrays.stream(args).collect(Collectors.joining(" "))))
				sender.sendMessage("Started playing music");
			else
				sender.sendMessage("§cFailed to play music.");
			return true;
		} else if (command.getName().equalsIgnoreCase("stopplay"))
		{
			if (an.stopPlaying())
				sender.sendMessage("Stopped playing music");
			else
				sender.sendMessage("§cCan't stop the party!");
			return true;
		} else
		{
			sender.sendMessage("Command not implemented!");
			return true;
		}
	}

	/**
	 * Adds the specified path to the java library path
	 *
	 * @param pathToAdd
	 *            the path to add
	 * @throws Exception
	 */
	public static void addLibraryPath(String pathToAdd) throws Exception
	{
		final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		//get array of paths
		final String[] paths = (String[]) usrPathsField.get(null);

		//check if the path to add is already present
		for (String path : paths)
		{
			if (path.equals(pathToAdd))
			{
				return;
			}
		}

		//add the new path
		final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = pathToAdd;
		usrPathsField.set(null, newPaths);
		System.setProperty("java.library.path", Arrays.stream(newPaths).collect(Collectors.joining(";")));
	}
}
