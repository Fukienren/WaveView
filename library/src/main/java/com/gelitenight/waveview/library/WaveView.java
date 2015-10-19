/*
 *  Copyright (C) 2015, gelitenight(gelitenight@gmail.com).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.gelitenight.waveview.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class WaveView extends View {
    /**
     * +------------------------+
     * |<--wave length->        |______
     * |   /\          |   /\   |  |
     * |  /  \         |  /  \  | amplitude
     * | /    \        | /    \ |  |
     * |/      \       |/      \|__|____
     * |        \      /        |  |
     * |         \    /         |  |
     * |          \  /          |  |
     * |           \/           | water level
     * |                        |  |
     * |                        |  |
     * +------------------------+__|____
     */
    private static final float DEFAULT_AMPLITUDE_RATIO = 0.05f;
    private static final float DEFAULT_WATER_LEVEL_RATIO = 0.5f;
    private static final float DEFAULT_WAVE_LENGTH_RATIO = 1.0f;
    private static final float DEFAULT_WAVE_SHIFT_RATIO = 0.0f;

    // if true, the shader will display the wave
    private boolean mShowWave;


    // paint to draw wave
    private Paint mViewPaint;
    // paint to draw border
    private Paint mBorderPaint;

    private float mDefaultAmplitude;
    private float mDefaultWaterLevel;
    private float mDefaultWaveLength;
    private double mDefaultAngularFrequency;

    private float mAmplitudeRatio = DEFAULT_AMPLITUDE_RATIO;
    private float mWaveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO;
    private float mWaterLevelRatio = DEFAULT_WATER_LEVEL_RATIO;
    private float mWaveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, mViewPaint);
        genPaint(0xFF33CC55,0xFF559966);
    }

    public float getWaveShiftRatio() {
        return mWaveShiftRatio;
    }

    /**
     * Shift the wave horizontally according to <code>waveShiftRatio</code>.
     *
     * @param waveShiftRatio Should be 0 ~ 1. Default to be 0.
     *                       <br/>Result of waveShiftRatio multiples width of WaveView is the length to shift.
     */
    public void setWaveShiftRatio(float waveShiftRatio) {
        if (mWaveShiftRatio != waveShiftRatio) {
            mWaveShiftRatio = waveShiftRatio;
            invalidate();
        }
    }

    public float getWaterLevelRatio() {
        return mWaterLevelRatio;
    }

    /**
     * Set water level according to <code>waterLevelRatio</code>.
     *
     * @param waterLevelRatio Should be 0 ~ 1. Default to be 0.5.
     *                        <br/>Ratio of water level to WaveView height.
     */
    public void setWaterLevelRatio(float waterLevelRatio) {
        if (mWaterLevelRatio != waterLevelRatio) {
            mWaterLevelRatio = waterLevelRatio;
            invalidate();
        }
    }

    public float getAmplitudeRatio() {
        return mAmplitudeRatio;
    }

    /**
     * Set vertical size of wave according to <code>amplitudeRatio</code>
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     *                       <br/>Ratio of amplitude to height of WaveView.
     */
    public void setAmplitudeRatio(float amplitudeRatio) {
        if (mAmplitudeRatio != amplitudeRatio) {
            mAmplitudeRatio = amplitudeRatio;
            invalidate();
        }
    }

    public float getWaveLengthRatio() {
        return mWaveLengthRatio;
    }

    /**
     * Set horizontal size of wave according to <code>waveLengthRatio</code>
     *
     * @param waveLengthRatio Default to be 1.
     *                        <br/>Ratio of wave length to width of WaveView.
     */
    public void setWaveLengthRatio(float waveLengthRatio) {
        mWaveLengthRatio = waveLengthRatio;
    }

    public boolean isShowWave() {
        return mShowWave;
    }

    public void setShowWave(boolean showWave) {
        mShowWave = showWave;
    }

    public void setBorder(int width, int color) {
        if (mBorderPaint == null) {
            mBorderPaint = new Paint();
            mBorderPaint.setStyle(Style.STROKE);
        }
        mBorderPaint.setColor(color);
        mBorderPaint.setStrokeWidth(width);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(w != oldw) {
            genMask();
        }
    }




    Path mWave1;
    Path mWave2;
    Path maskpath;
    Paint mWavePaint1;
    Paint mWavePaint2;
    Paint maskPaint;


    void genPaint(int color1,int color2){


        mWavePaint1 = new Paint();
        mWavePaint1.setColor(color1);
        mWavePaint1.setAlpha(40);
        mWavePaint1.setAntiAlias(true);

        mWavePaint2 = new Paint();
        mWavePaint2.setColor(color1);
        mWavePaint2.setAlpha(60);
        mWavePaint2.setAntiAlias(true);
    }

    void genMask(){

        //mask
        float radius = getWidth() / 2f;
        maskPaint = new Paint();
        maskPaint.setXfermode(
                new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        maskPaint.setColor(Color.TRANSPARENT);
        maskPaint.setStyle(Style.FILL);
        maskPaint.setAntiAlias(true);
        maskpath = new Path();
        maskpath.moveTo(0, 0);
        maskpath.lineTo(getWidth(), 0);
        maskpath.lineTo(getWidth(), getHeight() / 2f);
        //RectF rect = new RectF(0,0,getWidth(),getHeight());
        maskpath.addCircle(getWidth() / 2f, getHeight() / 2f, radius, Path.Direction.CCW);

        maskpath.lineTo(getWidth(), getHeight());
        maskpath.lineTo(0, getHeight());
        maskpath.lineTo(0, 0);
        maskpath.close();
    }

    void genPath(){
        mDefaultAngularFrequency = 2.0f * Math.PI / mWaveLengthRatio / getWidth();
        mDefaultAmplitude = getHeight() * mAmplitudeRatio;
        mDefaultWaterLevel = getHeight() * (1.0f-mWaterLevelRatio);
        mDefaultWaveLength = getWidth();


        float waveX1 = 0;
        float xOffset = mWaveShiftRatio * getWidth();
        final float wave2Shift = mDefaultWaveLength / 4;
        final float endX = getWidth();
        final float endY = getHeight();



        double wx = (waveX1-xOffset) * mDefaultAngularFrequency;
        int startY = (int) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx));
        Path path = new Path();


        path.moveTo(0, startY);

        Path path2 = new Path();

        double wxb = (0- xOffset- wave2Shift) * mDefaultAngularFrequency;
        int startYb = (int) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wxb));
        path2.moveTo(0, startYb);
        while (waveX1 < endX) {

            waveX1++;
            double wx1 = (waveX1- xOffset) * mDefaultAngularFrequency;

            int startY1 = (int) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx1));

            path.lineTo(waveX1, startY1);
            wx1 = (waveX1 - xOffset- wave2Shift) * mDefaultAngularFrequency;
            startY1 = (int) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx1));
            path2.lineTo(waveX1, startY1);

        }

        path.lineTo(endX, endY);
        //RectF oval = new RectF(0,0,getWidth(),getHeight());
        //path.arcTo(oval,0,180);
        path.lineTo(0, endY);
        path.lineTo(0, 0);
        path.close();


        double wx2 = (endX - wave2Shift) * mDefaultAngularFrequency;
        int startY2 = (int) (mDefaultWaterLevel + mDefaultAmplitude * Math.sin(wx2));
        path2.lineTo(endX, startY2);
        path2.lineTo(endX, endY);
        path2.lineTo(0, endY);
        path2.lineTo(0, 0);
        path2.close();


        mWave1 = path;
        mWave2 = path2;



    }


    boolean mfonDraw = false;

    @Override
    public void invalidate() {
        if(mfonDraw == false) {
            mfonDraw = true;
            super.invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mfonDraw = false;
        genPath();
        canvas.drawColor(Color.TRANSPARENT);
        if (mShowWave ) {
            canvas.save();
            canvas.drawPath(mWave1, mWavePaint1);
            canvas.drawPath(mWave2, mWavePaint2);
            canvas.drawPath(maskpath,maskPaint);
            canvas.restore();

            if (mBorderPaint != null) {
                mBorderPaint.setAntiAlias(true);
                canvas.drawCircle(getWidth() / 2f, getHeight() / 2f,
                        (getWidth() - mBorderPaint.getStrokeWidth()) / 2f, mBorderPaint);
            }
        }
    }

}
