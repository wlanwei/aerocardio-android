package com.uteamtec.heartcool.views.widget;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Animation dedicated for login button
 * Created by liulingfeng on 2015/11/14.
 */
public class LoginAnimation extends Animation {
    private float percent;
    public LoginAnimation() {
        super();
    }


    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0f) {
            percent = interpolatedTime;
        }
        else {
            percent = 1.0f;
        }

    }
}
