package com.example.springboottemplate.entity;

public class EquUseLog {
    private int id; //自增id
    private int userId; //操作用户的id
    private int equId; //操作的设备id
    private String createdTime; //设备操作时间
    private String remark; //备注

    public EquUseLog() {
    }

    public EquUseLog(int id, int userId, int equId, String createdTime, String remark) {
        this.id = id;
        this.userId = userId;
        this.equId = equId;
        this.createdTime = createdTime;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEquId() {
        return equId;
    }

    public void setEquId(int equId) {
        this.equId = equId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "EquUseLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", equId=" + equId +
                ", createdTime='" + createdTime + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
