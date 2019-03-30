package com.cjenglish.db;

import org.greenrobot.greendao.annotation.Entity;

import java.io.Serializable;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by cc on 2019/3/26.
 */
@Entity
public class WordItem implements Serializable {
    @Id(autoincrement = true)
    private Long id;
    private Long pid;
    private String name;
    private String content;

    /**
     * 用于排序
     */
    @NotNull
    private Long sortline;

    private Long createTime;

    @Generated(hash = 506440113)
    public WordItem(Long id, Long pid, String name, String content,
            @NotNull Long sortline, Long createTime) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.content = content;
        this.sortline = sortline;
        this.createTime = createTime;
    }

    @Generated(hash = 1382998129)
    public WordItem() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSortline() {
        return sortline;
    }

    public void setSortline(Long sortline) {
        this.sortline = sortline;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
