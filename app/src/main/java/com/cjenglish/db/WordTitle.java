package com.cjenglish.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * Created by cc on 2019/3/25.
 */
@Entity
public class WordTitle implements Serializable {

    @Id(autoincrement = true)
    private Long id;
    private String name;
    private int seqNumber;
    private Long timeCreate;

    @Generated(hash = 815531688)
    public WordTitle(Long id, String name, int seqNumber, Long timeCreate) {
        this.id = id;
        this.name = name;
        this.seqNumber = seqNumber;
        this.timeCreate = timeCreate;
    }

    @Generated(hash = 1848928493)
    public WordTitle() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public Long getTimeCreate() {
        return timeCreate;
    }

    public void setTimeCreate(Long timeCreate) {
        this.timeCreate = timeCreate;
    }
}
