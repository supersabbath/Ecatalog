package com.canonfer.clipboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class DBHelper extends SQLiteOpenHelper {

	  public static final String DATABASE_NAME = "MyDBName.db";
	  public static final String IMAGES_TABLE_NAME = "images";
	  public static final String IMAGES_COLUMN_ID = "id";
	  public static final String IMAGES_COLUMN_URL = "url";
	  private static final String KEY_IMAGE = "image";
	  
	  
	  public final String LogTag="Ecatlog";
	   private HashMap hp;

	   public DBHelper(Context context)
	   {
	      super(context, "/storage/sdcard0/ecatalog/ecatalog_test.db" , null, 1);
	   }

	   @Override
	   public void onCreate(SQLiteDatabase db) {
	 
	      db.execSQL(
	      "CREATE TABLE "+ IMAGES_TABLE_NAME+  
	      "("+IMAGES_COLUMN_ID+" integer primary key,"+ IMAGES_COLUMN_URL+ " TEXT,"+KEY_IMAGE+" BLOB)"
	      );
	   }

	   
	   @Override
	   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	      // TODO Auto-generated method stub
	      db.execSQL("DROP TABLE IF EXISTS "+ IMAGES_TABLE_NAME);
	      onCreate(db);
	   }

	   
	   public boolean insertImage  (StoredImage imageStorage)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();

	      contentValues.put(IMAGES_COLUMN_URL,  imageStorage._url);
	      contentValues.put(KEY_IMAGE, imageStorage._image);
	      db.insert(IMAGES_TABLE_NAME, null, contentValues);
	      db.close(); // Closing database connection
	      return true;
	   }
	   
	   public StoredImage getStoredImage(int id){
		      SQLiteDatabase db = this.getReadableDatabase();
		      Cursor res =  db.rawQuery( "select * from "+IMAGES_TABLE_NAME+" where id="+id+"", null );
		      if (res != null)
		    	  res.moveToFirst();
		      
		      StoredImage imgSt = new StoredImage (Integer.parseInt(res.getString(0)),
		    		  res.getString(1),
		    		  res.getBlob(2)
		    		  );
		      return imgSt;
		   }
	   
		   public int numberOfRows(){
		      SQLiteDatabase db = this.getReadableDatabase();
		      int numRows = (int) DatabaseUtils.queryNumEntries(db, IMAGES_TABLE_NAME);
		      return numRows;
		   }
		   public boolean updateStoredImage (StoredImage imgSt)
		   {
			  Log.i(LogTag, "Updating image url " + imgSt.getURL());
		      SQLiteDatabase db = this.getWritableDatabase();
		      ContentValues contentValues = new ContentValues();
		      contentValues.put("url", imgSt._url);
		      contentValues.put(KEY_IMAGE, imgSt._image);
		      
		      db.update(IMAGES_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(imgSt._id) } );
		      return true;
		   }

		   public Integer deleteItem(int itemId)
		   {
		      SQLiteDatabase db = this.getWritableDatabase();
		      return db.delete(IMAGES_TABLE_NAME, 
		      "id = ? ", 
		      new String[] { Integer.toString(itemId) });
		   }
		   
		   public ArrayList <StoredImage> getAllStoredImages()
		   {
		      ArrayList array_list = new ArrayList();
		      
		      SQLiteDatabase db = this.getReadableDatabase();
		      Cursor res =  db.rawQuery( "select * from images", null );
		      res.moveToFirst();
		      while(res.isAfterLast() == false){
		    	  StoredImage imgSt = new StoredImage (Integer.parseInt(res.getString(0)),
			    		  res.getString(1),
			    		  res.getBlob(2)
			    		  );  
		    	  
		      array_list.add(imgSt);
		      res.moveToNext();
		      }
		   return array_list;
		   }
}
