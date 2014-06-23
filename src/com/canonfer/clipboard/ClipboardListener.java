package com.canonfer.clipboard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.canonfer.ecatalog.R;
import com.canonfer.ecatalog.activities.GridViewActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.content.ContentResolver;
/**
 * This class implements the listener for ClipboardManager It stores the last 10
 * images copied to the clipboard
 * 
 * */
@SuppressLint("NewApi")
public class ClipboardListener implements
		ClipboardManager.OnPrimaryClipChangedListener {

	public final String LogTag = "Ecatalog";
	private DBHelper mDataBase;
	private Context mContext;

	public ClipboardListener(Context context) {

		mContext = context;
		mDataBase = new DBHelper(mContext);
	}

	@Override
	public void onPrimaryClipChanged() {
		// Gets a handle to the clipboard service.

		ClipboardManager clipboard = (ClipboardManager) mContext
				.getSystemService(Context.CLIPBOARD_SERVICE);
		Log.i(LogTag, "new object");
		ClipData cp = clipboard.getPrimaryClip();
		this.processReceivedClipData(cp);
		
	}

	
	
	public void processReceivedClipData(ClipData cp) {
		
		ClipData.Item item = cp.getItemAt(0);
	
		if (this.validateText(item)){ // it is text
			
	/*		Uri pasteUri = item.getUri();
			ContentResolver cr = mContext.getContentResolver();
			String uriMimeType = cr.getType(pasteUri);*/
			//TODO: check type
			byte imageInByte[]=null;
			StoredImage img = new StoredImage(0,item.getText().toString(),imageInByte);
			mDataBase.insertImage(img);
			Log.i(LogTag, "Inserted "+item.getText().toString());
			pushNotification();
			
		}
	}
	
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void pushNotification(){
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(mContext)
		        .setSmallIcon(R.drawable.apple)
		        .setContentTitle("Ecatalog")
		        .setContentText("New Image added!")
		        .setAutoCancel(true);
	
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(mContext, GridViewActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(GridViewActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(1, mBuilder.build());
		
	}
	public boolean validateText(ClipData.Item item) {

		CharSequence pasteData = item.getText();
		// If the string contains data, then the paste operation is done
		if (pasteData != null) {
		
				String urlString =pasteData.toString();
				if (matchURL(urlString)){
					ImageValidator validator = new ImageValidator();
					return validator.validate(urlString);
					
				}else{
					return false;
				}

			// The clipboard does not contain text. If it contains a URI,
			// attempts to get data from it
		} else {
			Uri pasteUri = item.getUri();

			// If the URI contains something, try to get text from it
			if (pasteUri != null) {
             
				String urlString =pasteUri.toString();
				if (matchURL(urlString)){
					ImageValidator validator = new ImageValidator();
					return validator.validate(urlString);
					
				}else{
					return false;
				}
				
			} else {

				// Something is wrong. The MIME type was plain text, but the
				// clipboard does not contain either
				// text or a Uri. Report an error.
				Log.e(LogTag,"Clipboard contains an invalid data type");
				return false;
			}

		}
	}
	
	private boolean matchURL (String  linkUrl) {
		
	return	android.util.Patterns.WEB_URL.matcher(linkUrl).matches();	
	}
	
	public class ImageValidator{
		 
		   private Pattern pattern;
		   private Matcher matcher;
		 
		   private static final String IMAGE_PATTERN = 
		                "([^\\s]+(\\.(?i)(jpg|png|bmp))$)";
		 
		   public ImageValidator(){
			  pattern = Pattern.compile(IMAGE_PATTERN);
		   }
		 
		   /**
		   * Validate image with regular expression
		   * @param image image for validation
		   * @return true valid image, false invalid image
		   */
		   public boolean validate(final String image){
		 
			  matcher = pattern.matcher(image);
			  return matcher.matches();
		 
		   }
		}
}



