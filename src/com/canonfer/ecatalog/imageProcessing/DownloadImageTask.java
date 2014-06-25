package com.canonfer.ecatalog.imageProcessing;

import java.io.InputStream;
import com.canonfer.clipboard.DBHelper;
import com.canonfer.clipboard.StoredImage;
import com.canonfer.ecatalog.activities.GridViewActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
	
/**
 * Image downloading taks will be performed in this Class
 * This class performs a background download of the image. The main thread will not block.
 * Once the download is finish the OnPostExecute method will callback the origin thread
 * */
public class  DownloadImageTask extends AsyncTask<String, Void, Bitmap>{
	public final String LogTag="Ecatalog";
	  ImageView bmImage;
	  DBHelper mDB;
	  
	  public DownloadImageTask(ImageView bmImage, DBHelper ddBB) {
	      this.bmImage = bmImage;
	      this.mDB = ddBB;
	  }
/**
 * Download the images using the url stored in the database. Receives the image ID to perform the sql query  
 */
	  protected Bitmap doInBackground(String... imagesIDs) {
	      String idInDataBase = imagesIDs[0];
	      StoredImage imgSt= mDB.getStoredImage(Integer.parseInt(idInDataBase));
	      
	      Bitmap mIcon11 = null;
	      try {
	        InputStream in = new java.net.URL(imgSt.getURL()).openStream();
	        Log.d(LogTag,"Downloading image:" + imgSt.getURL().toString());
	        mIcon11 = BitmapFactory.decodeStream(in);
	        
	        if(mIcon11 != null){
	        	byte[] bytes = GridViewActivity.bitmapToBytes(mIcon11);
	        	imgSt.setImage(bytes);
				mDB.updateStoredImage(imgSt);
	        }
	      } catch (Exception e) {
	          Log.e(LogTag, e.getMessage());
	          e.printStackTrace();
	      }
	      
	      return mIcon11;
	  }
/**
 * This method acts like a callback when task is completed
 */
	  protected void onPostExecute(Bitmap result) {
 	      bmImage.setImageBitmap(result);
	      bmImage.postInvalidate();
	  }
	 
}
