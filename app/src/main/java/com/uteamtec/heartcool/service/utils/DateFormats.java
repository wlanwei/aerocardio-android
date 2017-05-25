package com.uteamtec.heartcool.service.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 时间格式集合
 * Created by wd
 */
public final class DateFormats {

    public static final SimpleDateFormat YYYY = new SimpleDateFormat(
            "yyyy", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM = new SimpleDateFormat(
            "yyyy-MM", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM_DD = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static final SimpleDateFormat YYYY_MM_CN = new SimpleDateFormat(
            "yyyy年MM月", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM_DD_CN = new SimpleDateFormat(
            "yyyy年MM月dd日", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM_DD_HH_MM_CN = new SimpleDateFormat(
            "yyyy年MM月dd日 HH时mm分", Locale.getDefault());
    public static final SimpleDateFormat YYYY_MM_DD_HH_MM_SS_CN = new SimpleDateFormat(
            "yyyy年MM月dd日 HH时mm分ss秒", Locale.getDefault());

    public static final SimpleDateFormat HH_MM_SS = new SimpleDateFormat(
            "HH:mm:ss", Locale.getDefault());

    public static final SimpleDateFormat HH_MM_SS_CN = new SimpleDateFormat(
            "HH时mm分ss秒", Locale.getDefault());

}
