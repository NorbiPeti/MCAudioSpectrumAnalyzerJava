package tk.sznp.audiospectrum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {
	private Thread thread;
	private boolean running = false;

	// Fired when plugin is first enabled
	@Override
	public void onEnable() {
		thread = new Thread() {
			// Runnable runnable=new Runnable() {
			public void run() {
				PluginMain.this.run(5896);
			}
		};
		running = true;
		thread.start();
		// runnable.run();
	}

	// Fired when plugin is disabled
	@Override
	public void onDisable() {
		running = false;
	}

	private volatile Player player;
	private volatile int moveup;
	private volatile byte[] packet;
	private volatile boolean runningtask;

	public void run(int port) {
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(port);
			byte[] receiveData = new byte[8];

			System.out.printf("Listening on udp:%s:%d%n", InetAddress
					.getLocalHost().getHostAddress(), port);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			while (running) {
				serverSocket.receive(receivePacket);
				packet = receivePacket.getData();
				player = null;
				try {
					player = Bukkit.getPlayer("NorbiPeti");
				} catch (Exception e) {
				}
				/*
				 * getServer().dispatchCommand( getServer().getConsoleSender(),
				 * String.format("scoreboard players set t%d tracks %d",
				 * packet[0], Byte.toUnsignedInt(packet[1]))); //TODO: Teljesen
				 * szerveroldali legyen
				 */
				if (player == null)
					continue;

				moveup = 0;
				// System.out.println("Packet[1]: " + packet[1]);
				Runnable runnable = new Runnable() {
					public void run() {
						while (true) {
							if (player == null) {
								System.out.println("Player is null");
								return;
							}
							Block block = player.getWorld().getBlockAt(
									129 - packet[0], 56 + moveup, -654);
							if (Byte.toUnsignedInt(packet[1]) - moveup * 16 > 16) {
								block.setType(Material.SNOW_BLOCK);
								block.getState().setData(
										new MaterialData(Material.SNOW_BLOCK,
												(byte) 0));
							} else {
								block.setType(Material.SNOW);
								MaterialData data = new MaterialData(
										Material.SNOW);
								data.setData((byte) (Byte
										.toUnsignedInt(packet[1]) - moveup));
								block.getState().setData(data);
							}
							if (Byte.toUnsignedInt(packet[1]) - moveup * 16 > 16)
								moveup += 1;
							else
								break;
						}
						for(int i=56+moveup+1; i<255; i++)
						{
							Block block = player.getWorld().getBlockAt(
									129 - packet[0], i, -654);
							block.setType(Material.AIR);
						}
						runningtask = false;
					}
				};
				try {
					runningtask = true;
					getServer().getScheduler().runTask(this, runnable);
					while (runningtask)
						;
				} catch (Exception e) {
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			serverSocket.close();
		}
	}
}
