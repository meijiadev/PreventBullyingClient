package com.mj.preventbullying.client.foldtree;

import androidx.annotation.NonNull;

/**
 * Create by MJ on 2023/12/21.
 * Describe :
 */

public class TreeModel extends BaseModel<TreeModel> {
    private Boolean localFlag;
    private String type;

    private int weight;

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setLocalFlag(Boolean localFlag) {
        this.localFlag = localFlag;
    }

    public Boolean getLocalFlag() {
        return localFlag;
    }

    @Override
    public String toString() {
        return "TreeModel{" +
                "localFlag=" + localFlag +
                ", type='" + type + '\'' +
                ", weight=" + weight +
                "isOpen=" + isOpen() +
                ", parentId='" + getParentId() + '\'' +
                ", id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", leave=" + getLeave() +
                '}';
    }
}
