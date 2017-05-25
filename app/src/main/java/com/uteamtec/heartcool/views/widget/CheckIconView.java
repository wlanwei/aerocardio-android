package com.uteamtec.heartcool.views.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.uteamtec.heartcool.R;

/**
 * Created by liulingfeng on 2015/11/12.
 */
public class CheckIconView extends View {

    //common variables
    private TransformAnimation anime;
    private float percent = 0;
    private Paint p;
    private int width;
    private int height;
    private int size;
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float x3;
    private float y3;
    private float x2n;
    private float y2n;
    private float x2p;
    private float y2p;

    private float dist12;
    private float dist23;

    private float outterRadius;

    //custom variables
    private int scale = 0;

    public CheckIconView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Initialize paint
        p = new Paint();
        p.setAntiAlias(true);

        //Initialize initial animation
        anime = new TransformAnimation();
        anime.setDuration(500);
        anime.setFillAfter(true);
        anime.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = canvas.getWidth();
        height = canvas.getHeight();

        //set the size of the check
        x1 = width*0.3f - width*0.08f;
        y1 = width*0.5f;
        x2 = width*0.5f - width*0.08f;
        y2 = width*0.7f;
        x3 = width*0.9f - width*0.08f;
        y3 = width*0.3f;
        dist12 = (float) (Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
        dist23 = (float) (Math.sqrt((x3-x2)*(x3-x2) + (y3-y2)*(y3-y2)));

        //set the width of the check
        p.setStrokeWidth(width / 20);

        x2n = (float) (x2 + p.getStrokeWidth()/2* Math.cos(Math.PI/4));
        y2n = (float) (y2 + p.getStrokeWidth()/2* Math.cos(Math.PI/4));
        x2p = (float) (x2 - p.getStrokeWidth()/2* Math.cos(Math.PI/4));
        y2p = (float) (y2 + p.getStrokeWidth()/2* Math.cos(Math.PI/4));

        dist12 = (float) (Math.sqrt((x1-x2n)*(x1-x2n) + (y1-y2n)*(y1-y2n)));
        dist23 = (float) (Math.sqrt((x3-x2p)*(x3-x2p) + (y3-y2p)*(y3-y2p)));

        outterRadius = height > width ? width/2:height/2;

        p.setColor(getResources().getColor(R.color.grass));

        canvas.drawCircle(width/2, height/2, outterRadius, p);


        p.setColor(getResources().getColor(R.color.white));
        canvas.drawCircle(x1, y1, p.getStrokeWidth()/2, p);
        if (percent <= 0.5f) {
            canvas.drawLine(x1, y1, (float) (x1+dist12*2*percent*Math.cos(Math.PI/4)), (float) (y1 + dist12*2*percent*Math.cos(Math.PI/4)), p);
        }
        else {
            canvas.drawLine(x1, y1, x2n, y2n, p);
            canvas.drawLine(x2p, y2p, (float) (x2 + dist23 *2*(percent-0.5f)*Math.cos(Math.PI/4)), (float) (y2-dist23*2*(percent-0.5f)*Math.cos(Math.PI/4)), p);
            canvas.drawCircle((float) (x2 + dist23 *2*(percent-0.5f)*Math.cos(Math.PI/4)), (float) (y2-dist23*2*(percent-0.5f)*Math.cos(Math.PI/4)) , p.getStrokeWidth()/2, p);
        }
    }

    public void startInitAnimation(){
        this.startAnimation(anime);
    }

    public void setEndView(){
        this.percent = 1.0f;
        postInvalidate();
    }

    private class TransformAnimation extends Animation{
        public TransformAnimation(){}

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                percent = interpolatedTime;
            }
            else {
                percent = 1.0f;
            }
            postInvalidate();
        }
    }
}
