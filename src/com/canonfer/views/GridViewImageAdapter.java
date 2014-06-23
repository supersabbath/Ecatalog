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
		private ArrayList <LauncherIcon> icons;
		private DBHelper mddBB;
		public GridViewImageAdapter(GridViewActivity c, DBHelper dBase) {
			mContext = c;
			icons = c.getIcons();
			mddBB = dBase;
		}

		@Override
		public int getCount() 
		{
			return icons.size();
		}

		@Override
		public LauncherIcon getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public class ViewHolder {
			public ImageView icon;
			public TextView text;
		}

		// Create a new ImageView for each item referenced by the Adapter
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
						Context.LAYOUT_INFLATER_SERVICE);
          
				v = vi.inflate(R.layout.dasboard_icon, null);
				holder = new ViewHolder();
	//			holder.text = (TextView) v.findViewById(R.id.dashboard_icon_text);
				holder.icon = (ImageView) v.findViewById(R.id.dashboard_icon_img);

				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}

			if( icons.get(position).remote){ 	// download according to image ID
			
				int imageID =icons.get(position).imgId;
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
		//	holder.text.setText(icons.get(position).text);
			return v;
		}

	} // end ImageAdapter
