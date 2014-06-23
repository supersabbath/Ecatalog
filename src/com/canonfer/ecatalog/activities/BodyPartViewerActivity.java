package com.canonfer.ecatalog.activities;

import com.canonfer.ecatalog.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.HorizontalScrollView
;
import android.widget.ImageView;

public class BodyPartViewerActivity extends Activity {

	private HorizontalScrollView  gallery;
	private ImageView bodyImageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.body_viewer_activity);
		gallery =(HorizontalScrollView) findViewById(R.id.gallery1);
		bodyImageView=(ImageView)findViewById(R.id.imageView1);
		
	}

}
