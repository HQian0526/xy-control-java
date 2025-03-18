package com.example.springboottemplate.entity;

public class Desk {
    private int id; //自增id
    private int seatId; //桌台/座位id
    private String seatName; //桌台/座位名
    private int storeId; //所属店铺id
    private String remark; //备注

    public Desk() {
    }

    public Desk(int id, int seatId, String seatName, int storeId, String remark) {
        this.id = id;
        this.seatId = seatId;
        this.seatName = seatName;
        this.storeId = storeId;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Desk{" +
                "id=" + id +
                ", seatId=" + seatId +
                ", seatName='" + seatName + '\'' +
                ", storeId=" + storeId +
                ", remark='" + remark + '\'' +
                '}';
    }
}
