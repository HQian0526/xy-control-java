package com.example.springboottemplate.entity;

public class CardBuyLog {
    private int id; //自增id
    private int userId; //购买用户id
    private int cardId; //购买套餐id
    private String createdTime; //套餐购买时间
    private int status; //订单状态 1已付款 2已退款 3退款中
    private int ticketStatus; //是否已开票 1否 2是

    public CardBuyLog() {
    }

    public CardBuyLog(int id, int userId, int cardId, String createdTime, int status, int ticketStatus) {
        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.createdTime = createdTime;
        this.status = status;
        this.ticketStatus = ticketStatus;
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

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(int ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    @Override
    public String toString() {
        return "CardBuyLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", cardId=" + cardId +
                ", createdTime='" + createdTime + '\'' +
                ", status=" + status +
                ", ticketStatus=" + ticketStatus +
                '}';
    }
}
