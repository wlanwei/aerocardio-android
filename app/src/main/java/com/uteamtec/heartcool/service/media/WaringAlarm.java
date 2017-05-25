package com.uteamtec.heartcool.service.media;

import android.content.Context;
import android.media.MediaPlayer;

import com.uteamtec.heartcool.R;

/**
 * 警报器
 * Created by wd
 */
public class WaringAlarm {

    public static void playWaring(Context context) {
        if (context == null) {
            return;
        }
        MediaPlayer player = MediaPlayer.create(context, R.raw.waring);
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

}
