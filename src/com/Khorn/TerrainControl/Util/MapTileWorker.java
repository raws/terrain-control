package com.Khorn.TerrainControl.Util;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.World;

/**
 * A worker class responsible for generating one chunk of a biome map.
 * 
 * @author Ross Paffett
 */
public class MapTileWorker implements Runnable {

	/**
	 * The BufferedImage representing the entire map. Each MapTileWorker should only access the
	 * region of the map that it is responsible for.
	 */
	private final BufferedImage map;
	
	/**
	 * A color index containing colors for each biome.
	 */
	private final int[] colors;
	
	/**
	 * The world for which the map is being generated.
	 */
	private final World world;
	
	/**
	 * The chunk coordinates for which this worker is responsible. To find this worker's
	 * applicable world block coordinates, multiply the chunk coordinates by the chunk's
	 * block size (16 blocks by default).
	 */
	private final int chunkX, chunkZ;
	
	public MapTileWorker(BufferedImage map, int[] colors, World world, int chunkX, int chunkZ) {
		this.map = map;
		this.colors = colors;
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
	}
	
	@Override
	public void run() {
		BufferedImage image = map.getSubimage(chunkX * 16, chunkZ * 16, 16, 16);
		BiomeBase[] biomes = new BiomeBase[256]; 
		biomes = world.getWorldChunkManager().a(biomes, chunkX * 16, chunkZ * 16, 16, 16);
		int imageX, imageY;
		
		for (int blockX = 0; blockX < 16; blockX++) {
			for (int blockZ = 0; blockZ < 16; blockZ++) {
				imageX = map.getWidth() * 16 - ((chunkZ + map.getWidth() / 2) * 16 + blockZ + 1);
				imageY = (chunkX + map.getHeight() / 2) * 16 + blockX;
				
				try {
					image.setRGB(blockX, blockZ, colors[biomes[blockX + (16 * blockZ)].F]);
					// map.setRGB(imageX, imageY, colors[biomes[blockX + (16 * blockZ)].F]);
				} catch (RasterFormatException e) {
					err(e.getClass().getName() + ": " + e.getMessage());
					return;
				}
				
				Thread.yield();
			}
		}
		
		map.setData(image.getData());
	}
	
	private void log(String message) {
		System.out.println("[MapTileWorker(" + chunkX + ", " + chunkZ + ")] " + message);
	}
	
	private void err(String message) {
		System.err.println("[MapTileWorker(" + chunkX + ", " + chunkZ + ")] " + message);
	}

}
