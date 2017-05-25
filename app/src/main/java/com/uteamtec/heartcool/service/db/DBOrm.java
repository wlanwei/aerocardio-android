package com.uteamtec.heartcool.service.db;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.uteamtec.heartcool.AeroCardioApp;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 数据库Orm
 * Created by wd
 */
public final class DBOrm {

    private LiteOrm liteOrm;

    private DBOrm() {
        liteOrm = LiteOrm.newSingleInstance(new DataBaseConfig(AeroCardioApp.getApplication(),
                "lite_orm.db",
                true, // debug
                3, // version
                null
        ));
    }

    private static DBOrm _instance;

    public static LiteOrm getOrm() {
        if (_instance == null) {
            synchronized (DBOrm.class) {
                if (_instance == null) {
                    _instance = new DBOrm();
                }
            }
        }
        return _instance.liteOrm;
    }

    public static void clean() {
        DBOrm.getOrm().deleteAll(DBDetection.class);
        DBOrm.getOrm().deleteAll(DBEcg.class);
        DBOrm.getOrm().deleteAll(DBEcgMark.class);
        DBOrm.getOrm().deleteAll(DBEcgMarkStats.class);
    }

    public static long save(Object obj) {
        return getOrm().save(obj);
    }

    public static long cascadeSave(Object obj) {
        return getOrm().cascade().save(obj);
    }

    public static <T> int save(Collection<T> objs) {
        return getOrm().save(objs);
    }

    public static <T> int cascadeSave(Collection<T> objs) {
        return getOrm().cascade().save(objs);
    }

    public static <T> ArrayList<T> query(Class<T> c) {
        return getOrm().query(c);
    }

    public static <T> ArrayList<T> cascadeQuery(Class<T> c) {
        return getOrm().cascade().query(c);
    }

    public static <T> ArrayList<T> query(QueryBuilder<T> builder) {
        return getOrm().query(builder);
    }

    public static <T> ArrayList<T> cascadeQuery(QueryBuilder<T> builder) {
        return getOrm().cascade().query(builder);
    }

    public static <T> T queryById(long id, Class<T> c) {
        return getOrm().queryById(id, c);
    }

    public static <T> T cascadeQueryById(long id, Class<T> c) {
        return getOrm().cascade().queryById(id, c);
    }

    public static <T> long queryCount(Class<T> c) {
        return getOrm().queryCount(c);
    }

    public static long queryCount(QueryBuilder builder) {
        return getOrm().queryCount(builder);
    }

}
