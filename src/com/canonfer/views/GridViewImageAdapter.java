package com.canonfer.views;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.canonfer.clipboard.DBHelper;
import com.canonfer.clipboard.StoredImage;
import com.canonfer.ecatalog.R;
import com.canonfer.ecatalog.activities.GridViewActivity;
import com.canonfer.ecatalog.imageProcessing.DownloadImageTask;

	/**
	 * 
	 *  GridViewImageAdapter Adapter for ExpandableHeightGridView + GridViewActivity
	 * 
	 * */
	public class GridViewImageAdapter extends BaseAdapter {
	
		private Context mContext;
		/**
		 * Icons contais the replica of the objects present in the DDBB
		 */
		private ArrayList <LauncherIcon> icons;
		private DBHelper mddBB;
		public GridViewImageAdapter(GridViewActivity c, DBHelper dBase) {
			mContext = c;
			icons = c.getIcons();
			mddBB = dBase;
		}
/**
 * number of icons on the adapter
 */
		@Override
		public int getCount() 
		{
			return icons.size();
		}
/**
 * returns the Launcher icon for a give position
 */
		@Override
		public LauncherIcon getItem(int position) {
			return icons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
/**
 * convinient method to remove an image from the DDBB storage and also from the icons.
 * @param launcherIcon
 */
		public void removeItem(LauncherIcon launcherIcon)
		{
			//icons.remove(position);
			
			int i =icons.indexOf(launcherIcon);
			icons.remove(i);
			GridViewActivity activity=(GridViewActivity)mContext;
			this.notifyDataSetChanged();
			mddBB.deleteItem(launcherIcon.imgId);
		}
		/**
		 * Public Class that encapsulates the grid's elements
		 * see R.layout.dasboard_icon 
		 * @author fernandocanon
		 *
		 */
		public class ViewHolder {
			public ImageView icon;
			public TextView text;
			public CustomImageButton deleteBtn;
		
		}
	

		/**
		 *  Create a new ImageView for each item referenced by the Adapter.
		 *  Also checks the value in icon.remote to performe asynchronous download
		 *  @see DownloadImageTask
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			int imageID =icons.get(position).imgId;
			
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
          
				v = vi.inflate(R.layout.dasboard_icon, null);
				
				holder = new ViewHolder();
				holder.text = (TextView) v.findViewById(R.id.dashboard_icon_text);
				holder.icon = (ImageView) v.findViewById(R.id.dashboard_icon_img);
				holder.deleteBtn = (CustomImageButton)v.findViewById(R.id.editButton);
				holder.deleteBtn.referenceID=imageID;
				holder.deleteBtn.launcherIconInAdapter= icons.get(position);
				v.setTag(holder);
				
			} else {
				holder = (ViewHolder) v.getTag();
			}
			
			if( icons.get(position).remote){ 	// download according to image ID
			
				new DownloadImageTask(holder.icon, mddBB).execute(Integer.toString(imageID));
				
			}else{ // Image is in database
				StoredImage imgStored = mddBB.getStoredImage(icons.get(position).imgId);
				//convert byte to bitmap take from contact class
				byte[] outImage=imgStored.getImage();
				ByteArrayInputStream imageStream = new ByteArrayInputStream(outImage);
				Bitmap theImage = BitmapFactory.decodeStream(imageStream);
				
				ImageView imgView= (ImageView)holder.icon;
				imgView.setImageBitmap(theImage);
				imgView.invalidate();
			}
		    holder.text.setText(String.valueOf(imageID)); 
			return v;
		}

	} // end ImageAdapter
