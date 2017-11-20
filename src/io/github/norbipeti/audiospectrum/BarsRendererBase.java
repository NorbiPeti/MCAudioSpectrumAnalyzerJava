package io.github.norbipeti.audiospectrum;

import java.nio.FloatBuffer;

import org.bukkit.map.MapRenderer;

public abstract class BarsRendererBase extends MapRenderer
{
	protected volatile FloatBuffer bars;
	protected byte firstrender = 0;
	protected byte count = 16;

	public BarsRendererBase(FloatBuffer bars)
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
