package com.uteamtec.heartcool.service.stats;

/**
 * Created by wd
 */
final class MutableLong {

    private long val;

    public MutableLong() {
        this.val = 0;
    }

    public MutableLong(long val) {
        this.val = val;
    }

    public long get() {
        return this.val;
    }

    public void set(long val) {
        this.val = val;
    }

    public MutableLong increment() {
        ++this.val;
        return this;
    }

    @Override
    public String toString() {
        return Long.toString(val);
    }

}
