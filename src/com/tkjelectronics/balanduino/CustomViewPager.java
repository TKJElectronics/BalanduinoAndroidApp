/*
 * This is a custom ViewPager that allows me to enable and disable the ViewPager
 * When the button is pressed
 * Source: http://blog.svpino.com/2011/08/disabling-pagingswiping-on-android.html
 */

package com.tkjelectronics.balanduino;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private static boolean pagerEnabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        pagerEnabled = true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pagerEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (pagerEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public static void setPagingEnabled(boolean e) {
        pagerEnabled = e;
    }
}