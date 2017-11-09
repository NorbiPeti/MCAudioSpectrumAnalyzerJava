package io.github.norbipeti.audiospectrum;

import org.bukkit.map.MapRenderer;

public abstract class BarsRendererBase extends MapRenderer
{
	protected int[] bars;
	protected byte firstrender = 0;
	protected byte count = 16;

	public BarsRendererBase(int[] bars)
	{
		this.bars = bars;
	}

	public byte setBarCount(byte count)
	{
		firstrender = 0;
		if (count >= 0 && count <= 16)
			this.count = count;
		return this.count;
	}
}
