package io.github.norbipeti.audiospectrum;

import org.bukkit.entity.Player;
import org.bukkit.map.*;

public class BarsRenderer extends MapRenderer
{
	private int[] bars;

	public BarsRenderer(int[] bars)
	{
		this.bars = bars;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView mv, MapCanvas mc, Player pl)
	{ //Width: 16, empty space: 16, count per map: 8
		int offsetx = mv.getId() % 2 * 8, offsety = mv.getId() < 2 ? -128 : 0;
		//System.out.println("OX: " + offsetx + " OY: " + offsety + " ID: " + mv.getId());
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 128; j++)
				for (int k = 0; k < 16; k++)
					mc.setPixel(i * 32 + k, 128 - j, j < bars[offsetx + i] + offsety
							? MapPalette.matchColor(255 - j + offsety, j - offsety, 0) : 0); //TODO: 0 is transparent
	} //TODO: Render areas inbetween black
}
