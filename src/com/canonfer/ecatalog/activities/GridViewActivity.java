package com.canonfer.ecatalog.activities;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.canonfer.clipboard.ClipboardListener;
import com.canonfer.clipboard.DBHelper;
import com.canonfer.clipboard.StoredImage;
import com.canonfer.ecatalog.MainActivity;
import com.canonfer.ecatalog.R;
import com.canonfer.ecatalog.imageProcessing.AsyncResponse;
import com.canonfer.ecatalog.imageProcessing.DownloadImageTask;
import com.canonfer.ecatalog.imageProcessing.ImageProcessorTask;
import com.canonfer.views.CustomImageButton;
import com.canonfer.views.ExpandableHeightGridView;
import com.canonfer.views.GridViewImageAdapter;
import com.canonfer.views.GridViewImageAdapter.ViewHolder;
import com.canonfer.views.LauncherIcon;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * GridViewActivity for presenting the preview of the images
 * Main Activity that presents list of images.
 * @author Fernando Ca–on
 * @version 2014
 */
public class GridViewActivity extends Activity implements OnItemClickListener, AsyncResponse {

	public final String LogTag="Ecatalog";
	/**
	 * PREFS_NAME name for SharedPreferences
	 */
	public static final String PREFS_NAME ="AppPreferences";
	static final String EXTRA_MAP = "map";
	/**
	 *  mDB instance of the database for image storage
	 */
	private  DBHelper mDB;
	public ArrayList <LauncherIcon> mIcons;
	public static final int CAMERA_RESULT = 556;
	ImageView mSelected;
	public ExpandableHeightGridView  mGridView ;
	ScrollView mGridScroll;
	
	 final LauncherIcon[] ICONS = {
		new LauncherIcon(R.drawable.apple, "Metro", "metro.png",false),
		new LauncherIcon(R.drawable.watch, "RER", "rer.png",false),
		new LauncherIcon(R.drawable.gpdpzoom, "Bus", "bus.png",false),
		new LauncherIcon(R.drawable.yoda, "Noctilien", "noctilien.png",false),

	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.i(LogTag,this.getApplicationContext().getPackageCodePath());
		setContentView(R.layout.grid_view_activity);
		mIcons = new ArrayList();
		createDataBase();
		mGridView = (ExpandableHeightGridView) findViewById(R.id.dashboard_grid);
		mGridView.setExpanded(true);
		mGridView.setAdapter(new GridViewImageAdapter(this, mDB));
		mGridView.setOnItemClickListener(this);

		// Hack to disable GridView scrolling
		mGridView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return event.getAction() == MotionEvent.ACTION_MOVE;
			}
		});
		
		mGridScroll = (ScrollView)findViewById(R.id.grid_scroll);
		this.startClipBoardManager();
	}
/**
 *	Creates the products image database.
 * @see DBHelper
 * */
	public void createDataBase() {

		mDB = new DBHelper(getApplicationContext());
		
	}
	/**
	 * Removes the item click by the trash button
	 */
	public void deleteItem(View view) {
	    // Do something in response to button click
		CustomImageButton trashButton=(CustomImageButton)view;
		
		if (trashButton.launcherIconInAdapter != null){
			((GridViewImageAdapter)mGridView.getAdapter()).removeItem(trashButton.launcherIconInAdapter);
			
		}
	}

/**
 * OnResume performs the initialization of the DDBB and OpenCV
 * */
	@Override
	public void onResume() {
		super.onResume();
 
		this.prepareDataBaseImage();
		mGridScroll.computeScroll();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this,
				mLoaderCallback);
	}
	
	@Override
	public void onRestart()
	{
		super.onRestart();
		((GridViewImageAdapter)mGridView.getAdapter()).notifyDataSetChanged();
		mGridView.invalidate();
		mGridScroll.computeScroll();
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
	 
			String currentText = getResources().getString(R.string.edit);
			int visibility =0;
			if (item.getTitle().equals(currentText)){
				item.setTitle("Done");
				visibility =View.VISIBLE;
			}else{
				item.setTitle(R.string.edit);
				visibility=View.INVISIBLE;
			}

			final int size = mGridView.getChildCount();
			for (int i = 0; i < size; i++) {
				ViewGroup gridChild = (ViewGroup) mGridView.getChildAt(i);
				int childSize = gridChild.getChildCount();
				for (int k = 0; k < childSize; k++) {
					if (gridChild.getChildAt(k) instanceof ImageButton) {
						gridChild.getChildAt(k).setVisibility(visibility);
					}
				}
			}
		default:

		}
		return super.onOptionsItemSelected(item);

	}
	/**
	 * 
	 * Open CV initializer  Callback
	 * 
	 * */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(LogTag, "OpenCV loaded successfully");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};


	/**
	 * action performed when a image is clicked. Starts the asychronous processing of the image
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		mSelected=(ImageView) v.findViewById(R.id.dashboard_icon_img);
		Bitmap image = ((BitmapDrawable)mSelected.getDrawable()).getBitmap();
		ImageProcessorTask procesor=new ImageProcessorTask();
		procesor.delegate=GridViewActivity.this;
		procesor.execute(image);
	 
	}

	/**
	 *  ImageProcessor AsyncResponse Method
	 * @param r received bitmap from a background thread
	 * */
	public void processFinish(Bitmap r){

		Intent intent = new Intent(GridViewActivity.this, CameraActivity.class);
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		r.compress(Bitmap.CompressFormat.PNG, 50, bs);
		ExpandableHeightGridView  gridView = (ExpandableHeightGridView) findViewById(R.id.dashboard_grid);
		gridView.invalidate();
    	intent.putExtra("byteArray", bs.toByteArray());
    	startActivityForResult(intent, CAMERA_RESULT);
	}


    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  
	  if (resultCode == CAMERA_RESULT) {
		  
    	 
      }

	}
    /**
     * Converts Bitmap to bytes. Static method.
     * @param image Bitmap to conver 
     */
	public static byte [] bitmapToBytes(Bitmap image){
		
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	byte imageInByte[] = stream.toByteArray();
	return imageInByte;
	}

	
	public ArrayList getIcons() {
		return mIcons;
	}
/**
 * Previous configuraion of the data base
 */
	public void prepareDataBaseImage(){
		this.addStaticIcons();
		
		if(!mIcons.isEmpty())
			mIcons.clear();
			
		ArrayList <StoredImage> imageList= mDB.getAllStoredImages();
		for(StoredImage imgItem:imageList){
			boolean download = false;
			
			if (imgItem.getImage() == null)
			{
				download = true;
				Log.i(LogTag,"Will download: " +imgItem.getURL());
			}
			mIcons.add (new LauncherIcon(imgItem.getID(), imgItem.getURL(),imgItem.getURL() , download));
		}

		Log.i(LogTag, "Total Elements "+ mIcons.size());
	}
/**
 * this method add the initial preset images, apple, glasses.. etc. Only once in application's lifetime
 */

	public void addStaticIcons() {
		SharedPreferences settings = getSharedPreferences(this.PREFS_NAME, 0);
		boolean isInit = settings.getBoolean("isAppInit", false);
		if (isInit == true) {
			return;
		} else {

			for (int i = 0; i < ICONS.length; i++) {

				LauncherIcon icon = ICONS[i];
				Bitmap image = BitmapFactory.decodeResource(getResources(),
						icon.imgId);
				StoredImage img = new StoredImage(0, icon.text,
						bitmapToBytes(image));
				mDB.insertImage(img);
			}
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("isAppInit", true);
			editor.commit();
		}
	}

	/**
	 * this method starts the Clipboard Manager that allows the copy and paste from the browsers
	 */
	public void startClipBoardManager () {
		ClipboardManager clipManager= (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		clipManager.addPrimaryClipChangedListener(new ClipboardListener(getApplicationContext()));
	}

	
}
