package com.Khorn.TerrainControl.CustomObjects;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coordinate {

	/**
     * Regular expression for matching data string lines. Match
     * groups are as follows:
     * 
     * 1: Comma-delimited triplet of coordinates, in X,Z,Y order
     * 2: Comma-delimited list of block IDs, block data, and
     *    probabilities, e.g. "1(25),17.3,98(100)" (block data
     *    and probabilities are optional)
     * 3: Branch data pair, e.g. "0@50" (optional)
     */
    private static final Pattern dataPattern = Pattern.compile("^((?:-?\\d+,?){3}):((?:\\d+(?:\\.\\d+)?(?:\\(\\d+\\))?,?)+)(?:#(\\d+@\\d+))?$");
    
    /**
     * Regular expression for matching block IDs, block data, and
     * probabilities. Match groups are as follows:
     * 
     *  1: Block ID
     *  2: Block data (optional)
     *  3: Probability (optional)
     */
    private static final Pattern blockPattern = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\((\\d+)\\))?");
	
    /**
     * The CustomObject this Coordinate belongs to.
     */
    private CustomObject object;
    
    /**
     * The CustomObject-relative block coordinates represented by this Coordinate.
     */
    private int x, y, z;
    
    /**
     * A collection of block IDs, block data, and probabilities for each block
     * which may be placed at this Coordinate. Each element is an array of
     * integers in the following order: [block ID, block data, probability]
     * 
     * Probability is an integer in the range 0 to 100 describing how likely
     * this block ID and block data will be placed at this Coordinate's position.
     * The probability is considered each time the object is spawned. If the
     * object spawner reaches the end of the block list without choosing a
     * block, no block will be placed.
     */
    private WeightedBlockCollection blocks;
    
    public int workingData = 0;
    public int workingExtra = 0;
    private String dataString;
    private int branchOdds = -1;
    public int branchDirection = -1;
    public boolean dig;
    
    public Coordinate(CustomObject object, String line) throws MalformedCoordinateException, EmptyCoordinateException
    {
    	Matcher matcher = dataPattern.matcher(line);
    	if (matcher.matches() && matcher.group(1) != null && matcher.group(2) != null)
    	{
    		this.object = object;
    		this.blocks = new WeightedBlockCollection(getRandom());
    		
    		/**
    		 * Coordinates
    		 */
    		String[] coordinates = matcher.group(1).split(",");
    		this.x = Integer.valueOf(coordinates[0]);
    		this.z = Integer.valueOf(coordinates[1]);
    		this.y = Integer.valueOf(coordinates[2]);
    		
    		/**
    		 * Block ID, data, and probability
    		 */
    		String[] blockLines = matcher.group(2).split(",");
    		for (String blockLine : blockLines)
    		{
    			Matcher blockMatcher = blockPattern.matcher(blockLine);
    			if (blockMatcher.matches() && blockMatcher.group(1) != null)
    			{
    				int[] block = { 0, 0 };
    				block[0] = Integer.valueOf(blockMatcher.group(1));
    				
    				if (blockMatcher.group(2) != null)
    				{
    					block[1] = Integer.valueOf(blockMatcher.group(2));
    				}
    				
    				int probability = 1;
    				if (blockMatcher.group(3) != null)
    				{
    					probability = Integer.valueOf(blockMatcher.group(3));
    				}
    				
    				getBlocks().add(probability, block);
    			}
    			else
    			{
    				throw new MalformedCoordinateException(line);
    			}
    		}
    		
    		/**
    		 * If all probabilities were zero, then the weighted block collection
    		 * will be empty, as it discards all zero-weighted entries.
    		 */
    		if (getBlocks().size() == 0) throw new EmptyCoordinateException(line);
    		
    		/**
    		 * Branch direction and probability
    		 */
    		if (matcher.group(3) != null)
    		{
    			String[] branch = matcher.group(3).split("@");
    			this.branchDirection = Integer.valueOf(branch[0]);
    			this.branchOdds = Integer.valueOf(branch[1]);
    		}
    	}
    	else
    	{
    		throw new MalformedCoordinateException(line);
    	}
    	
    	/**
    	 * Legacy support during refactor
    	 * TODO Get rid of this
    	 */
    	int[] firstBlock = getBlocks().firstBlock();
		this.workingData = firstBlock[0];
		this.workingExtra = firstBlock[1];
		this.dig = object.dig;
    }
    
    public Coordinate(CustomObject object, int x, int y, int z, WeightedBlockCollection blocks, int branchDirection, int branchOdds)
    {
    	this.object = object;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.blocks = new WeightedBlockCollection(blocks);
    	this.branchDirection = branchDirection;
    	this.branchOdds = branchOdds;
    	
    	/**
    	 * Legacy support during refactor
    	 * TODO Get rid of this
    	 */
    	int[] firstBlock = getBlocks().firstBlock();
    	this.workingData = firstBlock[0];
    	this.workingExtra = firstBlock[1];
    	this.dig = object.dig;
    }
    
    public Coordinate(Coordinate coordinate)
    {
    	this(coordinate.getObject(), coordinate.getX(), coordinate.getY(), coordinate.getZ(), coordinate.getBlocks(), coordinate.branchDirection, coordinate.branchOdds);
    }
    
    public Coordinate(int initX, int initY, int initZ, String initData, boolean digs)
    {
        x = initX;
        y = initY;
        z = initZ;
        dataString = initData;
        dig = digs;
    }

    public CustomObject getObject()
    {
		return object;
	}

	public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }

    public int getChunkX(int _x)
    {
        return (_x + x) >> 4;
    }

    public int getChunkZ(int _z)
    {
        return (_z + z) >> 4;
    }

    public WeightedBlockCollection getBlocks()
    {
		return blocks;
	}
    
    public boolean shouldDig()
    {
    	return getObject().dig;
    }
    
    public Random getRandom()
    {
    	return getObject().getRandom();
    }
    
    /**
     * Return a block ID and block data pair according to this
     * Coordinate's block probabilities.
     * 
     * The int[2] returned contains elements in the following
     * order: [block ID, block data]
     * 
     * @return an int[2] block ID and block data pair
     */
    public int[] getBlockIdAndData()
    {
    	return getBlocks().next();
    }

	public static int RotateData(int type, int data)
    {

        switch (type)
        {
            case 50:
            case 75:
            case 76:
                switch (data)
                {
                    case 1:
                        return 3;
                    case 2:
                        return 4;
                    case 3:
                        return 2;
                    case 4:
                        return 1;
                }
                break;
            case 66:
                switch (data)
                {
                    case 6:
                        return 7;
                    case 7:
                        return 8;
                    case 8:
                        return 9;
                    case 9:
                        return 6;
                }

            case 27:
            case 28:
                switch (data & 0x7)
                {
                    case 0:
                        return 0x1 | data & 0xFFFFFFF8;
                    case 1:
                        return 0x0 | data & 0xFFFFFFF8;
                    case 2:
                        return 0x5 | data & 0xFFFFFFF8;
                    case 3:
                        return 0x4 | data & 0xFFFFFFF8;
                    case 4:
                        return 0x2 | data & 0xFFFFFFF8;
                    case 5:
                        return 0x3 | data & 0xFFFFFFF8;
                }
                break;
            case 53:
            case 67:
            case 108:
            case 109:
            case 114:
                switch (data)
                {
                    case 0:
                        return 2;
                    case 1:
                        return 3;
                    case 2:
                        return 1;
                    case 3:
                        return 0;
                }
                break;
            case 69:
            case 77:
                int thrown = data & 0x8;
                int withoutThrown = data & 0xFFFFFFF7;
                switch (withoutThrown)
                {
                    case 1:
                        return 0x3 | thrown;
                    case 2:
                        return 0x4 | thrown;
                    case 3:
                        return 0x2 | thrown;
                    case 4:
                        return 0x1 | thrown;
                }
                break;
            case 64:
            case 71:
                int topHalf = data & 0x8;
                int swung = data & 0x4;
                int withoutFlags = data & 0xFFFFFFF3;
                switch (withoutFlags)
                {
                    case 0:
                        return 0x1 | topHalf | swung;
                    case 1:
                        return 0x2 | topHalf | swung;
                    case 2:
                        return 0x3 | topHalf | swung;
                    case 3:
                        return 0x0 | topHalf | swung;
                }
                break;
            case 63:
                return (data + 4) % 16;
            case 23:
            case 54:
            case 61:
            case 62:
            case 65:
            case 68:
                switch (data)
                {
                    case 2:
                        return 5;
                    case 3:
                        return 4;
                    case 4:
                        return 2;
                    case 5:
                        return 3;
                }
                break;
            case 86:
            case 91:
                switch (data)
                {
                    case 0:
                        return 1;
                    case 1:
                        return 2;
                    case 2:
                        return 3;
                    case 3:
                        return 0;
                }
                break;
            case 93:
            case 94:
                int dir = data & 0x3;
                int delay = data - dir;
                switch (dir)
                {
                    case 0:
                        return 0x1 | delay;
                    case 1:
                        return 0x2 | delay;
                    case 2:
                        return 0x3 | delay;
                    case 3:
                        return 0x0 | delay;
                }
                break;
            case 96:
                int withoutOrientation = data & 0xFFFFFFFC;
                int orientation = data & 0x3;
                switch (orientation)
                {
                    case 0:
                        return 0x3 | withoutOrientation;
                    case 1:
                        return 0x2 | withoutOrientation;
                    case 2:
                        return 0x0 | withoutOrientation;
                    case 3:
                        return 0x1 | withoutOrientation;
                }
                break;
            case 29:
            case 33:
            case 34:
                int rest = data & 0xFFFFFFF8;
                switch (data & 0x7)
                {
                    case 2:
                        return 0x5 | rest;
                    case 3:
                        return 0x4 | rest;
                    case 4:
                        return 0x2 | rest;
                    case 5:
                        return 0x3 | rest;
                }
                break;
            case 99:
            case 100:
                if (data >= 10)
                    return data;
                return data * 3 % 10;
            case 106:
                return (data << 1 | data >> 3) & 0xF;
            case 107:
                return data + 1 & 0x3 | data & 0xFFFFFFFC;

        }

        return data;
    }


    public void Rotate()
    {
        this.workingExtra = RotateData(this.workingData,this.workingExtra);
        if (branchDirection != -1)
        {
            branchDirection = branchDirection + 1;
            if (branchDirection > 3)
            {
                branchDirection = 0;
            }
        }
        int tempx = x;
        x = z;
        z = (tempx * (-1));
    }
    
    public void RegisterData()
    {
        String workingDataString = dataString;
        String workingExtraString;
        String branchDataString = null;
        if (workingDataString.contains("#"))
        {
            String stringSet[] = workingDataString.split("#");
            workingDataString = stringSet[0];
            branchDataString = stringSet[1];

        }
        if (workingDataString.contains("."))
        {
            String stringSet[] = workingDataString.split("\\.");
            workingDataString = stringSet[0];
            workingExtraString = stringSet[1];
            workingExtra = Integer.parseInt(workingExtraString);
        }
        workingData = Integer.parseInt(workingDataString);
        if (branchDataString != null)
        {
            String stringSet[] = branchDataString.split("@");
            branchDirection = Integer.parseInt(stringSet[0]);
            branchOdds = Integer.parseInt(stringSet[1]);
        }
    }

    Coordinate GetCopy(int initX, int initY, int initZ, String initData, boolean digs)
    {
        Coordinate copy = new Coordinate(initX, initY, initZ, initData, digs);

        copy.workingData = this.workingData;
        copy.workingExtra = this.workingExtra;
        copy.branchDirection = this.branchDirection;
        copy.branchOdds = this.branchOdds;
        return copy;
    }

    public Coordinate GetCopy()
    {
        return this.GetCopy(x, y, z, dataString, dig);

    }
    
    public Coordinate clone()
    {
    	return new Coordinate(this);
    }

    public Coordinate GetSumm(Coordinate workCoord)
    {
        return this.GetCopy(x + workCoord.getX(), y + workCoord.getY(), z + workCoord.getZ(), dataString, dig);

    }
}