package io.github.norbipeti.audiospectrum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin
{
	//private Thread thread;
	private boolean running = false;
	private volatile int[] bars = new int[16];
	private BarsRenderer br;

	// Fired when plugin is first enabled
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		br = new BarsRenderer(bars);
		for (short i = 0; i < 4; i++)
		{
			MapView map = Bukkit.getMap(i);
			if (map == null)
				map = Bukkit.createMap(Bukkit.getWorlds().get(0));
			map.getRenderers().clear();
			map.addRenderer(br);
		}
		//thread = new Thread(() -> PluginMain.this.run(5896));
		//running = true;
		//thread.start();
		Analyzer an = new Analyzer();
		Bukkit.getConsoleSender().sendMessage("§bInitializing analyzer...");
		an.init(); //TODO: Add command to play music, test
		Bukkit.getConsoleSender().sendMessage("§bDone!");
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable()
	{
		running = false;
	}

	private volatile byte[] packet = new byte[16];

	public void run(int port)
	{
		DatagramSocket serverSocket = null;
		try
		{
			serverSocket = new DatagramSocket(port);

			System.out.printf("Listening on udp:%s:%d%n", InetAddress.getLocalHost().getHostAddress(), port);
			DatagramPacket receivePacket = new DatagramPacket(packet, packet.length);

			while (running)
			{
				serverSocket.receive(receivePacket);
				for (int i = 0; i < packet.length && i < bars.length; i++)
					bars[i] = Byte.toUnsignedInt(packet[i]);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			serverSocket.close();
		}
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
		} else
		{
			sender.sendMessage("Command not implemented!");
			return true;
		}
	}
}
