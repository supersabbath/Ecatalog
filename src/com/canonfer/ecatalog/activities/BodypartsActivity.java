package com.canonfer.ecatalog.activities;

import android.app.Activity;
import com.canonfer.ecatalog.R;
import com.canonfer.ecatalog.util.*;
import com.canonfer.views.GridViewImageAdapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

public class BodypartsActivity extends Activity {

	public static final int NUM_OF_COLUMNS = 3;

	// Gridview image padding
	public static final int GRID_PADDING = 8; // in dp
	private Utils utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private GridViewImageAdapter adapter;
	private GridView gridView;
	private int columnWidth;
	static final int REQUEST_IMAGE_CAPTURE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_grid_view);
		gridView = (GridView) findViewById(R.id.grid_view);
		utils = new Utils(this);

		// Initilizing Grid View
		InitilizeGridLayout();

		// loading all image paths from SD card
		imagePaths = utils.getFilePaths();

		// Gridview adapter
	/*	adapter = new GridViewImageAdapter(BodypartsActivity.this, imagePaths,
				columnWidth);
*/
		// setting grid view adapter
		gridView.setAdapter(adapter);
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				GRID_PADDING, r.getDisplayMetrics());

		columnWidth = (int) ((utils.getScreenWidth() - ((NUM_OF_COLUMNS + 1) * padding)) / NUM_OF_COLUMNS);

		gridView.setNumColumns(NUM_OF_COLUMNS);
		gridView.setColumnWidth(columnWidth);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setPadding((int) padding, (int) padding, (int) padding,
				(int) padding);
		gridView.setHorizontalSpacing((int) padding);
		gridView.setVerticalSpacing((int) padding);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.open:

		default:
			Intent takePictureIntent = new Intent(
					MediaStore.ACTION_IMAGE_CAPTURE);
			if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

			try {
				Bundle extras = data.getExtras();
				Bitmap imageBitmap = (Bitmap) extras.get("data");
				
				File pictureFile = new File(
						Environment.getExternalStorageDirectory() + "/ecatalog");
				pictureFile.mkdirs();
				pictureFile = new File(pictureFile.getAbsolutePath()
						+ "picture" + System.currentTimeMillis() + ".jpg");
				FileOutputStream fileOutputStream = new FileOutputStream(
						pictureFile);

				BufferedOutputStream bos = new BufferedOutputStream(
						fileOutputStream);

				imageBitmap.compress(CompressFormat.JPEG, 90, bos);

				bos.flush();
				bos.close();
		//		adapter.addPath(pictureFile.getAbsolutePath());
				
			} catch (FileNotFoundException e) {

				e.printStackTrace();

				Toast.makeText(this, "fail to save image", 4).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "fail to save image", 4).show();;
			}
		}
		
		adapter.notifyDataSetChanged();
	}
	
	
	private	Uri saveMediaEntry(String imagePath) {

		File imageFile = new File(imagePath);

		ContentValues v = new ContentValues();
		v.put(Images.Media.TITLE, "photo_ecatalog" + imageFile.getName());
		v.put(Images.Media.DISPLAY_NAME, "photo_ecatalog" + imageFile.getName());
		v.put(Images.Media.MIME_TYPE, "image/jpeg");

		File f = new File(imagePath);
		File parent = f.getParentFile();
		String path = parent.toString().toLowerCase();
		String name = parent.getName().toLowerCase();
		v.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
		v.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
		v.put(Images.Media.SIZE, f.length());
		f = null;

		v.put("_data", imagePath);
		ContentResolver c = getContentResolver();
		return c.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
	}

}
