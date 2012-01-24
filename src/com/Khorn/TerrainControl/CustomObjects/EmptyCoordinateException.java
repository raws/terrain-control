package com.Khorn.TerrainControl.CustomObjects;

/**
 * Exception thrown if a Coordinate's weighted block collection is empty
 * after the Coordinate is initialized.
 * 
 * @author Ross Paffett <ross@rosspaffett.com>
 */
public class EmptyCoordinateException extends Exception {
	
	private static final long serialVersionUID = -3634446397421649107L;
	private final String line;
	
	public EmptyCoordinateException(String line) {
		super();
		this.line = line;
	}
	
	public String getLine() {
		return line;
	}
	
}
