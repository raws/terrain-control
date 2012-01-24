package com.Khorn.TerrainControl.CustomObjects;

public class EmptyCustomObjectException extends Exception {

	private static final long serialVersionUID = -8328779061666131711L;
	private final CustomObject object;
	
	public EmptyCustomObjectException(CustomObject object) {
		this.object = object;
	}
	
	public CustomObject getObject() {
		return object;
	}
	
}
