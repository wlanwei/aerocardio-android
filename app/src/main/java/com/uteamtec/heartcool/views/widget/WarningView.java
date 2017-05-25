package com.uteamtec.heartcool.views.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uteamtec.heartcool.R;

/**
 * Created by wd
 */
public class WarningView extends RelativeLayout {

    private ImageView _image;
    private TextView _text;

    public WarningView(Context context) {
        super(context);
        init(context);
    }

    public WarningView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WarningView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.wight_warning_view, this);
        _image = (ImageView) findViewById(R.id.img_warning);
        _text = (TextView) findViewById(R.id.tx_warning);
    }

    public enum WarningType {
        NONE,
        LEADOFF,
        NOISE,
    }

    private void showWarning(WarningType type) {
        hideHandler.removeMessages(0);
        switch (type) {
            case LEADOFF:
                _image.setImageResource(R.mipmap.warning_device);
                _text.setText("接触不理想");
                break;
            case NOISE:
                _image.setImageResource(R.mipmap.warning_noise);
                _text.setText("房早");
                break;
            default:
                _image.setVisibility(INVISIBLE);
                _text.setVisibility(INVISIBLE);
                return;
        }
        _image.setVisibility(VISIBLE);
        _text.setVisibility(VISIBLE);
        hideHandler.sendEmptyMessageDelayed(0, 5000);
    }

    public void showWarningUI(Activity activity, final WarningType type) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWarning(type);
                }
            });
        }
    }

    private Handler hideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showWarning(WarningType.NONE);
        }
    };

}
