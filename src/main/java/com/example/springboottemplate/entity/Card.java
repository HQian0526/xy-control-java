package com.example.springboottemplate.entity;

public class Card {
    private int id;
    private int cardId; //套餐id
    private String name; //套餐名
    private String price; //套餐价格
    private String intro; //套餐详情介绍
    private int type; //套餐类型 1小时卡 2次卡 3天卡 4周卡 5月卡 6季卡 7半年卡 8年卡 9其他
    private String createdTime; //套餐创建时间
    private int createdBy; //套餐创建人
    private int status; //套餐状态 1启用 2停用 3删除
    private int effectDay; //套餐有效天数
    private String startTime; //套餐开始时间
    private String endTime; //套餐结束时间
    private String remark; //备注

    public Card() {
    }

    public Card(int id, int cardId, String name, String price, String intro, int type, String createdTime, int createdBy, int status, int effectDay, String startTime, String endTime, String remark) {
        this.id = id;
        this.cardId = cardId;
        this.name = name;
        this.price = price;
        this.intro = intro;
        this.type = type;
        this.createdTime = createdTime;
        this.createdBy = createdBy;
        this.status = status;
        this.effectDay = effectDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getEffectDay() {
        return effectDay;
    }

    public void setEffectDay(int effectDay) {
        this.effectDay = effectDay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", intro='" + intro + '\'' +
                ", type=" + type +
                ", createdTime='" + createdTime + '\'' +
                ", createdBy=" + createdBy +
                ", status=" + status +
                ", effectDay=" + effectDay +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
