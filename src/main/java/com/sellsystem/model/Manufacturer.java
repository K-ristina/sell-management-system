package com.sellsystem.model;

/**
 * 进货厂商表 — manufacturer
 */
public class Manufacturer {
    private int 厂商编号;
    private String 厂商名称;
    private String 法人代表;
    private String 电话;
    private String 厂商地址;

    public Manufacturer() {}

    public Manufacturer(int 厂商编号, String 厂商名称, String 法人代表, String 电话, String 厂商地址) {
        this.厂商编号 = 厂商编号;
        this.厂商名称 = 厂商名称;
        this.法人代表 = 法人代表;
        this.电话 = 电话;
        this.厂商地址 = 厂商地址;
    }

    public int get厂商编号() { return 厂商编号; }
    public void set厂商编号(int 厂商编号) { this.厂商编号 = 厂商编号; }

    public String get厂商名称() { return 厂商名称; }
    public void set厂商名称(String 厂商名称) { this.厂商名称 = 厂商名称; }

    public String get法人代表() { return 法人代表; }
    public void set法人代表(String 法人代表) { this.法人代表 = 法人代表; }

    public String get电话() { return 电话; }
    public void set电话(String 电话) { this.电话 = 电话; }

    public String get厂商地址() { return 厂商地址; }
    public void set厂商地址(String 厂商地址) { this.厂商地址 = 厂商地址; }

    @Override
    public String toString() {
        return 厂商名称;
    }
}
