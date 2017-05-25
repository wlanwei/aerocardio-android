package com.uteamtec.heartcool.service.db;

import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

/**
 * 数据库表模板
 * Created by wd
 */
abstract class DBModel implements Serializable {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
