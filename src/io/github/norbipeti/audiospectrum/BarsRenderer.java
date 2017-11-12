package io.github.norbipeti.audiospectrum;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.bukkit.entity.Player;
import org.bukkit.map.*;

public class BarsRenderer extends BarsRendererBase
{
	private boolean single = true;

	public BarsRenderer(FloatBuffer bars)
	{
		super(bars);
		//System.out.println("black: " + MapPalette.matchColor(Color.black));
		//System.out.println("BLACK: " + MapPalette.matchColor(Color.BLACK));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render(MapView mv, MapCanvas mc, Player pl)
	{ //Width: 8, empty space: 8, count per map: 8
		if (firstrender < 4 ? firstrender++ < 4 : false) //Only increment if true
			for (int i = 0; i < 128; i++)
				for (int j = 0; j < 128; j++)
					mc.setPixel(i, j, MapPalette.matchColor(Color.black));
		if (single)
		{
			if (mv.getId() != 0)
				return;
			for (int i = 0; i < 16 && i < count; i++)
				for (int j = 0; j < 128; j++)
					for (int k = 0; k < 4; k++)
						mc.setPixel(i * 8 + k, 128 - j, j < bars.get(i * 64) / 2
								? MapPalette.matchColor(j, 255 - j * 2, 0) : MapPalette.matchColor(Color.BLACK));
			return;
		}
		int offsetx = mv.getId() % 2 * 8, offsety = mv.getId() < 2 ? -128 : 0;
		for (int i = 0; i < 8 && i < count - offsetx; i++)
			for (int j = 0; j < 128; j++)
				for (int k = 0; k < 8; k++)
					mc.setPixel(i * 16 + k, 128 - j,
							j < bars.get((offsetx + i) * 64) + offsety
									? MapPalette.matchColor(j - offsety, 255 - j + offsety, 0)
									: MapPalette.matchColor(Color.BLACK));
	}

	/**
	 * Sets whether to use a single map or 4
	 */
	public boolean toggleSingle()
	{
		firstrender = 0;
		return this.single = !single;
	}
}
