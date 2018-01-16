package com.wxtytdwg.tool;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.view.WindowManager.*;
import android.widget.*;
import java.nio.*;

import java.lang.Process;
import java.io.*;
import android.hardware.input.*;

public class JumpService extends Service
{
	WindowManager mWinMng;
	LayoutParams param ;
	ImageView imv;
	public static ImageReader imr;
	public static float delta_rate=42;//x0.01
	public static float jumprate=1.68f;
	public static int delay=2500;
	boolean flag;
	
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		mWinMng = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		addView();
	}
	
	public void addView()
	{
		if(imv == null)
		{
			imv = new ImageView(this);
			imv.setImageResource(R.drawable.ic_launcher);
			
			param=new LayoutParams();
			param.type =2010;//让悬浮窗显示在最上层
			param.format = PixelFormat.RGBA_8888;
			param.flags =WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
			// mParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
			param.gravity=Gravity.TOP;
			param.width =LayoutParams.WRAP_CONTENT;
			param.height =LayoutParams.WRAP_CONTENT;
			param.x=300;
			param.y=0;
			imv.setOnClickListener(new ImageView.OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						flag=!flag;
						if(flag)new Thread(screenseer).start();
					}
				});
			mWinMng.addView(imv, param);
		}
	}

	public void removeView()
	{
		if(imv != null)
		{
			mWinMng.removeView(imv);
			imv = null;
			
		}
	}
	Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			imv.setImageBitmap((Bitmap)msg.obj);
		}
	};
	private void execShellCmd(String cmd) {

		try {
			// 申请获取root权限，这一步很重要，不然会没有作用
			Process process = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = process.getOutputStream();
			outputStream.write(cmd.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void tap(float x,float y,int time){//用不了
		Instrumentation mInst = new Instrumentation();
		mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
												 SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
												 x, y, 0));
		mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
												 SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
												 x, y, 0));
	}
	public Bitmap getScreen(){
		Image image = imr.acquireLatestImage();
		int width = image.getWidth();
		int height = image.getHeight();
		final Image.Plane[] planes = image.getPlanes();
		final ByteBuffer buffer = planes[0].getBuffer();
		int pixelStride = planes[0].getPixelStride();
		int rowStride = planes[0].getRowStride();
		int rowPadding = rowStride - pixelStride * width;
		Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height,
											Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(buffer);
		bitmap = bigzd(Bitmap.createBitmap(bitmap, 0, (int)(640*(MainActivity.fblx/1080f)),width, height-(int)(640*(MainActivity.fblx/1080f))),1080,1280);
		image.close();
		return bitmap;
	}
	public static Bitmap bigzd(Bitmap bitmap,float na,float nb) {
		Matrix matrix = new Matrix();
		matrix.postScale(na/bitmap.getWidth(),nb/bitmap.getHeight()); 
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		return resizeBmp;
	}
	public double getrate(double dst){
		return jumprate;
	}
	Runnable screenseer=new Runnable(){
		@Override
		public void run()
		{
			while(flag){
				Sobel sob=new Sobel();
				sob.detection(getScreen());
				double dst=sob.getdistence();
				int time=(int)(dst*(getrate(dst)-dst*(delta_rate/100000d)));
				execShellCmd("input swipe 540 960 550 970 "+time);
				//tap(540,960,time);
				handler.obtainMessage(1,bigzd(sob.getBitmap(),360,600)).sendToTarget();
				try{
					Thread.sleep(delay+time);
				}catch (InterruptedException e){}
			}
		}
	};
}
