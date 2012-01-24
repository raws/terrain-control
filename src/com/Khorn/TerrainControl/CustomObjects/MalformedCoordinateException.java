package com.Khorn.TerrainControl.CustomObjects;

/**
 * Exception thrown when Coordinate cannot process its input.
 * 
 * @author Ross Paffett <ross@rosspaffett.com>
 */
public class MalformedCoordinateException extends Exception {
	
	private static final long serialVersionUID = -6600163160189327824L;
	private final String line;
	
	public MalformedCoordinateException(String line) {
		super();
		this.line = line;
	}

	public String getLine() {
		return line;
	}
	
}
