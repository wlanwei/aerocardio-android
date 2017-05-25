package com.uteamtec.heartcool.user;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by admin on 2016/5/9.
 */
public class HeartCoolBrowser extends View {


    private List<Short> list=new ArrayList();

    float scale=1,tempscale=1; //屏幕放大倍数,缓存屏幕放大倍数
    private int space_X=20,space_Y=20;//网格x轴和y轴的距离
    private short spaceData=2;       //数据的倍数
    private int normalMax=100,normalMin=60; //分割线的值

    float offset_X,offset_Y,offsetTemp_X,offsetTemp_Y;  //偏移量x，偏移量y，临时偏移量x，临时偏移量y
//    float[] data={2,23,56,8,45,-97,-43,47,95,-26,56,65,56,34,34,3,7,-78,-78,48,-37,19,57,-43,-64,-24,-23,-65,53,99,-9,-99,54};
    float[] data=new float[1000];

    public HeartCoolBrowser(Context context) {
        super(context);


    }
    public HeartCoolBrowser(Context context, AttributeSet attrs) {
        super(context, attrs);

        Random random=new Random();
        for (int i = 0; i < data.length; i ++) {
            data[i] = (float)random.nextInt(200)-100;
        }
        invalidate();

    }

    public HeartCoolBrowser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }
    public void addData(short roate){
        list.add(roate);
        clearAnimation();
        invalidate();

    }
    public void clearData(){
        list.clear();
//        clearAnimation();
//        invalidate();
    }

    //设置Y轴倍数
    public void setSpaceData(short spaceData){
        this.spaceData=spaceData;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLines(canvas);  //绘制边框方格
        drawCurve(canvas);  //绘制曲线

    }

    private int currentCurve_X;
    //绘制曲线
    public void drawCurve(Canvas canvas){
        int curve_X=space_X;
        currentCurve_X=list.size()*space_X;
        for (int i = 1;i<list.size();i++){


            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            paint.setColor(Color.rgb(255,99,124));

            float startX=(curve_X-space_X-currentCurve_X+getWidth())*scale+offset_X;
            float stopX=(curve_X-currentCurve_X+getWidth())*scale+offset_X;
            float startY=(getHeight()*2)/3+(60-list.get(i-1))*spaceData*scale+offset_Y;
            float stopY=(getHeight()*2)/3+(60-list.get(i))*spaceData*scale+offset_Y;

            float tempStart_X=startX-currentCurve_X+getWidth();
            float tempSttop_X=stopX-currentCurve_X+getWidth();
            canvas.drawLine(startX,startY,stopX,stopY,paint);

            curve_X+=space_X;
        }
    }
    //绘制方格
    public void drawLines(Canvas canvas){

        int middleX= getWidth()/2;
        int middleY= getHeight()/2;
        int count_x=0,count_y=0;
        Paint paint=new Paint();
        paint.setColor(Color.rgb(100, 100, 100));
        paint.setStrokeWidth(2);
        float num_x = 0,num_y=0;



        while (num_x<getWidth()){
            canvas.drawLine(middleX+num_x,0,middleX+num_x,getHeight(),paint);
            canvas.drawLine(middleX-num_x,0,middleX-num_x,getHeight(),paint);

            num_x=++count_x*space_X*scale;
        }

        while (num_y<middleY){
            canvas.drawLine(0,middleY+num_y,getWidth(),middleY+num_y,paint);
            canvas.drawLine(0,middleY-num_y,getWidth(),middleY-num_y,paint);

            num_y=++count_y*space_Y*scale;
        }
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);


        canvas.drawLine(0, (getHeight() * 2) / 3 + (60 - normalMax) * spaceData * scale + offset_Y, getWidth(), (getHeight() * 2) / 3 + (60 - normalMax) * spaceData * scale + offset_Y, paint);
        canvas.drawLine(0, (getHeight() * 2) / 3 + (60 - normalMin) * spaceData * scale + offset_Y, getWidth(), (getHeight() * 2) / 3 + (60 - normalMin) * spaceData * scale + offset_Y, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText(normalMax + "", 0, (getHeight() * 2) / 3 + (60 - normalMax) * spaceData * scale + offset_Y - 10, paint);
        canvas.drawText(normalMin + "", 0, (getHeight() * 2) / 3 + (60 - normalMin) * spaceData * scale + offset_Y - 10, paint);
    }

    //触摸事件的属性
    private PointF startPoint = new PointF();//触摸的开始点
    private int mode = 0; // 用于标记模式
    private static final int DRAG = 1; // 拖动
    private static final int ZOOM = 2; // 放大
    private float initDis=0; //两个手指的相距距离
    @Override
    public boolean onTouchEvent(MotionEvent event) {


        int action = event.getAction();
        // 多点触摸的时候 必须加上MotionEvent.ACTION_MASK
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startPoint.set(event.getX(), event.getY()); // 开始点
                // 初始为drag模式
                mode = DRAG;

                tempscale=scale;
                offsetTemp_X=offset_X;
                offsetTemp_Y=offset_Y;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                // 初始的两个触摸点间的距离
                initDis = spacing(event);
                // 设置为缩放模式
                mode = ZOOM;


                break;

            case MotionEvent.ACTION_MOVE:


                // drag模式
                if (mode == DRAG) {
                    offset_X=offsetTemp_X+event.getX()-startPoint.x;
                    offset_Y=offsetTemp_Y+event.getY()-startPoint.y;
                } else if (mode == ZOOM) {
                    float newDis = spacing(event);
                    // 计算出缩放比例
                    scale =tempscale*newDis / initDis;
                    if(scale>3){
                        scale=3;
                    }if(scale<0.5){
                        scale=0.5f;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_POINTER_UP:
                mode = 0;
                break;
        }

        invalidate();
        return true;

    }

    //取两点的距离
    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (IllegalArgumentException ex) {
            Log.v("TAG", ex.getLocalizedMessage());
            return 0;
        }
    }

    //取两点的中点
    private void midPoint(PointF point, MotionEvent event) {
        try {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        } catch (IllegalArgumentException ex) {

            //这个异常是android自带的，网上清一色的这么说。。。。
            Log.v("TAG", ex.getLocalizedMessage());
        }
    }
}
