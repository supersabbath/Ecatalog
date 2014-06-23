package com.canonfer.ecatalog.activities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.canonfer.ecatalog.R;
import com.canonfer.ecatalog.imageProcessing.AsyncResponse;
import com.canonfer.ecatalog.imageProcessing.ImageProcessorTask;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


/**
 * Based on:
 * 
 * @author Fernando Canon
 */
@SuppressLint("NewApi")
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	/**
	 * 
	 * mPreviewRunning controls the changes on the surface
	 */
	Camera mCamera;
	boolean mPreviewRunning = false;
	boolean isFrontCamera = false;
	private Context mContext = this;
	private android.widget.RelativeLayout.LayoutParams layoutParams;
	/**
	 * Camera and Product image Layout
	 */
	private RelativeLayout cameraImageLayout;
	/**
	 * Camera surface
	 */
		private CameraPreview mPreview;
		private FrameLayout mPreviewFrame;
	/**
	 * ImageView Container for the final photo
	 */
	private ImageView resultImageView;
	/**
	 * Product Image View
	 */
	private ImageView productImageview;
	/**
	 * Camera's button
	 */
	private ImageButton takePictureButton;

	private File pictureFile = null;
	private ScaleGestureDetector SGD;
	private TranslateAnimation TAnimator;
	private RelativeLayout mToolbarLayout;
	private ProgressBar mSpinner;
	private SeekBar mSizerBar;
	float scaleFactor;
	
	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		Log.e(TAG, "onCreate");
		
		scaleFactor=1;
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_activity);
		mToolbarLayout=(RelativeLayout) findViewById(R.id.toolbar);
		cameraImageLayout = (RelativeLayout) findViewById(R.id.cameraImageLayout);
	//	cameraImageLayout.setOnDragListener(new myDragEventListener());
		takePictureButton = (ImageButton) findViewById(R.id.takePictureButton);
		
		 
		takePictureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {// Click to make the photo
				findViewById(R.id.cameraProgressBar).setVisibility(View.VISIBLE);
				findViewById(R.id.galleryProgressBar).setVisibility(View.VISIBLE);
				mCamera.takePicture(null, null, mPictureCallback);

			}
		});
		productImageview = (ImageView) findViewById(R.id.productImageView);
	
		
		resultImageView = (ImageView) findViewById(R.id.resultImageView);
		
		if (getIntent().hasExtra("byteArray")) {

			Bitmap b = BitmapFactory.decodeByteArray(getIntent()
					.getByteArrayExtra("byteArray"), 0, getIntent()
					.getByteArrayExtra("byteArray").length);
			productImageview.setImageBitmap(b);

			productImageview.setVisibility(View.VISIBLE);
	//		SGD = new ScaleGestureDetector(this, new ScaleListener());

		}
		mSizerBar= (SeekBar)findViewById(R.id.seek1);
		mSizerBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			 
			
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                scaleFactor = progress;
                scaleFactor /=100;
                
                float w =productImageview.getHeight() * scaleFactor;
                float h = productImageview.getWidth() * scaleFactor;
                Log.i(TAG, "value: " + progress);
        
            /*    ScaleAnimation animation = new ScaleAnimation(scaleFactor, scaleFactor, scaleFactor,
                		scaleFactor, Animation.RELATIVE_TO_SELF, 0.5f,
    					Animation.RELATIVE_TO_SELF, 0.5f);
    			animation.setFillAfter(true);
    			*/
    			ObjectAnimator animX = ObjectAnimator.ofFloat(productImageview, "scaleX", scaleFactor);
    			ObjectAnimator animY = ObjectAnimator.ofFloat(productImageview, "scaleY", scaleFactor);
    			AnimatorSet animSetXY = new AnimatorSet();
    			animSetXY.playTogether(animX, animY);
    			animSetXY.start();
    		//	productImageview.startAnimation(animation);
    			
    			
                }
 
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
 
            public void onStopTrackingTouch(SeekBar seekBar) {
   /*         	ObjectAnimator animX = ObjectAnimator.ofFloat(productImageview, "scaleX", scaleFactor);
    			ObjectAnimator animY = ObjectAnimator.ofFloat(productImageview, "scaleY", scaleFactor);
    			AnimatorSet animSetXY = new AnimatorSet();
    			animSetXY.playTogether(animX, animY);
    			animSetXY.start(); */
            }
        });
		
		 final Button button = (Button) findViewById(R.id.flipCameraBtn);
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 flipCamera();
             }
         });
/*		
		productImageview.setOnDragListener(new myDragEventListener());
*/
		mCamera = getCameraInstance();
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		mPreviewFrame = (FrameLayout) findViewById(R.id.surface_camera);

		ViewTreeObserver viewTreeObserver = mPreviewFrame.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
		  viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			  
		    @Override
		    public void onGlobalLayout() {
		    	mPreviewFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				int w = mPreviewFrame.getWidth();
				int h = mPreviewFrame.getHeight();
				int newHeight = (int) (w/0.75);
				CameraActivity.setWidthHeight(mPreviewFrame, w, newHeight);
		    }
		  });
		}
	}
	

	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
	    
	    int w = mPreviewFrame.getWidth();
		mPreviewFrame.addView(mPreview);
		mCamera.setParameters(this.configureCamera());
	}
	
    @Override
    protected void onPause() {
        super.onPause();
       // releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
	/**
	 * Call At init when resizing the camera surface
	 * */
	public static void setWidthHeight(FrameLayout v, int width, int height){
		android.widget.RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
 
	    v.setLayoutParams(lp);
	}
	


	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if (imageData != null) {
				mCamera.stopPreview();
				StoreByteImage(mContext, imageData, 90);
			}
		}
	};
	
	  private int findFrontFacingCamera() {
		    int cameraId = -1;
		    // Search for the front facing camera
		    int numberOfCameras = Camera.getNumberOfCameras();
		    for (int i = 0; i < numberOfCameras; i++) {
		      CameraInfo info = new CameraInfo();
		      Camera.getCameraInfo(i, info);
		      if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
		        Log.d(TAG, "Camera found");
		        cameraId = i;
		        break;
		      }
		    }
		    return cameraId;
		 }
	  
 
	  
	  private void flipCamera() {

	        if (mCamera != null)
	        {
	            System.out.println("flipcamera");
	            mCamera.stopPreview();
	            mCamera.release();
	            mCamera = null;
	            isFrontCamera = false;

	        }
	        mCamera = Camera.open(findFrontFacingCamera());
	        if (mCamera != null) {
	            try {
	   	
	            	mCamera.setPreviewDisplay(mPreview.getHolder());
	            	Parameters params = this.configureCamera();
	            	params.setRotation(270);
	            	mCamera.setParameters(params); 
	            	mCamera.startPreview();
	            	isFrontCamera = true;
	            	 
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }

	   }
	  
	  public void flipImage(){
		  
		  BitmapDrawable drawable =(BitmapDrawable) productImageview.getDrawable();
		  Bitmap productBitmap = drawable.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
		  Matrix matrix = new Matrix();
		  matrix.preScale(-1.0f, 1.0f);
		  productImageview.setImageBitmap(Bitmap.createBitmap(productBitmap, 0, 0, productBitmap.getWidth(), productBitmap.getHeight(), matrix, true));
		  productBitmap.recycle();
	  }

	@Override
	public boolean onTouchEvent(MotionEvent ev) 
	{
		Log.i("SV", "event");
		return true;
	}
	
	
	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		private Matrix matrix = new Matrix();
		private float scale = 1f;

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scale *= detector.getScaleFactor();
			scale = Math.max(0.1f, Math.min(scale, 5.0f));
			/*
			 * matrix.setScale(scale, scale); ObjectAnimator anim =
			 * ObjectAnimator.ofFloat(productImageview, "height", 0f, 1f);
			 * anim.setDuration(1000); anim.start();
			 */
			ScaleAnimation animation = new ScaleAnimation(scale, scale, scale,
					scale, Animation.RELATIVE_TO_SELF, 1f,
					Animation.RELATIVE_TO_SELF, 1f);
			animation.setFillAfter(true);
			productImageview.startAnimation(animation);
			Log.i("SV", "Scale" + scale);
			/*
			 * productImageview.setImageMatrix(matrix);
			 * productImageview.invalidate(); Log.i("SV", "Scale"+scale);
			 */
			return true;
		}
	}

	/**
	 * 
	 * 	
	 **/
	
	public boolean StoreByteImage(Context mContext, byte[] imageData,
			int quality) {
	
	
		BitmapFactory.Options options = new BitmapFactory.Options();
		// avoid outofmemoryexception
		options.inTempStorage = new byte[16 * 1024];
		//TODO: check this out
		Bitmap resultBitmap = BitmapFactory.decodeByteArray(imageData, 0,
				imageData.length, options);
	 
		resultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, true);

		 Log.d(TAG, "ResultBitmap w:" + resultBitmap.getWidth()+" h:" +resultBitmap.getHeight());	
		float density = getResources().getDisplayMetrics().density;
		FileOutputStream fileOutputStream = null;

		try {

			Canvas resultImageCanvas = new Canvas(resultBitmap);
			 Log.d(TAG, "Canvas w:" + resultImageCanvas.getWidth()+" h:" +resultImageCanvas.getHeight());	
		 
		     
		     if (isFrontCamera) {
		       flipImage();
		     }
					 
			BitmapDrawable drawable =(BitmapDrawable) productImageview.getDrawable();
			Bitmap clannerToMergeBM = drawable.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
			
			int drawableWidth= drawable.getBounds().width();
			int drawableHeight = drawable.getBounds().height();
	
			 Log.d(TAG, "Image w:" + clannerToMergeBM.getWidth()+" h:" +clannerToMergeBM.getHeight());	
			 Log.d(TAG, "Drawable w:" + drawableWidth +" h:" + drawableHeight);	
			 Log.d(TAG, "Preview left:" + mPreview.getLeft()+" width:" +mPreview.getWidth() +"height:" +mPreview.getHeight());	
			 Log.d(TAG, "Product left:"+productImageview.getLeft()+" product w ="+productImageview.getWidth() +"product top " + productImageview.getTop()+"Product h :"+productImageview.getHeight());
			 Log.d(TAG, "Product height:"+ productImageview.getLayoutParams().height);
			float clannerPosX = 0;
			float clannerPosY = 0;
			float newWidth =0;
			float newHeight = 0;
			  
			 // Future versions 
 			clannerPosX = transformValue(productImageview.getLeft(),mPreview.getWidth(),resultBitmap.getWidth());
			clannerPosY = transformValue(productImageview.getTop(),mPreview.getHeight(),resultBitmap.getHeight());
			Point productCorner = productImageOriginCorner(productImageview.getWidth(), productImageview.getHeight(), productImageview.getWidth()*scaleFactor, productImageview.getHeight()*scaleFactor);

		
		
			newWidth =	transformValue(drawableWidth, mPreview.getWidth(),resultBitmap.getWidth());
			newHeight = transformValue(drawableHeight,mPreview.getHeight(),resultBitmap.getHeight());
			 
			newWidth*=scaleFactor;
			newHeight*=scaleFactor;
			
			Matrix m = productImageview.getImageMatrix();
			float []values = {0,0,0,0,0,0,0,0,0};
			m.getValues(values);
			float yTranslation = values[5];
			
			if (yTranslation != 0 ){
				float scaleY = values[4];
			 
				if (scaleFactor < 1){
					
					clannerPosY =transformValue(yTranslation+productImageview.getTop()+productCorner.y,mPreview.getHeight(),resultBitmap.getHeight());
				}else
				clannerPosY =transformValue(yTranslation+productImageview.getTop(),mPreview.getHeight(),resultBitmap.getHeight());
			//transformValue(yTranslation+productImageview.getTop(),productImageview.getTop(),productImageview.getHeight(), mPreview.getTop(), mPreview.getHeight());
					
			}
			if (scaleFactor < 1)
			{
				clannerPosX+= transformValue(productCorner.x, mPreview.getWidth(),resultBitmap.getWidth());
				clannerPosY +=transformValue(productCorner.y,mPreview.getHeight(),resultBitmap.getHeight());
			}
		
			
			Log.i(TAG, " A: X pos:"+clannerPosX+"  Y "+ clannerPosY+" heigth " +newHeight+" width"+ newWidth );
			Bitmap bm =getResizedBitmap(clannerToMergeBM, (int)newHeight,(int) newWidth);
			clannerToMergeBM.recycle();
	
			
			resultImageCanvas.drawBitmap(bm, clannerPosX,clannerPosY, null);
			showPreview(resultBitmap);
			
			pictureFile = getAlbumStorageDir("ecatalog");

		   File picture = new File(pictureFile.getAbsolutePath() 
					+"/"+ System.currentTimeMillis() + ".jpg");
			fileOutputStream = new FileOutputStream(picture);

			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);

			resultBitmap.compress(CompressFormat.JPEG, 90, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			showAlertViewError(R.string.camera_error);

		} catch (IOException e) {

			e.printStackTrace();
			showAlertViewError(R.string.camera_error);
		}

		saveMediaEntry(pictureFile.getAbsolutePath());

		return true;
	}
	
	public Point productImageOriginCorner(float canvasW, float canvasH, float productW, float productH){
		float x = (canvasW*0.5f)-(productW*0.5f) ;
		float y = (canvasH*0.5f)-(productH*0.5f) ;
		Point p=new Point((int)x,(int)y);
		return p;
	}
	
	public void showCameraViews(View view) {
		
		cameraImageLayout.setVisibility(View.VISIBLE);
	    mToolbarLayout.setVisibility(View.VISIBLE);
	    findViewById(R.id.resultImageLayout).setVisibility(View.GONE);
		findViewById(R.id.cameraProgressBar).setVisibility(View.GONE);
		findViewById(R.id.galleryProgressBar).setVisibility(View.GONE);

	}

	public void showPreview (Bitmap bitmap){
		
		// Show image to user
		cameraImageLayout.setVisibility(View.GONE);
	    mToolbarLayout.setVisibility(View.GONE);
	    findViewById(R.id.resultImageLayout).setVisibility(View.VISIBLE);
		findViewById(R.id.cameraProgressBar).setVisibility(View.INVISIBLE);
		findViewById(R.id.galleryProgressBar).setVisibility(View.INVISIBLE);
		resultImageView.setVisibility(View.VISIBLE);
		resultImageView.setImageBitmap(bitmap);
		
	}

	public File getAlbumStorageDir(String albumName) {
		 	
	
		String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+"/"+albumName;
			File file = new File(extStorageDirectory);
	        if (!file.mkdirs()) {
	            Log.e(TAG, "Directory not created");
	        }
	        return file;
	}
	
	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
	    int width = bm.getWidth();
	    int height = bm.getHeight();
	    float scaleWidth =0;
	    float scaleHeight =0;
	    if (newWidth < width){  //  scale down
	     scaleWidth = ((float) newWidth) / width;
	      scaleHeight = ((float) newHeight) / height;
	    }else
	    {	
	        scaleWidth = ((float) width ) /newWidth ;
		    scaleHeight = ((float)height ) / newHeight;
	    	
	    }
	    // CREATE A MATRIX FOR THE MANIPULATION
	    Matrix m = productImageview.getImageMatrix();
	    m.postScale(scaleWidth, scaleHeight);  // this is important to keep track of the transformations when imageView scaling
	    // "RECREATE" THE NEW BITMAP
	    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width,height, m,false);
	 
	    return resizedBitmap;
	}
	float transformValue (float x,float  fromValue,float toValue)
	{
		return (x/fromValue) * toValue;
	}
	
	//  [(a,b)-(c,d)] to [(e,f)-(g,h)] -->  x' = e + (x - a) * (g - e) / (c - a);
	float transformValue (float x,float  A,float C, float E, float G){
		 Log.i(TAG, " Transformed Value: A="+ A +"  C= "+ C+" E= " +E+" G="+ G +" x="+x);
		// return x *( (G-E)/(C-A));
	   //
		 return E + ((x-A) * (G-E)/(C-A));
	    
	}
	/**
	 * Copies the image to the device's gallery
	 * 
	 * @param imagePath Route to store the image
	 *   
	 * @return The path on the where the image has been created
	 */
	private	Uri saveMediaEntry(String imagePath) {

		File imageFile = new File(imagePath);

		ContentValues v = new ContentValues();
		v.put(Images.Media.TITLE, "ecatalog" + imageFile.getName());
		v.put(Images.Media.DISPLAY_NAME, "ecatalog" + imageFile.getName());
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

	/** A safe way to get an instance of the Camera object. */
	public Camera getCameraInstance() {
		Camera c = null;
		try {

			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {

			showAlertViewError(R.string.camera_error);
			e.printStackTrace();
		}
		return c; // returns null if camera is unavailable
	}

	public void showAlertViewError(int message) {

		Builder builder = new AlertDialog.Builder(CameraActivity.this);
		builder.setMessage(getString(message));
		builder.setPositiveButton(getString(R.string.yes),
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

	}

	/**
	 * Based on Android documentation:
	 * 
	 * A basic Camera preview class
	 */
	public class CameraPreview extends SurfaceView implements
			SurfaceHolder.Callback {
		private SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = getHolder();
			mHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				
			} catch (IOException e) {
				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i(TAG, "surfaceDestroyed");
			mCamera.stopPreview();
			mPreviewRunning = false;
			mCamera.release();
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.
			Log.i(TAG, "Surface changed");
			if (mHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				Log.d(TAG,
						"tried to stop a non-existent preview:"
								+ e.getMessage());
			}

			// set preview size and make any resize, rotate or
			// reformatting changes here
			
			Camera.Parameters params = mCamera.getParameters();
			params.set("orientation", "portrait");
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			if (params.getMaxNumMeteringAreas() > 0) { // check that metering
														// areas are supported
				List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();

				Rect areaRect1 = new Rect(-100, -100, 100, 100); // specify an
																	// area in
																	// center of
																	// image
				meteringAreas.add(new Camera.Area(areaRect1, 600)); // set
																	// weight to
																	// 60%
				Rect areaRect2 = new Rect(800, -1000, 1000, -800); // specify an
																	// area in
																	// upper
																	// right of
																	// image
				meteringAreas.add(new Camera.Area(areaRect2, 400)); // set
																	// weight to
																	// 40%
				params.setMeteringAreas(meteringAreas);
			}
		 
			mCamera.setParameters(params);
			// start preview with new settings
			try {

				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();

			} catch (Exception e) {
				Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			}
		}
	}
	private Camera.Parameters configureCamera() {
		
		Camera.Parameters params = mCamera.getParameters();
	 
		params.setPictureSize(640,480);
		mCamera.setDisplayOrientation(90);
		params.setRotation(90);
 
		return params;
	}
	/**
	 * Aspect Ratio adjustment for the preview surface
	 * */
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;

		if (sizes == null)
			return null;

		Size optimalSize = null;

		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Find size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	protected class myDragEventListener implements OnDragListener {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			   switch(event.getAction())                   
		         {
		           
			// TODO Auto-generated method stub
		  case DragEvent.ACTION_DRAG_STARTED:
              layoutParams = (RelativeLayout.LayoutParams) 
              v.getLayoutParams();
              Log.d(TAG, "Action is DragEvent.ACTION_DRAG_STARTED");
              // Do nothing
              break;
           case DragEvent.ACTION_DRAG_ENTERED:
              Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
              int x_cord = (int) event.getX();
              int y_cord = (int) event.getY();  
              break;
           case DragEvent.ACTION_DRAG_EXITED :
              Log.d(TAG, "Action is DragEvent.ACTION_DRAG_EXITED");
              x_cord = (int) event.getX();
              y_cord = (int) event.getY();
              layoutParams.leftMargin = x_cord;
              layoutParams.topMargin = y_cord;
              v.setLayoutParams(layoutParams);
              break;
           case DragEvent.ACTION_DRAG_LOCATION  :
              Log.d(TAG, "Action is DragEvent.ACTION_DRAG_LOCATION");
              x_cord = (int) event.getX();
              y_cord = (int) event.getY();
              break;
           case DragEvent.ACTION_DRAG_ENDED   :
              Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENDED");
              // Do nothing
              break;
           case DragEvent.ACTION_DROP:
              Log.d(TAG, "ACTION_DROP event");
              // Do nothing
              break;
           default: break;
           }
           return true;

		};
	}//

	
}
