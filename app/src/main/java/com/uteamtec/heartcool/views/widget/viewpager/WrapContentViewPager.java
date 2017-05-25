package com.uteamtec.heartcool.views.widget.viewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.uteamtec.heartcool.R;


public class WrapContentViewPager extends ViewPager {

    private int heightPx = 0;

    public int getToolBarHeight() {
        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int toolBarHeight = ta.getDimensionPixelSize(0, -1);
        ta.recycle();
        return toolBarHeight;
    }

    public WrapContentViewPager(Context context) {
        super(context);
        init(context);
    }

    public WrapContentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        heightPx = context.getResources().getDisplayMetrics().heightPixels - getToolBarHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int height = 0;
//        for (int i = 0; i < getChildCount(); i++) {
//            View child = getChildAt(i);
//            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
//            int h = child.getMeasuredHeight();
//            if (h > height) {
//                height = h;
//            }
//            if (heightPx > height) {
//                height = heightPx;
//            }
//        }
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int maxHeight() {
        return heightPx;
    }

}
