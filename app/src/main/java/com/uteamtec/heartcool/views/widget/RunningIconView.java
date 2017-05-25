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
public class RunningIconView extends View {

    //common variables
    private TransformAnimation anime;
    private float percent = 0;
    private Paint p;
    private int width;
    private int height;
    private int outterRadius;

    //custom variables
    private int scale = 0;

    public RunningIconView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Initialize paint
        p = new Paint();
        p.setAntiAlias(true);

        //Initialize initial animation
        anime = new TransformAnimation();
        anime.setDuration(2000);
        anime.setFillAfter(true);
        anime.setInterpolator(new LinearInterpolator());
        anime.setRepeatMode(Animation.RESTART);
        anime.setRepeatCount(Animation.INFINITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = canvas.getWidth();
        height = canvas.getHeight();
        outterRadius = height > width ? width/2:height/2;
        p.setColor(getResources().getColor(R.color.grass));

        canvas.drawCircle(width/2, height/2, outterRadius, p);

        p.setColor(getResources().getColor(R.color.white));
        //1st circle
        float scaleValue1 = (float) Math.abs(Math.sin(2*Math.PI*percent ));
        p.setAlpha( (int) (scaleValue1*scaleValue1*200) );

        canvas.drawCircle(width / 2 - width / 3, height / 2, width / 10, p);
//        canvas.drawCircle(width / 2 - width / 6, height / 2, width / 32 + (float) (width / 32 * scaleValue1), p);
//        canvas.drawCircle(width / 2 - width / 6, height / 2, width / 32 + (float) (width / 32 * Math.abs(Math.cos(2 * Math.PI * percent))), p);

        //2nd circle
        float scaleValue2 = (float) Math.abs(Math.sin(2*Math.PI*percent - Math.PI/4 ));
        p.setAlpha( (int) (scaleValue2*scaleValue2*200) );
        canvas.drawCircle(width / 2, height / 2, width / 10 , p);
//        canvas.drawCircle(width / 2, height / 2, width / 32 + (float) (width / 32 * scaleValue2), p);

        //3rd circle
        float scaleValue3 = (float) Math.abs(Math.sin(2*Math.PI*percent - Math.PI/2 ));
        p.setAlpha( (int) (scaleValue3*scaleValue3*200) );
        canvas.drawCircle(width / 2 + width / 3, height / 2, width / 10 , p);
//        canvas.drawCircle(width / 2 + width / 6, height / 2, width / 32 + (float) (width / 32 * scaleValue3), p);
//        canvas.drawCircle(width/2 + width/6, height/2 , width/32 + (float) (width/32*Math.abs(Math.cos(2*Math.PI*percent + Math.PI/4))), p);

    }

    public void startInitAnimation(){
        this.startAnimation(anime);
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
