package io.github.norbipeti.audiospectrum;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BarsRenderer extends MapRenderer
{
	private int[] bars;

	public BarsRenderer(int[] bars)
	{
		this.bars = bars;
	}

	@Override
	public void render(MapView arg0, MapCanvas arg1, Player arg2)
	{

	}
}
