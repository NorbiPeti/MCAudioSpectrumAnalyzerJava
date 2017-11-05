package io.github.norbipeti.audiospectrum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.bukkit.Bukkit;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin
{
	private Thread thread;
	private boolean running = false;
	private volatile int[] bars = new int[16];

	// Fired when plugin is first enabled
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		BarsRenderer br = new BarsRenderer(bars);
		for (short i = 0; i < 4; i++)
		{
			MapView map = Bukkit.getMap(i);
			if (map == null)
				map = Bukkit.createMap(Bukkit.getWorlds().get(0));
			map.getRenderers().clear();
			map.addRenderer(br);
		}
		thread = new Thread()
		{
			public void run()
			{
				PluginMain.this.run(5896);
			}
		};
		running = true;
		thread.start();
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable()
	{
		running = false;
	}

	private volatile byte[] packet = new byte[2];

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
				bars[Byte.toUnsignedInt(packet[0])] = Byte.toUnsignedInt(packet[1]);
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			serverSocket.close();
		}
	}
}
