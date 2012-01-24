package com.Khorn.TerrainControl.CustomObjects;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class WeightedBlockCollection {
	
	private NavigableMap<Integer, int[]> blocks = new TreeMap<Integer, int[]>();
	private Random random;
	private int total = 0;
	
	public WeightedBlockCollection()
	{
		this(new Random());
	}
	
	public WeightedBlockCollection(Random random)
	{
		this.random = random;
	}
	
	public WeightedBlockCollection(WeightedBlockCollection collection)
	{
		this(collection, new Random());
	}
	
	public WeightedBlockCollection(WeightedBlockCollection collection, Random random)
	{
		this(random);
		this.blocks = new TreeMap<Integer, int[]>(collection.getMap());
		this.total = collection.getTotal();
	}
	
	public NavigableMap<Integer, int[]> getMap()
	{
		return blocks;
	}
	
	public int getTotal()
	{
		return total;
	}
	
	public void add(int[] block)
	{
		int weight = block[2];
		add(weight, block);
	}
	
	public void add(int weight, int[] block)
	{
		if (weight <= 0) return;
		total += weight;
		blocks.put(total, block);
	}
	
	public int[] next()
	{
		int value = random.nextInt(total) + 1;
		return blocks.ceilingEntry(value).getValue();
	}
	
	public int[] firstBlock()
	{
		return blocks.get(blocks.firstKey());
	}
	
	public int size()
	{
		return blocks.size();
	}
	
	public void clear()
	{
		blocks.clear();
		total = 0;
	}
	
}
