package com.canonfer.ecatalog.activities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
 * This activity manages the camera , the preview of the picture and the
 * required images processing
 */
// @SuppressLint("NewApi")
public class CameraActivity extends Activity {

	private static final String TAG = "CameraActivity";
	/**
	 * mCamera camera object
	 */
	public Camera mCamera;
	/**
	 * mPreviewRunning controls the changes on the surface
	 **/
	private boolean mPreviewRunning = false;
	/**
	 * isFrontCamera indicates which camera is in used. Back camera / front
	 * Camera
	 **/
	public boolean isFrontCamera = false;
	/**
	 * mContext context for CameraActivity
	 */
	private Context mContext = this;
	private android.widget.RelativeLayout.LayoutParams layoutParams;
	/**
	 * Camera and Product image Layout
	 */
	private RelativeLayout cameraImageLayout;
	/**
	 * Camera surface holder
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
	/*
	 * Camera's button for taking the picture
	 */
	private ImageButton takePictureButton;

	private File pictureFile = null;
	/**
	 * mToolbarLayout bar displayed under the photo preview
	 */
	private RelativeLayout mToolbarLayout;
	/**
	 * mSpinner circular progress view displayed while taking the photo.
	 */
	private ProgressBar mSpinner;
	/**
	 * mSizerBar controls the size of the image (product) displayed over the
	 * camera
	 */
	private SeekBar mSizerBar;

	/*
	 * scaleFactor stores the value that will scale the images according to the
	 * mSizerBar
	 */
	float scaleFactor;

	public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		scaleFactor = 1;

		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.camera_activity);

		mToolbarLayout = (RelativeLayout) findViewById(R.id.toolbar);
		cameraImageLayout = (RelativeLayout) findViewById(R.id.cameraImageLayout);
		takePictureButton = (ImageButton) findViewById(R.id.takePictureButton);
		productImageview = (ImageView) findViewById(R.id.productImageView);
		resultImageView = (ImageView) findViewById(R.id.resultImageView);
		configureTakePictureButton();
		putIntentExtraInProductView();
		configureSeekBar();
		configureFlipCameraButton();
		this.startCameraAndViews();

	}

	
	private void startCameraAndViews() {

		if (mCamera == null) {
			mCamera = getCameraInstance();
			// Create our Preview view and set it as the content of our
			// activity.
			mPreview = new CameraPreview(this, mCamera);
			mPreviewFrame = (FrameLayout) findViewById(R.id.surface_camera);
			mPreviewFrame.addView(mPreview);
			addViewTreeObserverToCameraSurface();
			
		}
	}
	/**
	 * Method for configuring the camera preview to the appropiated size. The
	 * appropiated size is given by the aspect ratio of the final photo.
	 * (3/4=0.75) . To do so, it has to wait until the layouts are rendered.
	 */
	private void addViewTreeObserverToCameraSurface() {
		ViewTreeObserver viewTreeObserver = mPreviewFrame.getViewTreeObserver();
		if (viewTreeObserver.isAlive()) {
			viewTreeObserver
					.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							mPreviewFrame.getViewTreeObserver()
									.removeOnGlobalLayoutListener(this);
							int w = mPreviewFrame.getWidth();
							int h = mPreviewFrame.getHeight();
							int newHeight = (int) (w / 0.75);
							CameraActivity.setWidthHeight(mPreviewFrame, w,
									newHeight);
						}
					});
		}
	}

	/**
	 * This method Setups the onClick listener in the flip button
	 */
	private void configureFlipCameraButton() {
		final Button button = (Button) findViewById(R.id.flipCameraBtn);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flipCamera();
			}
		});
	}

	/**
	 * This method takes the extra value from the previous activity's intent and
	 * render it in productImageView
	 */
	private void putIntentExtraInProductView() {
		if (getIntent().hasExtra("byteArray")) {
			Bitmap b = BitmapFactory.decodeByteArray(getIntent()
					.getByteArrayExtra("byteArray"), 0, getIntent()
					.getByteArrayExtra("byteArray").length);
			productImageview.setImageBitmap(b);
			productImageview.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * This method adds the listener to the camara click button.
	 */
	private void configureTakePictureButton() {
		takePictureButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {// Click to make the photo
				findViewById(R.id.cameraProgressBar)
						.setVisibility(View.VISIBLE);
				findViewById(R.id.galleryProgressBar).setVisibility(
						View.VISIBLE);
				mCamera.takePicture(null, null, mPictureCallback);

			}
		});
	}

	/**
	 * ConfigureSeekBar add a listener to the resource from the xml (R.id.seek1)
	 * file In the listener the value for scaling the product image is computed
	 * as well as the animation performed
	 */
	private void configureSeekBar() {
		mSizerBar = (SeekBar) findViewById(R.id.seek1);
		mSizerBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				scaleFactor = progress;
				scaleFactor /= 100;
				scaleFactor = (float) Math.max(0.2, scaleFactor);
				float w = productImageview.getHeight() * scaleFactor;
				float h = productImageview.getWidth() * scaleFactor;
				ObjectAnimator animX = ObjectAnimator.ofFloat(productImageview,
						"scaleX", scaleFactor);
				ObjectAnimator animY = ObjectAnimator.ofFloat(productImageview,
						"scaleY", scaleFactor);

				AnimatorSet animSetXY = new AnimatorSet();
				animSetXY.playTogether(animX, animY);
				animSetXY.setDuration((long) 0.01);
				animSetXY.start();

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first
		this.startCameraAndViews();
		
		mCamera.setParameters(this.configureCamera());

	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera(); // release the camera immediately on pause event
	}

	/**
	 * this method set the size of the view using the Relative layout parameters
	 * The container of the view must be instace of RelativeLayout
	 * 
	 * @param v	 view to be resize
	 * @param width	 new width value
	 * @param height  new height value
	 * */
	public static void setWidthHeight(FrameLayout v, int width, int height) {
		android.widget.RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				width, height);
		v.setLayoutParams(lp);
	}

	/**
	 * PictureCallback overrides the behavior of the camera when the photo has
	 * been taken.
	 */
	Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] imageData, Camera c) {

			if (imageData != null) {
				mCamera.stopPreview();
				processByteImage(mContext, imageData, 90);
			}
		}
	};

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release(); // release the camera for other applications
			mCamera = null;
		}
	}

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

	/**
	 * Method that flips the camera to the opposite position
	 * 
	 */
	private void flipCamera() {

		if (mCamera != null) {
			System.out.println("flipcamera");
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

		try {
			if (isFrontCamera) {

				this.presentBackCamera();

			} else {
				this.presentFrontCamera();
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * This method presents the back camera
	 * 
	 * @throws IOException
	 *             in case the preview holder fails
	 */
	private void presentBackCamera() throws IOException {

		mCamera = getCameraInstance();
		mCamera.setPreviewDisplay(mPreview.getHolder());
		Parameters params = this.configureCamera();
		mCamera.setParameters(params);
		mCamera.startPreview();
		isFrontCamera = false;

	}

	/**
	 * This method presents the front camera
	 * 
	 * @throws IOException
	 *             in case the preview holder fails
	 */

	private void presentFrontCamera() throws IOException {
		mCamera = Camera.open(findFrontFacingCamera());
		if (mCamera != null) {

			mCamera.setPreviewDisplay(mPreview.getHolder());
			Parameters params = this.configureCamera();
			params.setRotation(270);
			mCamera.setParameters(params);
			mCamera.startPreview();
			isFrontCamera = true;
		}
	}

	/**
	 * This methos flips the bitmap in productImageView inverting the matrix in
	 * x coordinates
	 * */
	public void flipImage() {
		BitmapDrawable drawable = (BitmapDrawable) productImageview
				.getDrawable();
		Bitmap productBitmap = drawable.getBitmap().copy(
				Bitmap.Config.ARGB_8888, true);
		Matrix matrix = new Matrix();
		matrix.preScale(-1.0f, 1.0f);
		productImageview.setImageBitmap(Bitmap.createBitmap(productBitmap, 0,
				0, productBitmap.getWidth(), productBitmap.getHeight(), matrix,
				true));
		productBitmap.recycle();
	}

	/**
	 * This method creates a new bitmap that merges the parametter with the
	 * product image.
	 * 
	 * @param backgroundImage	This image will be merge with the image store in productImageView
	 * @return the merged image amoung backgroundImage and the image in productImageView
	 */
	public Bitmap mergeBitmapWithProductImage(Bitmap backgroundImage) {

		Canvas resultImageCanvas = new Canvas(backgroundImage);
		if (isFrontCamera) {
			flipImage();
		}

		BitmapDrawable drawable = (BitmapDrawable) productImageview
				.getDrawable();
		Bitmap imageToMergeWithBG = drawable.getBitmap().copy(
				Bitmap.Config.ARGB_8888, true);

		int drawableWidth = drawable.getBounds().width();
		int drawableHeight = drawable.getBounds().height();

		/*
		 * Log.d(TAG, "Image w:" + imageToMergeWithBG.getWidth()+" h:"
		 * +imageToMergeWithBG.getHeight()); Log.d(TAG, "Drawable w:" +
		 * drawableWidth +" h:" + drawableHeight); Log.d(TAG, "Preview left:" +
		 * mPreview.getLeft()+" width:" +mPreview.getWidth() +"height:"
		 * +mPreview.getHeight()); Log.d(TAG,
		 * "Product left:"+productImageview.getLeft
		 * ()+" product w ="+productImageview.getWidth() +"product top " +
		 * productImageview
		 * .getTop()+"Product h :"+productImageview.getHeight()); Log.d(TAG,
		 * "Product height:"+ productImageview.getLayoutParams().height);
		 */
		float clannerPosX = 0;
		float clannerPosY = 0;
		float newWidth = 0;
		float newHeight = 0;

		Point productCorner = productImageOriginCorner(
				productImageview.getWidth(), productImageview.getHeight(),
				productImageview.getWidth() * scaleFactor,
				productImageview.getHeight() * scaleFactor);

		newWidth = transformValue(drawableWidth, mPreview.getWidth(),
				backgroundImage.getWidth());
		newHeight = transformValue(drawableHeight, mPreview.getHeight(),
				backgroundImage.getHeight());

		newWidth *= scaleFactor;
		newHeight *= scaleFactor;

		Matrix m = productImageview.getImageMatrix();
		float[] values = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		m.getValues(values);
		
		float yTranslation = values[5];
		Log.d(TAG, "translation:" + yTranslation);
		
		yTranslation *= scaleFactor;
		
		clannerPosY = transformValue(productImageview.getTop() + yTranslation,
				mPreview.getHeight(), backgroundImage.getHeight());
		clannerPosX = transformValue(productImageview.getLeft(),
				mPreview.getWidth(), backgroundImage.getWidth());

		if (scaleFactor < 1) {
			clannerPosX += transformValue(productCorner.x, mPreview.getWidth(),
					backgroundImage.getWidth());
			clannerPosY += transformValue(productCorner.y,
					mPreview.getHeight(), backgroundImage.getHeight());
		}

		Log.i(TAG, " A: X pos:" + clannerPosX + "  Y " + clannerPosY
				+ " heigth " + newHeight + " width" + newWidth);
		
		Bitmap bm = getResizedBitmap(imageToMergeWithBG, (int) newHeight,(int) newWidth);
		imageToMergeWithBG.recycle();

		resultImageCanvas.drawBitmap(bm, clannerPosX, clannerPosY, null);
		
		if (isFrontCamera) {
			flipImage();
		}

		return backgroundImage;
	}

	/**
	 * processByteImage the bytes returned by the camera.
	 * 
	 * @param mContext	 Activity context
	 * @param imageData  Bytes containing a image
	 **/
	public boolean processByteImage(Context mContext, byte[] imageData,
			int quality) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		// avoid outofmemoryexception
		options.inTempStorage = new byte[16 * 1024];
		Bitmap resultBitmap = BitmapFactory.decodeByteArray(imageData, 0,
				imageData.length, options);
		resultBitmap = resultBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Bitmap finalImage = this.mergeBitmapWithProductImage(resultBitmap); // merge
																			// the
																			// bitmaps

		FileOutputStream fileOutputStream = null;
		try {
			pictureFile = getAlbumStorageDir("ecatalog");

			pictureFile = new File(pictureFile.getAbsolutePath() + "/"
					+ System.currentTimeMillis() + ".jpg");
			fileOutputStream = new FileOutputStream(pictureFile);

			BufferedOutputStream bos = new BufferedOutputStream(
					fileOutputStream);

			finalImage.compress(CompressFormat.JPEG, 90, bos);

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
		showPreview(finalImage);
		return true;
	}

	/**
	 * This method computes the distance in X,Y between the top-left corner of a
	 * rectangle inside a container. For the container also the top-left corner
	 * is used. both with the same center
	 * 
	 * @param canvasW  width container rect
	 * @param canvasH	height container rect
	 * @param productW   width of the inner rect
	 * @param productH	Height of the inner rect
	 *            
	 */
	public Point productImageOriginCorner(float canvasW, float canvasH,
			float productW, float productH) {
		float x = (canvasW * 0.5f) - (productW * 0.5f);
		float y = (canvasH * 0.5f) - (productH * 0.5f);
		Point p = new Point((int) x, (int) y);
		return p;
	}

	public void showCameraViews(View view) {

		cameraImageLayout.setVisibility(View.VISIBLE);
		mToolbarLayout.setVisibility(View.VISIBLE);
		mSizerBar.setVisibility(View.VISIBLE);
		findViewById(R.id.resultImageLayout).setVisibility(View.GONE);
		findViewById(R.id.cameraProgressBar).setVisibility(View.GONE);
		findViewById(R.id.galleryProgressBar).setVisibility(View.GONE);
		isFrontCamera = !isFrontCamera;
		this.flipCamera();

	}

	public void showPreview(Bitmap bitmap) {

		// Show image to user
		cameraImageLayout.setVisibility(View.GONE);
		mToolbarLayout.setVisibility(View.GONE);
		mSizerBar.setVisibility(View.GONE);
		findViewById(R.id.resultImageLayout).setVisibility(View.VISIBLE);
		findViewById(R.id.cameraProgressBar).setVisibility(View.INVISIBLE);
		findViewById(R.id.galleryProgressBar).setVisibility(View.INVISIBLE);
		resultImageView.setVisibility(View.VISIBLE);
		resultImageView.setImageBitmap(bitmap);

	}

	public File getAlbumStorageDir(String albumName) {

		String extStorageDirectory = Environment.getExternalStorageDirectory()
				+ "/" + albumName;
		File file = new File(Environment.getExternalStorageDirectory() + "/"
				+ albumName);
		if (!file.mkdirs()) {
			Log.e(TAG, "Directory not created");
		}
		return file;
	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {

		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = 0;
		float scaleHeight = 0;
		if (newWidth < width) { // scale down
			scaleWidth = ((float) newWidth) / width;
			scaleHeight = ((float) newHeight) / height;
		} else {
			scaleWidth = ((float) width) / newWidth;
			scaleHeight = ((float) height) / newHeight;
		}
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix m = productImageview.getImageMatrix();
		m.postScale(scaleWidth, scaleHeight); // this is important to keep track
												// of the transformations when
												// imageView scaling
		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, m,
				false);

		return resizedBitmap;
	}

	/**
	 * TransformValue transform the valuex in a line of length (fromValue) to
	 * his proportional position in another line with length (toValue)
	 * 
	 * @param x  Value to transform           
	 * @param fromValue	Length of the segment where x belongs
	 * @param toValue	Length of the segment of the new value
	 * @return
	 */
	float transformValue(float x, float fromValue, float toValue) {
		return (x / fromValue) * toValue;
	}

	/**
	 * TransformValue transform the a value x, from one rectangle system to
	 * another Like this: [(a,b)-(c,d)] to [(e,f)-(g,h)] --> x' = e + (x - a) *
	 * (g - e) / (c - a);
	 * 
	 * @param x	 value to be transform        
	 * @param A	 top left corner of the outter square
	 * @param C	top right corner of the outter square        
	 * @param E	top left corner of the inner square        
	 * @param G	 top right corner of the inner square
	 *           
	 * @return the correspondent point in the bigger coordinate range.
	 */

	float transformValue(float x, float A, float C, float E, float G) {
		Log.i(TAG, " Transformed Value: A=" + A + "  C= " + C + " E= " + E
				+ " G=" + G + " x=" + x);
		// return x *( (G-E)/(C-A));
		//
		return E + ((x - A) * (G - E) / (C - A));

	}

	/**
	 * Copies the image to the device's gallery
	 * 
	 * @param imagePath Route to store the image
	 * @return The path on the where the image has been created
	 */
	private Uri saveMediaEntry(String imagePath) {

		File imageFile = new File(imagePath);

		ContentValues v = new ContentValues();
		v.put(Images.Media.TITLE, imageFile.getName());
		v.put(Images.Media.DISPLAY_NAME, imageFile.getName());
		v.put(Images.Media.MIME_TYPE, "image/jpeg");

		File f = new File(imagePath);
		File parent = f.getParentFile();
		String path = parent.toString().toLowerCase(Locale.getDefault());
		String name = parent.getName().toLowerCase(Locale.getDefault());
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
	 * Based on Android documentation A basic Camera preview class
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

			mPreviewRunning = false;
			releaseCamera();
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

		params.setPictureSize(640, 480);
		mCamera.setDisplayOrientation(90);
		params.setRotation(90);

		return params;
	}
	/*
	 * Aspect Ratio adjustment for the preview surface
	 * 
	 * private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	 * final double ASPECT_TOLERANCE = 0.05; double targetRatio = (double) w /
	 * h;
	 * 
	 * if (sizes == null) return null;
	 * 
	 * Size optimalSize = null;
	 * 
	 * double minDiff = Double.MAX_VALUE;
	 * 
	 * int targetHeight = h;
	 * 
	 * // Find size for (Size size : sizes) { double ratio = (double) size.width
	 * / size.height; if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
	 * continue; if (Math.abs(size.height - targetHeight) < minDiff) {
	 * optimalSize = size; minDiff = Math.abs(size.height - targetHeight); } }
	 * 
	 * if (optimalSize == null) { minDiff = Double.MAX_VALUE; for (Size size :
	 * sizes) { if (Math.abs(size.height - targetHeight) < minDiff) {
	 * optimalSize = size; minDiff = Math.abs(size.height - targetHeight); } } }
	 * return optimalSize; }
	 */
}
