package com.uteamtec.heartcool.views.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uteamtec.heartcool.R;
import com.uteamtec.heartcool.utils.L;

/**
 * Created by liulingfeng on 2015/11/18.
 */
public class RingMeterView extends RelativeLayout{
    private static final int DEFAULT_FOREGROUND_RING_COLOR = Color.argb(255, 0x46, 0xd3, 0x87);
    private static final int DEFAULT_BACKGROUND_RING_COLOR = Color.argb(255, 0xe3, 0xf9, 0xed);
    private static final int DEFAULT_VALUE_COLOR = Color.argb(255, 0x46, 0xd3, 0x87);
    private static final int DEFAULT_UNIT_COLOR = Color.argb(255, 0xa2, 0xa2, 0xa2);

    private static final int DEFAULT_RANGE = 100;

    private static final float VALUE_FONT_SCALE = 0.24f;
    private static final float UNIT_FONT_SCALE = 0.08f;

    private static final float VALUE_POS_Y = 0.456f;

    private static final float UNIT_POS_Y = 0.732f;

    private static final float RING_WIDTH_SCALE = 0.1f;

    String unit;
    int range;
    int value;
    int ringWidth;

    private RingMeterView viewRoot;
    private RingView ring;
    private TextView valueText;
    private TextView unitText;

    private ViewTreeObserver viewTreeObserver;

    public RingMeterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray customSettings = context.obtainStyledAttributes(attrs, R.styleable.RingMeterView);
        this.unit = customSettings.getString(R.styleable.RingMeterView_unit);
        this.unit = "bpm";
        this.range = customSettings.getInt(R.styleable.RingMeterView_range, DEFAULT_RANGE);
        this.value = 0;

        valueText = new TextView(context);
        valueText.setText(Integer.toString(value));
        valueText.setTextColor(DEFAULT_VALUE_COLOR);
        unitText = new TextView(context);
        unitText.setText(unit);
        unitText.setTextColor(DEFAULT_UNIT_COLOR);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        this.addView(valueText, params);

        params = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        this.addView(unitText, params);

        ring = new RingView(context);
        params = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.addView(ring, params);

//        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewRoot = this;


        viewTreeObserver = this.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //get height and widht of the layout
                int h = viewRoot.getHeight();
                int w = viewRoot.getWidth();
                float radius = h > w ? w/2.0f : h/2.0f;

                valueText.setTextSize((int) (radius * VALUE_FONT_SCALE)) ;
                int valueTextMarginT = (int) (h/1.8*VALUE_POS_Y - valueText.getHeight()/2.0f);

                LayoutParams paramsSub = (LayoutParams) valueText.getLayoutParams();
                paramsSub.setMargins(0, valueTextMarginT, 0, 0);
                valueText.setLayoutParams(paramsSub);

                unitText.setTextSize((int) (radius * UNIT_FONT_SCALE));
                int unitTextMarginT =  (int) (h*UNIT_POS_Y - unitText.getHeight()/2.0f);
                paramsSub = (LayoutParams) unitText.getLayoutParams();
                paramsSub.setMargins(0, unitTextMarginT, 0, 0);
                unitText.setLayoutParams(paramsSub);

                ringWidth = (int) (radius*RING_WIDTH_SCALE);

            }
        });


//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(R.layout.layout_ringmeter, this);
    }

    public void setValue(int val) {
        if (val > range ) {
            val = range;
        }
        else if (val < 0) {
            val = 0;
        }

        ring.setValue(val);
        this.value = val;
        this.valueText.setText(Integer.toString(value));
    }

    public int getValue(){
        return value;
    }

    private class RingView extends View {
        float percent;
        int oldValue = 0;
        int newValue = 0;
        int valueDiff;
        boolean isInAnimation = false;

        public RingView(Context context) {
            super(context);
            this.setRotation(-90);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            float radius = h > w ? w/2.0f : h/2.0f;

            float transitValue;
            if (newValue != oldValue) {
                transitValue = valueDiff * percent + oldValue;
            }
            else{
                transitValue = newValue;
            }
            L.i("<UI> oldValue = " + Integer.toString(oldValue) + " new value = " + Integer.toString(newValue) + " transitValue = " + Float.toString(transitValue));
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setAntiAlias(true);
            p.setColor(DEFAULT_BACKGROUND_RING_COLOR);
            p.setStrokeWidth(ringWidth);
            canvas.drawCircle(w / 2, h / 2, radius - ringWidth / 2.0f, p);
            p.setColor(DEFAULT_FOREGROUND_RING_COLOR);

            float startAngle = 0.0f;
            float sweepAngle = transitValue*360.0f/range;
            RectF oval = new RectF(ringWidth/2.0f,ringWidth/2.0f,w-ringWidth/2.0f,h-ringWidth/2.0f);
            canvas.drawArc(oval, startAngle, sweepAngle, false, p);
        }

        public void setValue(int val){
            if (isInAnimation) {
                return;
            }

            newValue = val;

            valueDiff = newValue - oldValue;

            percent = 0;
            TransformAnimation anime = new TransformAnimation();
            anime.setInterpolator(new DecelerateInterpolator());
            anime.setDuration(2000);
            anime.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //add flag to cancel new incoming setValue()
                    isInAnimation = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    oldValue = newValue;
                    isInAnimation = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            anime.setFillAfter(true);
            this.startAnimation(anime);
        }

        private class TransformAnimation extends Animation {
            public TransformAnimation (){}

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);
                percent = interpolatedTime;
                postInvalidate();
            }
        }
    }


}
