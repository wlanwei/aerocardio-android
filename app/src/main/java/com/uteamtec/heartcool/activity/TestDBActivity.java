package com.uteamtec.heartcool.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.uteamtec.algorithm.types.Ecg;
import com.uteamtec.heartcool.service.db.DBDetection;
import com.uteamtec.heartcool.service.db.DBEcg;
import com.uteamtec.heartcool.service.db.DBEcgMark;
import com.uteamtec.heartcool.service.db.DBOrm;
import com.uteamtec.heartcool.service.type.EcgMark;

import java.util.ArrayList;

/**
 * 数据库测试页面
 * Created by wd
 */
public class TestDBActivity extends BaseActivity {

    private static final String TAG = TestDBActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        clear();
        Log.e(TAG, "======================Save======================");
        save();
        Log.e(TAG, "======================Query======================");
        query();
        Log.e(TAG, "======================Finish======================");
    }

    private void clear() {
//        DBOrm.getOrm().dropTable(DBDetection.class);
//        DBOrm.getOrm().dropTable(DBEcg.class);
//        DBOrm.getOrm().dropTable(DBEcgMark.class);
        DBOrm.clean();
    }

    private void save() {
        DBDetection de = new DBDetection("ABC", "MAC");
        {
            DBEcgMark m = new DBEcgMark(new EcgMark(11, 12, 13, 14, 15));
            m.addEcg(new DBEcg(new Ecg(Ecg.TYPE_SINGLE, 11, 12, 13, new int[]{14, 15, 16})));
            de.addMark(m);
        }
        {
            DBEcgMark m = new DBEcgMark(new EcgMark(21, 22, 23, 24, 25));
            m.addEcg(new DBEcg(new Ecg(Ecg.TYPE_THREE, 21, 22, 23, new int[]{24, 25, 26})));
            de.addMark(m);
        }
        {
            DBEcgMark m = new DBEcgMark(new EcgMark(31, 32, 33, 34, 35));
            m.addEcg(new DBEcg(new Ecg(Ecg.TYPE_FULL, 31, 32, 33, new int[]{34, 35, 36})));
            de.addMark(m);
        }
        Log.e(TAG, "Save: " + de.toString());

        DBOrm.cascadeSave(de);
    }

    private void query() {
        DBDetection d = null;
        ArrayList<DBDetection> des = DBOrm.cascadeQuery(DBDetection.class);
        for (DBDetection de : des) {
            Log.e(TAG, "Query: " + de.toString());
            d = de;
        }
        if (d == null) {
            return;
        }
        for (DBEcgMark m : d.getMarks()) {
            Log.e(TAG, "Query: " + m.toString());
//            for (DBEcg e : m.getEcgs()) {
//                Log.e(TAG, "Query: " + e.toString());
//            }
        }
    }

}
