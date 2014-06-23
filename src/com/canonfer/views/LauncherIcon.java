package com.canonfer.views;
 
public class LauncherIcon {
	public final String text;
	public int imgId;
	final String map;
	public boolean remote;

	public LauncherIcon(int imgId, String text, String map, boolean downloadable) {
		super();
		this.imgId = imgId;
		this.text = text;
		this.map = map;
		this.remote =downloadable;
	}

}