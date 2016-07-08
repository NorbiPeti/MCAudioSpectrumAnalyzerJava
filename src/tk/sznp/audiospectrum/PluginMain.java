package tk.sznp.audiospectrum;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.ItemMeta.Spigot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;

public class PluginMain extends JavaPlugin
{
	private Thread thread;
	private boolean running = false;

	// Fired when plugin is first enabled
	@Override
	public void onEnable()
	{
		System.out.println("Adding ArmorStands...");
		Collection<ArmorStand> as = Bukkit.getWorlds().get(0)
				.getEntitiesByClass(ArmorStand.class);
		for (ArmorStand a : as)
		{
			Score score = Bukkit.getScoreboardManager().getMainScoreboard()
					.getObjective("BarIDX")
					.getScore(a.getUniqueId().toString());
			if (!score.isScoreSet())
				continue;
			int x = score.getScore();
			score = Bukkit.getScoreboardManager().getMainScoreboard()
					.getObjective("BarIDY")
					.getScore(a.getUniqueId().toString());
			if (!score.isScoreSet())
				continue;
			int y = score.getScore();
			ArmorStands.put(x + "-" + y, a);
		}
		for (int i = 0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				if (!ArmorStands.containsKey(i + "-" + j))
				{
					ArmorStand a = (ArmorStand) Bukkit.getWorlds().get(0)
							.spawnEntity(new Location(Bukkit.getWorlds().get(0),
									129, 4, -600), EntityType.ARMOR_STAND);
					ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE);
					LeatherArmorMeta lam = (LeatherArmorMeta) is.getItemMeta();
					lam.setColor(Color.RED);
					is.setItemMeta(lam);
					a.setChestplate(is);
					ArmorStands.put(i + "-" + j, a);
					Score score = Bukkit.getScoreboardManager()
							.getMainScoreboard().getObjective("BarIDX")
							.getScore(a.getUniqueId().toString());
					score.setScore(i);
					score = Bukkit.getScoreboardManager().getMainScoreboard()
							.getObjective("BarIDY")
							.getScore(a.getUniqueId().toString());
					score.setScore(j);
					System.out.println("Created armor stand " + i + "-" + j);
				}
			}
		}
		System.out.println("Done!");
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

	private volatile int moveup;
	private volatile byte[] packet;
	private volatile boolean runningtask;
	private volatile HashMap<String, ArmorStand> ArmorStands = new HashMap<String, ArmorStand>();

	public void run(int port)
	{
		DatagramSocket serverSocket = null;
		try
		{
			serverSocket = new DatagramSocket(port);
			byte[] receiveData = new byte[8];

			System.out.printf("Listening on udp:%s:%d%n",
					InetAddress.getLocalHost().getHostAddress(), port);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			while (running)
			{
				serverSocket.receive(receivePacket);
				packet = receivePacket.getData();

				moveup = 0;
				Runnable runnable = new Runnable()
				{
					public void run()
					{
						while (true)
						{
							/*
							 * Block block =
							 * Bukkit.getWorlds().get(0).getBlockAt(
							 * 129 - packet[0], 56 + moveup, -600);
							 */
							ArmorStand as = ArmorStands.get(packet[0] + "-"
									+ (Byte.toUnsignedInt(packet[1]) % 8));
							/*
							 * System.out.println("packet[0]: " + packet[0]);
							 * //TODO: TMP
							 * System.out.println("packet[1]: " + packet[1]);
							 * //TODO: TMP
							 * System.out.println("get: " + packet[0] + "-"
							 * + (Byte.toUnsignedInt(packet[1]) % 8)); //TODO:
							 * TMP
							 * System.out.println("as: " + as); //TODO: TMP
							 */
							if (as == null)
								break;
							if (Byte.toUnsignedInt(packet[1]) - moveup * 8 > 8)
							{
								/*
								 * block.setType(Material.SNOW_BLOCK);
								 * block.getState().setData(new MaterialData(
								 * Material.SNOW_BLOCK, (byte) 0));
								 */
								System.out.println("A: " + new Location(
										as.getWorld(), 129 - packet[0],
										4 + moveup, -600));
								as.teleport(new Location(as.getWorld(),
										129 - packet[0], 4 + moveup, -600));
								//TODO: Change armor color
							} else
							{
								/*
								 * block.setType(Material.SNOW);
								 * block.setData(
								 * (byte) (Byte.toUnsignedInt(packet[1])
								 * - moveup * 8));
								 */
								System.out.println("B: " + new Location(
										as.getWorld(), 129 - packet[0],
										4 + moveup
												+ (Byte.toUnsignedInt(packet[1])
														- moveup * 8) * 0.1,
										-600));
								as.teleport(new Location(as.getWorld(),
										129 - packet[0],
										4 + moveup
												+ (Byte.toUnsignedInt(packet[1])
														- moveup * 8) * 0.1,
										-600));
							}
							if (Byte.toUnsignedInt(packet[1]) - moveup * 8 > 8)
								moveup += 1;
							else
								break;
						}
						/*
						 * for (int i = 56 + moveup + 1; i < 56 + 255 / 8; i++)
						 * {
						 * Block block = Bukkit.getWorlds().get(0)
						 * .getBlockAt(129 - packet[0], i, -600);
						 * block.setType(Material.AIR);
						 * }
						 */
						runningtask = false;
					}
				};
				try
				{
					runningtask = true;
					getServer().getScheduler().runTask(this, runnable);
					while (runningtask)
						;
				} catch (Exception e)
				{
				}
			}
		} catch (IOException e)
		{
			System.out.println(e);
		} finally
		{
			serverSocket.close();
		}
	}
}
