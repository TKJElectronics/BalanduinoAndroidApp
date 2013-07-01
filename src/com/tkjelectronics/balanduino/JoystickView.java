/**
 * Copyright 2011 Thomas Niederberger
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications and improvements done by Kristian Lauszus, TKJ Electronics.
 */

package com.tkjelectronics.balanduino;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private OnJoystickChangeListener listener;
    
    final int holo_blue_dark = 0xff0099cc;
    final int buttonGray = 0xFF5C5C5C;
    	
    private final int circleColor = buttonGray;
    private int buttonColor = buttonGray;
    
    private float x;
    private float y;
    private float buttonRadius;
    private float joystickRadius;
    private float centerX;
    private float centerY;
    
    private boolean firstDraw = true;
    
    Paint p = new Paint();

    public JoystickView(Context context) {
        super(context);
    }
    public JoystickView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    }
    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Make the layout square        
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(firstDraw) {
        	firstDraw = false;
        	joystickRadius = (float)(getWidth()/3);
        	buttonRadius = joystickRadius/2;
        	centerX = (getWidth())/2;
        	centerY = (getHeight())/2;
        	x = centerX;
        	y = centerY;
        }
        
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        p.setColor(circleColor); 
        canvas.drawCircle(centerX, centerY, joystickRadius, p);
        canvas.drawCircle(centerX, centerY, joystickRadius/2, p);

        p.setColor(buttonColor);
        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, buttonRadius, p);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	x = event.getX();
    	y = event.getY();
        float abs = (float) Math.sqrt((x-centerX)*(x-centerX) + (y-centerY)*(y-centerY));
        if (abs > joystickRadius) {
        	x = ((x-centerX)*joystickRadius/abs + centerX);
        	y = ((y-centerY)*joystickRadius/abs + centerY);
        }
    	invalidate();
    	if (listener != null && event.getAction() == MotionEvent.ACTION_UP) {
    		buttonColor = buttonGray;
    		x = centerX;
    		y = centerY;
    		listener.setOnReleaseListener(0,0);
    	}
    	if (listener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
    		buttonColor = holo_blue_dark; 
    		listener.setOnTouchListener(getXValue(), getYValue());
    	}
    	if (listener != null && event.getAction() == MotionEvent.ACTION_MOVE) {
    		buttonColor = holo_blue_dark;
    		listener.setOnMovedListener(getXValue(), getYValue());    	
    	}
    	return true;
    }
    
    public double getXValue() {
    	return (x-centerX)/joystickRadius; // X-axis is positive at the right side
    }
    
    public double getYValue() {
    	return ((y-centerY)/joystickRadius)*-1; // Y-axis should be positive upwards
    }   

    public void setOnJoystickChangeListener(OnJoystickChangeListener listener) {
    	this.listener = listener;
    }
    
    public interface OnJoystickChangeListener {
    	public void setOnTouchListener(double xValue, double yValue);
    	public void setOnMovedListener(double xValue, double yValue);
    	public void setOnReleaseListener(double xValue, double yValue);
    }
}
