package com.wxtytdwg.tool;

import android.app.*;
import android.content.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.graphics.*;

import java.io.*;
import android.widget.*;

public class MainActivity extends Activity 
{
	private MediaProjection mediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mediaProjectionManager;
    private static final int REQUESTRESULT = 0x100;
    SharedPreferences fs;
	private int mWidth;
    private int mHeight;
    private int mScreenDensity;
    private ImageReader mImageReader;
	EditText et,et_rate,et_damping;
	
	public static int fblx,fbly;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fs=getSharedPreferences("infom",Context.MODE_PRIVATE);
		et=(EditText)findViewById(R.id.mainEditText1);
		et.setText(""+2500);
		
		JumpService.jumprate=fs.getFloat("jumprate",1.68f);
		JumpService.delta_rate=fs.getFloat("delta",42);
		et_rate=(EditText)findViewById(R.id.mainEditText_rate);
		et_damping=(EditText)findViewById(R.id.mainEditText_damping);
		et_rate.setText(""+JumpService.jumprate);
		et_damping.setText(""+JumpService.delta_rate);
		try{
			Runtime.getRuntime().exec("su");
		}catch (IOException e){
			Toast.makeText(this,"申请root失败",2000).show();
		}
    }
	public void on1(View v){
		if(et.length()>0)JumpService.delay=Integer.parseInt(et.getText().toString());
		if(et_rate.length()>0)JumpService.jumprate=Float.parseFloat(et_rate.getText().toString());
		if(et_damping.length()>0)JumpService.delta_rate=Float.parseFloat(et_damping.getText().toString());
		initData();
	}
	private void initData() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();
		fblx=mWidth;fbly=mHeight;
        DisplayMetrics outMetric = new DisplayMetrics();
        display.getMetrics(outMetric);
        mScreenDensity = (int) outMetric.density;
        Intent intent = new Intent(mediaProjectionManager.createScreenCaptureIntent());
        startActivityForResult(intent,REQUESTRESULT);
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode==RESULT_OK&&requestCode==REQUESTRESULT)
		{

            mImageReader = ImageReader.newInstance(mWidth,mHeight, PixelFormat.RGBA_8888, 2);
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
            mVirtualDisplay = mediaProjection.createVirtualDisplay("mediaprojection",mWidth,mHeight,
																   mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mImageReader.getSurface(),null,null);
			JumpService.imr=mImageReader;
			Intent intent=new Intent(MainActivity.this,JumpService.class);
			startService(intent);
			//System.exit(0);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
