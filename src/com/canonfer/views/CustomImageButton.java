package com.canonfer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class CustomImageButton extends ImageButton {

	public int referenceID;
	public LauncherIcon launcherIconInAdapter;
	
	public CustomImageButton(Context context) {
		super(context);
		referenceID = 0;
	}

	public CustomImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		referenceID = 0;
	}

	public CustomImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		referenceID = 0;
	}

}
