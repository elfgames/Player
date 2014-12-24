/*
 * This file is part of EasyRPG Player
 *
 * Copyright (c) 2013 EasyRPG Project. All rights reserved.
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *  
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.easyrpg.player;

import java.io.File;

import org.libsdl.app.SDLActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;

/**
 * EasyRPG Player for Android (inheriting from SDLActivity)
 */

public class EasyRpgPlayerActivity extends SDLActivity {
	private ImageView aView, bView, cView, dView;
	private ImageView[] numView;
	private boolean uiVisible = true;
	private boolean useNumpad = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
	    try {
 			if (Build.VERSION.SDK_INT >= 11) {
 				// Api 11: FLAG_HARDWARE_ACCELERATED
 				getWindow().setFlags(0x01000000, 0x01000000);
 			}
 		} catch (Exception e) {}
	    
		mLayout = (RelativeLayout)findViewById(R.id.main_layout);
	    mLayout.addView(mSurface);
	    
	    drawButtons();
	    drawCross();
	    drawNumbers();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Alt-Test for working around ugly Xperia Play button mapping
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && (!event.isAltPressed()))
	    {
	        showEndGameDialog();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.player_menu, menu);
	    Log.v("Player", "onCreateOption");
	    return true;
	}
	
	private void toggleStandardUI(boolean value) {
		if (value) {
			mLayout.addView(aView);
			mLayout.addView(bView);
			mLayout.addView(dView);
			mLayout.addView(cView);
		} else {
			mLayout.removeView(aView);
			mLayout.removeView(bView);
			mLayout.removeView(dView);
			mLayout.removeView(cView);
		}
	}

	private void toggleNumpadUI(boolean value) {
		if (value) {
			for (int i = 0; i < 9; i++)
				mLayout.addView(numView[i]);
		} else {
			for (int i = 0; i < 9; i++)
				mLayout.removeView(numView[i]);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.toggle_fps:
	            toggleFps();
	            return true;
	        case R.id.toggle_ui:
	        	if (uiVisible) {
	            toggleStandardUI(false);
	        	} else {
	            toggleStandardUI(true);
	        	}
	        	uiVisible = !uiVisible;
	            return true;
	        case R.id.end_game:
	        	showEndGameDialog();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void showEndGameDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("EasyRPG Player");
    
		// set dialog message
		alertDialogBuilder
			.setMessage("Do you really want to quit?")
			.setCancelable(false)
			.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					endGame();
				}
			  })
			.setNegativeButton("No",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
    
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
	}
	
	public static native void toggleFps();
	public static native void endGame();
	
	/**
	 * Used by the native code to retrieve the selected game in the browser.
	 * Invoked via JNI.
	 * 
	 * @return Full path to game
	 */
	public String getProjectPath() {
		return getIntent().getStringExtra("project_path");
	}
	
	/**
	 * Used by timidity of SDL_mixer to find the timidity folder for the instruments.
	 * Invoked via JNI.
	 * 
	 * @return Full path to the timidity.cfg
	 */
	public String getTimidityPath() {
		//Log.v("SDL", "getTimidity " + getApplication().getApplicationInfo().dataDir);
		String s = getApplication().getApplicationInfo().dataDir + "/timidity";
		if (new File(s).exists()) {
			return s;
		}
		
		return Environment.getExternalStorageDirectory().getPath() + "/easyrpg/timidity";
	}
	
	/**
	 * Used by the native code to retrieve the RTP directory.
	 * Invoked via JNI.
	 * 
	 * @return Full path to the RTP
	 */
	public String getRtpPath() {
		String str = Environment.getExternalStorageDirectory().getPath() + "/easyrpg/rtp";
		//Log.v("SDL", "getRtpPath " + str);
		return str;
	}
	
	/**
	 * Gets the display height in pixel.
	 * 
	 * @return display height in pixel
	 */
	public int getScreenHeight() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.heightPixels;
        return (int)screenWidthDp;
	}
	
	/**
	 * Gets the display width in pixel.
	 * 
	 * @return display width in pixel
	 */
	public int getScreenWidth() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels;
        return (int)screenWidthDp;
	}
	
	public int setNumpad(boolean value) {
		if (value == useNumpad)
			return 0;
		if (value)
		{
			useNumpad = true;
			runOnUiThread(new Runnable() {
				public void run() {
					toggleStandardUI(false);
					toggleNumpadUI(true);
				}
			});
		} else {
			useNumpad = false;
			runOnUiThread(new Runnable() {
				public void run() {
					toggleStandardUI(true);
					toggleNumpadUI(false);
				}
			});
		}
		return 0;
	}


	/**
	 * Gets Painter used for ui drawing.
	 * 
	 * @return painter
	 */
	private Paint getPainter() {
		Paint uiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		uiPaint.setColor(Color.argb(128, 255, 255, 255));
		uiPaint.setStyle(Style.STROKE);
		uiPaint.setStrokeWidth((float)3.0);
		return uiPaint;
	}
	
	/**
	 * Draws A and B button.
	 */
	private void drawButtons() {
		// Setup color
		Paint circlePaint = getPainter();
		
		// Set size
		int iconSize = getPixels(60); // ~1cm

		// Draw
		Bitmap abBmp = Bitmap.createBitmap(iconSize + 10, iconSize + 10, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(abBmp);
		c.drawCircle(iconSize / 2, iconSize / 2, iconSize / 2 - 5, circlePaint);
		
		// Add to screen layout
		aView = new ImageView(this);
		aView.setImageBitmap(abBmp);
		bView = new ImageView(this);
		bView.setImageBitmap(abBmp);
		dView = new ImageView(this);
		dView.setImageBitmap(abBmp);
		setLayoutPositionRight(aView, 0.13, 0.7);
		setLayoutPositionRight(bView, 0.03, 0.6);
		setLayoutPositionRight(dView, 0.03, 0.8);
		mLayout.addView(aView);
		mLayout.addView(bView);
		mLayout.addView(dView);
	}

	/**
	 * Draws numbers on screen
	 */
	private void drawNumbers() {
		numView = new ImageView[9];
	  Resources resources = getResources();
	  float scale = resources.getDisplayMetrics().density;
		// Setup color
		Paint numberPaint = getPainter();

		// Set size
		int iconSize = getPixels(100); // ~2.5cm
		int iconSize_33 = (int)(iconSize * 0.33);

		for (int i = 0; i < 9; i++)
		{
			String gText = Integer.toString(i + 1);
			Bitmap cBmp = Bitmap.createBitmap(iconSize + 10, iconSize + 10, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(cBmp);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			// text color - #3D3D3D
			paint.setColor(Color.rgb(0, 0, 0));
			// text size in pixels
			paint.setTextSize((int) (30 * scale));
			// text shadow
			paint.setShadowLayer(1f, 1f, 1f, Color.WHITE);

			// draw text to the Canvas center
			Rect bounds = new Rect();
			paint.getTextBounds(gText, 0, gText.length(), bounds);
			//int x = (cBmp.getWidth()) / 2;
			int x = (cBmp.getWidth() + bounds.width()) / 2;
			int y = (cBmp.getHeight() + bounds.height()) / 2;

			c.drawText(gText, x, y, paint);

			ImageView v = new ImageView(this);
			v.setImageBitmap(cBmp);
			int l_x = i % 3;
			int l_y = i / 3;
			setLayoutPosition(v, l_x * 0.3333, l_y * 0.3333);
			//mLayout.addView(v);
			numView[i] = v;
		}
	}
	
	/**
	 * Draws the digital cross.
	 */
	private void drawCross() {
		// Setup color
		Paint crossPaint = getPainter();
		
		// Set size
		int iconSize = getPixels(150); // ~2.5cm
		int iconSize_33 = (int)(iconSize * 0.33);
		
		// Draw the cross
		Bitmap cBmp = Bitmap.createBitmap(iconSize + 10, iconSize + 10, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(cBmp);
		Path path = new Path();
		path.moveTo(iconSize_33, 5);
		path.lineTo(iconSize_33*2, 5);
		path.lineTo(iconSize_33*2, iconSize_33);
		path.lineTo(iconSize - 5, iconSize_33);
		path.lineTo(iconSize - 5, iconSize_33*2);
		path.lineTo(iconSize_33*2, iconSize_33*2);
		path.lineTo(iconSize_33*2, iconSize-5);
		path.lineTo(iconSize_33, iconSize-5);
		path.lineTo(iconSize_33, iconSize_33*2);
		path.lineTo(5, iconSize_33*2);
		path.lineTo(5, iconSize_33);
		path.lineTo(iconSize_33, iconSize_33);
		path.close();
		path.offset(0, 0);
		c.drawPath(path, crossPaint);
		
		// Add to screen layout
		cView = new ImageView(this);
		cView.setImageBitmap(cBmp);
		setLayoutPosition(cView, 0.03, 0.5);
		mLayout.addView(cView);
	}
	
	/**
	 * Converts density independent pixel to real screen pixel.
	 * 160 dip = 1 inch ~ 2.5 cm
	 * 
	 * @param dipValue dip
	 * @return pixel
	 */
    public int getPixels(double dipValue) { 
    	int dValue = (int)dipValue;
        Resources r = getResources();
        int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dValue, r.getDisplayMetrics());
        return px; 
   }
    
    /**
     * Moves a view to a screen position.
     * Position is from 0 to 1 and converted to screen pixel.
     * Alignment is top left.
     * 
     * @param view View to move
     * @param x X position from 0 to 1
     * @param y Y position from 0 to 1
     */
	private void setLayoutPosition(View view, double x, double y) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        float screenHeightDp = displayMetrics.heightPixels / displayMetrics.density;
        
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	params.leftMargin = getPixels(screenWidthDp * x);
    	params.topMargin = getPixels(screenHeightDp * y);
    	view.setLayoutParams(params);        
	}
	
    /**
     * Moves a view to a screen position.
     * Position is from 0 to 1 and converted to screen pixel.
     * Alignment is top right.
     * 
     * @param view View to move
     * @param x X position from 0 to 1
     * @param y Y position from 0 to 1
     */
	private void setLayoutPositionRight(View view, double x, double y) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        float screenHeightDp = displayMetrics.heightPixels / displayMetrics.density;
        
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
    	params.rightMargin = getPixels(screenWidthDp * x);
    	params.topMargin = getPixels(screenHeightDp * y);
    	view.setLayoutParams(params);        
	}
}
