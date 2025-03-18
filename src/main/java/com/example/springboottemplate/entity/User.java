package com.example.springboottemplate.entity;

public class User {
    private int id; //id
    private String userName; //用户名
    private String realName; //真实姓名
    private int sex; //性别 0女 1男
    private String phone; //手机号码
    private String address; //收货地址
    private String birthday; //生日
    private String registDate; //注册日期
    private String email; //邮箱
    private String identity; //会员等级 1基础用户 2vip会员 3管理员

    public User() {
    }

    public User(int id, String userName, String realName, int sex, String phone, String address, String birthday, String registDate, String email, String identity) {
        this.id = id;
        this.userName = userName;
        this.realName = realName;
        this.sex = sex;
        this.phone = phone;
        this.address = address;
        this.birthday = birthday;
        this.registDate = registDate;
        this.email = email;
        this.identity = identity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getRegistDate() {
        return registDate;
    }

    public void setRegistDate(String registDate) {
        this.registDate = registDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", realName='" + realName + '\'' +
                ", sex=" + sex +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", birthday='" + birthday + '\'' +
                ", registDate='" + registDate + '\'' +
                ", email='" + email + '\'' +
                ", identity='" + identity + '\'' +
                '}';
    }
}
