package com.example.springboottemplate.entity;

public class Equ {
    private int id; //自增id
    private int equId; //设备id
    private String equName; //设备名称
    private int type; //设备类型 1照明 2门禁 3其他
    private int bindDeskId; //绑定的桌台
    private int bindStoreId; //绑定的店铺
    private String equCode; //设备控制的三元码
    private String equStatus; //设备状态
    private String createdTime; //设备创建时间
    private String createdBy; //创建人id
    private String remark; //备注

    public Equ() {
    }

    public Equ(int id, int equId, String equName, int type, int bindDeskId, int bindStoreId, String equCode, String equStatus, String createdTime, String createdBy, String remark) {
        this.id = id;
        this.equId = equId;
        this.equName = equName;
        this.type = type;
        this.bindDeskId = bindDeskId;
        this.bindStoreId = bindStoreId;
        this.equCode = equCode;
        this.equStatus = equStatus;
        this.createdTime = createdTime;
        this.createdBy = createdBy;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquId() {
        return equId;
    }

    public void setEquId(int equId) {
        this.equId = equId;
    }

    public String getEquName() {
        return equName;
    }

    public void setEquName(String equName) {
        this.equName = equName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBindDeskId() {
        return bindDeskId;
    }

    public void setBindDeskId(int bindDeskId) {
        this.bindDeskId = bindDeskId;
    }

    public int getBindStoreId() {
        return bindStoreId;
    }

    public void setBindStoreId(int bindStoreId) {
        this.bindStoreId = bindStoreId;
    }

    public String getEquCode() {
        return equCode;
    }

    public void setEquCode(String equCode) {
        this.equCode = equCode;
    }

    public String getEquStatus() {
        return equStatus;
    }

    public void setEquStatus(String equStatus) {
        this.equStatus = equStatus;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Equ{" +
                "id=" + id +
                ", equId=" + equId +
                ", equName='" + equName + '\'' +
                ", type=" + type +
                ", bindDeskId=" + bindDeskId +
                ", bindStoreId=" + bindStoreId +
                ", equCode='" + equCode + '\'' +
                ", equStatus='" + equStatus + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
