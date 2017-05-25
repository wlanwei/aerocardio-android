package com.uteamtec.heartcool.service.cache;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wd
 */
public final class ACacheHelper<E extends Serializable> {

    private final Class<E> clazz;

    private final ACache cache;

    private static final String KeyJournal = "journal";

    private static final String KeyJournalTime = "time";
    private static final String KeyJournalLength = "length";

    private JSONArray journal = null;

    public ACacheHelper(Class<E> clazz, Context context, String cacheName) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        this.clazz = clazz;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            this.cache = ACache.get(new File(context.getExternalFilesDir(null), cacheName));
        } else {
            this.cache = ACache.get(context, cacheName);
        }
    }

    public boolean sameClassAs(Object o) {
        return o != null && o.getClass() == clazz;
    }

    private void writeJournal() {
        cache.put(KeyJournal, readJournal());
    }

    private JSONArray readJournal() {
        if (journal == null) {
            journal = cache.getAsJSONArray(KeyJournal);
        }
        if (journal == null) {
            journal = new JSONArray();
        }
        return journal;
    }

    private void popJournal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            readJournal().remove(0);
        } else {
            journal = readJournal();
            JSONArray arr = new JSONArray();
            for (int i = 1; i < journal.length(); i++) {
                arr.put(journal.opt(i));
            }
            journal = arr;
        }
    }

    synchronized public void putAll(List<E> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        final String TIME = String.valueOf(System.currentTimeMillis());
        final String LEN = String.valueOf(values.size());
        int i = 0;
        for (E e : values) {
            cache.put(TIME + i, e);
            i++;
        }
        readJournal().put(new JSONObject(new HashMap<String, String>() {{
            put(KeyJournalTime, TIME);
            put(KeyJournalLength, LEN);
        }}));
        writeJournal();
    }

    synchronized public List<E> drain() {
        if (readJournal().length() != 0) {
            JSONObject obj = journal.optJSONObject(0);
            if (obj == null) {
                popJournal();
                return drain();
            }
            final String TIME = obj.optString(KeyJournalTime);
            final int LEN = obj.optInt(KeyJournalLength, 0);
            if (!TextUtils.isEmpty(TIME) && LEN > 0) {
                List<E> list = new ArrayList<>();
                for (int i = 0; i < LEN; i++) {
                    Object o = cache.getAsObject(TIME + i);
                    if (sameClassAs(o)) {
                        list.add((E) o);
                        cache.remove(TIME + i);
                    }
                }
                popJournal();
                writeJournal();
                return list;
            }
        }
        return null;
    }

}
