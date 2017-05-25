package com.uteamtec.heartcool.user;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

public class TimePickerView extends View {

    private Paint textPaint;
    private Paint circPaint;
    private Paint clickPaint;
    private Paint bgPaint;

    private int bacgroundColor = Color.rgb(255, 255, 255);//背景颜色
    private int circColor = Color.rgb(234, 246, 248);//选中数据的颜色
    private int textColor = Color.rgb(150, 150, 150);//字体的颜色
    private int textSize;

    private CallBack cb;

    private int startX;
    private int startY;

    int tatolDay = 40;
    int startDay = 3;
    private int clickindex = 100;
    private int[] needDraw = {};
    String[] dayName = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private ArrayList<Circle> circles = new ArrayList<>();

    public void setClickindex(int clickindex) {
        this.clickindex = clickindex;
    }

    public TimePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        textPaint = new Paint();
        circPaint = new Paint();
        clickPaint = new Paint();
        bgPaint = new Paint();
    }

    public TimePickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePickerView(Context context) {
        this(context, null);
    }

    private void initData() {
        textSize = getWidth() / 25;
        startX = startY = getWidth() / 8;
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        circPaint.setAntiAlias(true);
        circPaint.setStyle(Style.FILL);
        circPaint.setColor(circColor);
        clickPaint.setAntiAlias(true);
        clickPaint.setStyle(Style.STROKE);
        clickPaint.setStrokeWidth(2);
        clickPaint.setColor(Color.rgb(26, 161, 170));
        bgPaint.setColor(bacgroundColor);
        bgPaint.setStyle(Style.FILL);
        circles = new ArrayList<Circle>();
    }

    public void setData(int year, int month, int[] days, CallBack cb) {
        this.needDraw = days;
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        tatolDay = calendar.getActualMaximum(Calendar.DATE);
        this.cb = cb;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i("执行了ondraw方法--》", clickindex + "");
        initData();
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        //

        for (int i = 0; i < dayName.length; i++) {
            canvas.drawText(dayName[i], startX / 2 + (i) * startX, startY, textPaint);
        }
        //定义日历的高度和宽度
        int tempX = startX / 2;
        int tempY = startY + textSize * 3;
        System.out.println("[getwidth]" + getWidth());

        int nowDay = 1;

        for (int i = 0; i < tatolDay + startDay; i++) {
            if (i < startDay) {
                //canvas.drawText("", tempX,tempY,textPaint);
            } else {
                for (int j = 0; j < needDraw.length; j++) {
                    if (nowDay == needDraw[j]) {
                        //
                        if (nowDay == clickindex) {
                            canvas.drawCircle(tempX + textSize / 2, tempY - textSize / 3, startX / 3, clickPaint);
                            Circle circle = new Circle();
                            circle.x = tempX + textSize / 2;
                            circle.y = tempY - textSize / 2;
                            circle.r = textSize;
                            circle.nowDay = nowDay;
                            circles.add(circle);
                        } else {
                            canvas.drawCircle(tempX + textSize / 2, tempY - textSize / 3, startX / 3, circPaint);
                            Circle circle = new Circle();
                            circle.x = tempX + textSize / 2;
                            circle.y = tempY - textSize / 2;
                            circle.r = textSize;
                            circle.nowDay = nowDay;
                            circles.add(circle);
                        }

                    }
                }
                if (nowDay < 10) {
                    canvas.drawText(" " + nowDay + " ", tempX, tempY, textPaint);
                } else {
                    canvas.drawText(nowDay + "", tempX, tempY, textPaint);
                }

                nowDay++;
            }
            if ((i + 1) % 7 == 0) {
                tempX = startX / 2 + 3;
                tempY += textSize * 2 + 5;
            } else {
                tempX += startX + 3;
            }
        }
    }


	/*@Override
    public void onClick(View v) {
		int x = (int) v.getX();
		int y = (int) v.getY();
		System.out.println("[onclick]");
		for(int i=0;i<circles.size();i++){
			if(isIner(x, y,circles.get(i))){
				System.out.println("[�����]"+circles.get(i).nowDay);
			}
		}
	}*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < circles.size(); i++) {
                    if (isIner((int) event.getX(), (int) event.getY(), circles.get(i))) {
//					clickindex= circles.get(i).nowDay;
//					invalidate();
//					Log.i("------>", clickindex + "");
                        cb.cb(circles.get(i).nowDay);

                    }
                }
                break;

            default:
                break;
        }

        return true;
    }

    private boolean isIner(int x, int y, Circle circle) {
        int temp = (x - circle.x) * (x - circle.x) + (y - circle.y) * (y - circle.y);
        int r = (int) Math.sqrt(temp);
        System.out.println("[y] " + y + " [x] " + x + " [nowday] " + circle.nowDay + "  [r] " + r + "   [cr]" + circle.r);
        if (r < circle.r) {
            return true;
        } else {
            return false;
        }
    }

    class Circle {
        public int x;
        public int y;
        public int r;
        public int nowDay;
    }

    public interface CallBack {
        public void cb(int nowday);
    }



	/*private void setWeek(Canvas canvas){
        boolean isChinese = sf.getBoolean("isChinese",true);//获取中文状态
		boolean isEnglish = sf.getBoolean("isEnglish",true);//获取英文状态
		boolean isArab = sf.getBoolean("isArab", true);//获取阿拉伯语言
		if(isChinese==true && isEnglish==false & isArab==false){
			String[] dayName=new String[]{"周日","周一","周二","周三","周四","周五","周六"};
			for(int i=0;i<dayName.length;i++){
				canvas.drawText(dayName[i],startX/2+(i)*startX,startY, textPaint);
			}
		} else if(isChinese==false && isEnglish==true & isArab==false){
			String[] dayName=new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
			for(int i=0;i<dayName.length;i++){
				canvas.drawText(dayName[i],startX/2+(i)*startX,startY, textPaint);
			}
		}else if(isChinese==false && isEnglish==false & isArab==true){
			String[] dayName=new String[]{"الأحد","يوم الاثنين","الثلاثاء","يوم الأربعاء","يوم الخميس","الجمعة","السبت"};
			for(int i=0;i<dayName.length;i++){
				canvas.drawText(dayName[i],startX/2+(i)*startX,startY, textPaint);
			}
		}
	}*/


}
