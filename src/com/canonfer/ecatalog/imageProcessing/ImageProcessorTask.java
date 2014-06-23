package com.canonfer.ecatalog.imageProcessing;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;
import org.opencv.gpu.*; 


public class ImageProcessorTask extends AsyncTask<Bitmap, Integer,Bitmap> {

	private static final String  TAG   = "Ecatalog";
	public AsyncResponse delegate=null;


	@Override
	protected Bitmap doInBackground(Bitmap... params) {
		// TODO Auto-generated method stub
		return maskImage(params[0]);
	
	}
	
 
	protected void onProgressUpdate(Integer... progress) {
		// setProgressPercent(progress[0]);
	}

	protected void onPostExecute(Bitmap result) {
 
		delegate.processFinish(result);
		Log.i(TAG, "Bytes :"+result.getByteCount());
		 
	}


	
	public  Bitmap maskImage (Bitmap src){

		Mat imgMatrix = new Mat();
		Utils.bitmapToMat(src, imgMatrix,true);
		//imgMatrix.rows(),imgMatrix.cols(),CvType.CV_8UC1);
		Mat imgTo=new Mat();
		Imgproc.cvtColor(imgMatrix, imgTo,Imgproc.COLOR_RGB2GRAY);
		//	Imgproc.threshold(imgTo, imgTo, 0, 255, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU );
		//	.threshold(gray,0,255,cv2.THRESH_BINARY_INV+cv2.THRESH_OTSU)
		//	Imgproc.blur(imgTo, imgTo, new Size(1,1));
		// Imgproc.Sobel(imgMatrix, imgTo, imgMatrix.depth(), 1,1);//
		Imgproc.Canny(imgTo,imgTo, 50,255);

		Mat se90	=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,3));
		Mat se0	=Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,3));
		List <Mat> arrayMat = new ArrayList<Mat> ();
		arrayMat.add(se90);
		arrayMat.add(se0);
		Mat concated =new Mat();
		Core.hconcat(arrayMat,concated );

		Imgproc.dilate(imgTo, imgTo, concated);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(imgTo, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		Mat mask=new Mat(imgMatrix.size(), CvType.CV_8UC4,new Scalar(0,0,0,255));
		
		Imgproc.drawContours(mask, contours, 0, new Scalar(255,255,255), -1);
	
		Scalar constant =new Scalar(233,132,122,255);
		
		Mat dst=new Mat(imgMatrix.size(), CvType.CV_8UC4,new Scalar(3,132,122,255));
				//new  Mat(imgMatrix.rows(),imgMatrix.cols(),imgMatrix.type());
				//new Mat(imgMatrix.size(), CvType.CV_8UC1, new Scalar(Color.blue(Color.TRANSPARENT),Color.red(Color.TRANSPARENT),Color.green(Color.TRANSPARENT)));
		imgMatrix.copyTo(dst, mask);
				
		Bitmap resultBitmap = Bitmap.createBitmap(dst.cols(),  dst.rows(),Bitmap.Config.ARGB_8888);;
		Utils.matToBitmap(dst, resultBitmap);
		
		return replaceColor(resultBitmap,Color.argb(255, 3, 132, 122),Color.TRANSPARENT); 
	}



	
    public Bitmap replaceColor(Bitmap src,int fromColor, int targetColor) {

    	if(src == null) {
            return null;
        }
     // Source image size 
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        //get pixels
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int x = 0; x < pixels.length; ++x) {
            pixels[x] = (pixels[x] == fromColor) ? targetColor : pixels[x];
        }

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());
        //set pixels
        result.setPixels(pixels, 0, width, 0, 0, width, height);

        return result;
    }

}
/*
 * 	//Imgproc.cvtColor(imgMatrix, imgTo,Imgproc.COLOR_RGB2GRAY);
    	// 1) Apply gaussian blur to remove noise
    	Imgproc.GaussianBlur(imgMatrix, imgTo, new Size(11,11), 0);

    	// 2) AdaptiveThreshold -> classify as either black or white
    	Imgproc.adaptiveThreshold(imgTo, imgTo, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

    	// 3) Invert the image -> so most of the image is black
    	Core.bitwise_not(imgTo, imgTo);

    	// 4) Dilate -> fill the image using the MORPH_DILATE
    	Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
    	Imgproc.dilate(imgTo, imgTo, kernel);
 * */
