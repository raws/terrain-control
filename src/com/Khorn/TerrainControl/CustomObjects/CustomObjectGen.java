package com.Khorn.TerrainControl.CustomObjects;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.server.BiomeBase;
import net.minecraft.server.Block;
import net.minecraft.server.World;

import org.bukkit.Chunk;

import com.Khorn.TerrainControl.Configuration.WorldConfig;

public class CustomObjectGen
{

    private static boolean ObjectCanSpawn(World world, int x, int y, int z, CustomObject obj)
    {
        if ((world.getTypeId(x, y - 5, z) == 0) && (obj.needsFoundation))
            return false;

        boolean abort = false;
        int checkBlock = world.getTypeId(x, y + 2, z);
        if (!obj.spawnWater)
            abort = ((checkBlock == Block.WATER.id) || (checkBlock == Block.STATIONARY_WATER.id));
        if (!obj.spawnLava)
            abort = ((checkBlock == Block.LAVA.id) || (checkBlock == Block.STATIONARY_LAVA.id));

        checkBlock = world.getLightLevel(x, y + 2, z);
        if (!obj.spawnSunlight)
            abort = (checkBlock > 8);
        if (!obj.spawnDarkness)
            abort = (checkBlock < 9);

        if ((y < obj.spawnElevationMin) || (y > obj.spawnElevationMax))
            abort = true;

        if (!obj.spawnOnBlockType.contains(world.getTypeId(x, y - 1, z)))
            abort = true;

        return !abort;
    }

    public static boolean SpawnCustomObjects(World world, Random rand, WorldConfig worldSettings, int chunk_x, int chunk_z, BiomeBase localBiomeBase)
    {

        if (worldSettings.Objects.size() == 0)
            return false;

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned)
        {
            if (spawnattemps > worldSettings.objectSpawnRatio)
                return false;

            spawnattemps++;

            CustomObject SelectedObject = worldSettings.Objects.get(rand.nextInt(worldSettings.Objects.size()));

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(localBiomeBase))
                continue;

            int randomRoll = rand.nextInt(100);
            int ObjectRarity = SelectedObject.rarity;

            while (randomRoll < ObjectRarity)
            {
                int x = chunk_x + rand.nextInt(16);
                int z = chunk_z + rand.nextInt(16);
                int y = world.getHighestBlockYAt(x, z);
                ObjectRarity -= 100;

                if (!ObjectCanSpawn(world,x, y, z, SelectedObject))
                    continue;

                objectSpawned = GenerateCustomObject(world,rand, worldSettings,x, y, z, SelectedObject, false);
                // Checked Biome, Branch, Tree - soo try to generate.

                // here we spawn object and check group spawning

                if (objectSpawned && !SelectedObject.groupId.endsWith(""))
                {
                    ArrayList<CustomObject> groupList = worldSettings.ObjectGroups.get(SelectedObject.groupId);
                    if (groupList == null)
                        return objectSpawned;

                    int attempts = 3;
                    if ((SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin) > 0)
                        attempts = SelectedObject.groupFrequencyMin + rand.nextInt(SelectedObject.groupFrequencyMax - SelectedObject.groupFrequencyMin);

                    while (attempts > 0)
                    {
                        attempts--;

                        int objIndex = rand.nextInt(groupList.size());
                        CustomObject ObjectFromGroup = groupList.get(objIndex);

                        // duno about this check, but maybe it is correct
                        if (ObjectFromGroup.branch || !ObjectFromGroup.canSpawnInBiome(localBiomeBase))
                            continue;

                        x = x + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        z = z + rand.nextInt(SelectedObject.groupSeperationMax - SelectedObject.groupSeperationMin) + SelectedObject.groupSeperationMin;
                        int _y = world.getHighestBlockYAt(x, z);
                        if ((y - _y) > 10 || (_y - y) > 10)
                            continue;

                        if (!ObjectCanSpawn(world,x, y, z, ObjectFromGroup))
                            continue;
                        GenerateCustomObject(world,rand, worldSettings,x, _y, z, ObjectFromGroup, false);


                    }

                }

            }

        }
        return objectSpawned;
    }


    public static boolean SpawnCustomTrees(World world, Random rand, WorldConfig worldSettings, int x, int y, int z)
    {

        if (!worldSettings.HasCustomTrees)
            return false;
        if (world.getTypeId(x, y, z) != Block.SAPLING.id)
            return false;

        int oldData = world.getData(x, y, z) & 0x3;
        world.setRawTypeId(x, y, z, 0);

        Chunk chunk = world.getWorld().getChunkAt(x >> 4, z >> 4);

        BiomeBase localBiomeBase = world.getWorldChunkManager().getBiome(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16);

        boolean objectSpawned = false;
        int spawnattemps = 0;
        while (!objectSpawned && spawnattemps < worldSettings.objectSpawnRatio)
        {
            CustomObject SelectedObject = worldSettings.Objects.get(rand.nextInt(worldSettings.Objects.size()));

            spawnattemps++;

            if (SelectedObject.branch || !SelectedObject.canSpawnInBiome(localBiomeBase) || !SelectedObject.tree)
                continue;


            int randomRoll = rand.nextInt(100);

            if (randomRoll < SelectedObject.rarity)
            {
                if (CustomObjectGen.ObjectCanSpawn(world, x, y, z, SelectedObject))
                    objectSpawned = GenerateCustomObject(world,rand, worldSettings,x, y, z, SelectedObject, true);
            }

        }
        if (!objectSpawned)
            world.setRawTypeIdAndData(x, y, z, Block.SAPLING.id, oldData);
        return objectSpawned;
    }

    public static boolean GenerateCustomObject(World world, Random rand, WorldConfig worldSettings, int x, int y, int z, CustomObject workObject, boolean notify)
    {
        /*
         * 1) ground check (moved to ObjectCanSpawn)
         * 2) add branches and copy all data to work array (dont change default CustomObject object)
         * 3) collision check and rotation
         * 4) spawn
         */

        // 1)

        // 2)

        int index = 0;
        int branchLimit = 0;
        ArrayList<CustomObject> branchGroup = worldSettings.BranchGroups.get(workObject.groupId);
        
        ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
        while (index < workObject.Data.size())
        {
            Coordinate coordinate = workObject.Data.get(index);
            coordinates.add(coordinate.clone());

            if ((coordinate.branchDirection != -1) && (branchGroup != null) && (branchLimit < workObject.branchLimit))
            {
                CustomObject workingBranch = branchGroup.get(rand.nextInt(branchGroup.size()));
                int counter = 0;
                while (counter < workingBranch.Data.size())
                {
                    Coordinate untranslatedCoordinate = workingBranch.Data.get(counter).GetCopy();
                    int directionCounter = 0;
                    while (directionCounter < (coordinate.branchDirection))
                    {
                        untranslatedCoordinate.Rotate();
                        directionCounter++;
                    }

                    coordinates.add(untranslatedCoordinate.GetSumm(coordinate));
                    counter++;
                }

            }
            index++;
        }

        // 3)
        int RotationAmount = 0;
        index = 0;
        int faultCounter = 0;

        if (workObject.randomRotation)
        {
            RotationAmount = rand.nextInt(3);
        }


        while (index < coordinates.size())
        {
            int counter = 0;
            Coordinate point = coordinates.get(index);
            while (counter < RotationAmount)
            {
                point.Rotate();
                counter++;
            }
            if(!world.isLoaded(point.getX() + x, point.getY() + y,point.getZ() + z))
                return false;


            if (!workObject.dig)
            {
                if (world.getTypeId((x + point.getX()), (y + point.getY()), (z + point.getZ())) > 0)
                {
                    faultCounter++;
                    if (faultCounter > (coordinates.size() * (workObject.collisionPercentage / 100)))
                    {
                        return false;
                    }
                }
            }
            index++;
        }

        // 4)

        index = 0;
        while (index < coordinates.size())
        {
            Coordinate coordinate = coordinates.get(index);
            int[] block = coordinate.getBlockIdAndData();
            if (block != null && (world.getTypeId(x + coordinate.getX(), y + coordinate.getY(), z + coordinate.getZ()) == 0 || coordinate.shouldDig()))
            {
            	ChangeWorld(world, notify, (x + coordinate.getX()), (y + coordinate.getY()), (z + coordinate.getZ()), block);
            }
            if ((!worldSettings.denyObjectsUnderFill) && (workObject.underFill) && (world.getTypeId(x + coordinate.getX(), y, z + coordinate.getZ()) > 0))
            {
                int depthScanner = 0;
                int blockForFill = world.getTypeId(x, y - 1, z);
                while (depthScanner < 64)
                {
                    if (coordinate.getY() < depthScanner)
                    {
                        int countdown = depthScanner;
                        while ((world.getTypeId(x + coordinate.getX(), y + coordinate.getY() - countdown, z + coordinate.getZ()) == 0) && (countdown < 64))
                        {
                            ChangeWorld(world,notify, (x + coordinate.getX()), y + coordinate.getY() - countdown, z + coordinate.getZ(), blockForFill, 0);
                            countdown++;
                        }
                    }
                    depthScanner++;
                }
            }
            index++;
        }
        return true;

    }


    private  static  boolean ChangeWorld(World world, boolean notify, int x, int y, int z, int type, int data)
    {
        if (notify)
            return world.setTypeIdAndData(x, y, z, type, data);
        else
            return world.setRawTypeIdAndData(x, y, z, type, data);

    }

    private static boolean ChangeWorld(World world, boolean notify, int x, int y, int z, int[] block)
    {
    	return ChangeWorld(world, notify, x, y, z, block[0], block[1]);
    }

}
