package com.example.springboottemplate.entity;

public class Store {
    private int id; //id
    private int storeId; //商户id
    private String name; //商户名
    private String address; //商户地址
    private int userId; //关联user表的id
    private int storeType; //商户类型 1永久 2租用
    private String storeTime; //商户到期时间
    private int stauts; //商户状态 1正常 2异常 3冻结 4注销
    private String identityName; //法人姓名
    private String identityCompanyName; //工商主体名称
    private String identityNo; //法人身份证号
    private String identityFontImg; //法人身份证正面base64
    private String identityBackImg; //法人身份证反面base64
    private String identityPhone; //法人手机号
    private String identityEmail; //法人邮箱
    private String remark; //备注

    public Store() {
    }

    public Store(int id, int storeId, String name, String address, int userId, int storeType, String storeTime, int stauts, String identityName, String identityCompanyName, String identityNo, String identityFontImg, String identityBackImg, String identityPhone, String identityEmail, String remark) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.userId = userId;
        this.storeType = storeType;
        this.storeTime = storeTime;
        this.stauts = stauts;
        this.identityName = identityName;
        this.identityCompanyName = identityCompanyName;
        this.identityNo = identityNo;
        this.identityFontImg = identityFontImg;
        this.identityBackImg = identityBackImg;
        this.identityPhone = identityPhone;
        this.identityEmail = identityEmail;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStoreType() {
        return storeType;
    }

    public void setStoreType(int storeType) {
        this.storeType = storeType;
    }

    public String getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(String storeTime) {
        this.storeTime = storeTime;
    }

    public int getStauts() {
        return stauts;
    }

    public void setStauts(int stauts) {
        this.stauts = stauts;
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getIdentityCompanyName() {
        return identityCompanyName;
    }

    public void setIdentityCompanyName(String identityCompanyName) {
        this.identityCompanyName = identityCompanyName;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getIdentityFontImg() {
        return identityFontImg;
    }

    public void setIdentityFontImg(String identityFontImg) {
        this.identityFontImg = identityFontImg;
    }

    public String getIdentityBackImg() {
        return identityBackImg;
    }

    public void setIdentityBackImg(String identityBackImg) {
        this.identityBackImg = identityBackImg;
    }

    public String getIdentityPhone() {
        return identityPhone;
    }

    public void setIdentityPhone(String identityPhone) {
        this.identityPhone = identityPhone;
    }

    public String getIdentityEmail() {
        return identityEmail;
    }

    public void setIdentityEmail(String identityEmail) {
        this.identityEmail = identityEmail;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", storeId=" + storeId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", userId=" + userId +
                ", storeType=" + storeType +
                ", storeTime='" + storeTime + '\'' +
                ", stauts=" + stauts +
                ", identityName='" + identityName + '\'' +
                ", identityCompanyName='" + identityCompanyName + '\'' +
                ", identityNo='" + identityNo + '\'' +
                ", identityFontImg='" + identityFontImg + '\'' +
                ", identityBackImg='" + identityBackImg + '\'' +
                ", identityPhone='" + identityPhone + '\'' +
                ", identityEmail='" + identityEmail + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
