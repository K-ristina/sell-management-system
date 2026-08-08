package com.sellsystem.model;

/**
 * 系统用户表 — userdb
 */
public class User {
    private int 用户编号;
    private String 用户名;
    private String 密码;

    public User() {}

    public User(int 用户编号, String 用户名, String 密码) {
        this.用户编号 = 用户编号;
        this.用户名 = 用户名;
        this.密码 = 密码;
    }

    public int get用户编号() { return 用户编号; }
    public void set用户编号(int 用户编号) { this.用户编号 = 用户编号; }

    public String get用户名() { return 用户名; }
    public void set用户名(String 用户名) { this.用户名 = 用户名; }

    public String get密码() { return 密码; }
    public void set密码(String 密码) { this.密码 = 密码; }

    @Override
    public String toString() {
        return "User{" + "用户编号=" + 用户编号 + ", 用户名='" + 用户名 + '\'' + '}';
    }
}
