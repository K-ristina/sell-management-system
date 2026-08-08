package com.sellsystem.model;

/**
 * 员工信息表 — employee
 */
public class Employee {
    private int 员工编号;
    private String 员工姓名;
    private String 员工电话;
    private String 员工地址;

    public Employee() {}

    public Employee(int 员工编号, String 员工姓名, String 员工电话, String 员工地址) {
        this.员工编号 = 员工编号;
        this.员工姓名 = 员工姓名;
        this.员工电话 = 员工电话;
        this.员工地址 = 员工地址;
    }

    public int get员工编号() { return 员工编号; }
    public void set员工编号(int 员工编号) { this.员工编号 = 员工编号; }

    public String get员工姓名() { return 员工姓名; }
    public void set员工姓名(String 员工姓名) { this.员工姓名 = 员工姓名; }

    public String get员工电话() { return 员工电话; }
    public void set员工电话(String 员工电话) { this.员工电话 = 员工电话; }

    public String get员工地址() { return 员工地址; }
    public void set员工地址(String 员工地址) { this.员工地址 = 员工地址; }

    @Override
    public String toString() {
        return 员工姓名;
    }
}
