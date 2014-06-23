package com.canonfer.clipboard;

public class StoredImage {

	// private variables
	int _id;
	String _url;
	byte[] _image;

	// Empty constructor

	public StoredImage() {

	}

	public StoredImage(int keyID,String url, byte[] image) {
		this._id=keyID;
		this._image = image;
		this._url = url;
	}
	public int getID(){	
		return this._id;
	}
	
	public String getURL() {
		return this._url;
	}

	// setting Url
	public void setURL(String url) {
		this._url = url;
	}

	// getting Image
	public byte[] getImage() {
		return this._image;
	}

	// setting Image
	public void setImage(byte[] image) {
		this._image = image;
	}

}
