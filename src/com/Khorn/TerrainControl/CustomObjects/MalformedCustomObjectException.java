package com.Khorn.TerrainControl.CustomObjects;

import java.io.File;

/**
 * Exception thrown when a custom object's configuration contains
 * an irrecoverable error. Throwing this exception indicates that
 * the custom object will not be loaded.
 * 
 * @author Ross Paffett <ross@rosspaffett.com>
 */
public class MalformedCustomObjectException extends Exception {

	private static final long serialVersionUID = -8629977827316061574L;
	
	/**
	 * The path to the custom object configuration file which
	 * triggered this MalformedCustomObjectException.
	 */
	private final File file;
	
	public MalformedCustomObjectException(String message, File file) {
		super(message);
		this.file = file;
	}
	
	public MalformedCustomObjectException(String message, File file, Throwable cause) {
		super(message, cause);
		this.file = file;
	}

	public File getFile() {
		return file;
	}
	
}
