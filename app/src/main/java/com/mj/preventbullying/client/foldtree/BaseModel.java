package com.mj.preventbullying.client.foldtree;

import java.util.List;


public class BaseModel<E extends BaseModel> {
    /**
     * 是否展开
     */
    private boolean isOpen;

    /**
     * 上一级父id
     */
    private String parentId;

    /**
     * 自己的id
     */
    private String id;
    /**
     * item名
     */
    private String name;


    /**
     * 标记第几级
     */
    private int leave;

    private List<E>children;

    public int getLeave() {
        return leave;
    }

    public void setLeave(int leave) {
        this.leave = leave;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<E> getChildren() {
        return children;
    }

    public void setChildren(List<E> children) {
        this.children = children;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }


    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BaseModel{" +
                "isOpen=" + isOpen +
                ", parentId='" + parentId + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", leave=" + leave +
                ", children=" + children +
                '}';
    }
}
