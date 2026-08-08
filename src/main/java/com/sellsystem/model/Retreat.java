package com.sellsystem.model;

import java.math.BigDecimal;

/**
 * 退货记录表 — retreat
 */
public class Retreat {
    private int 退货编号;
    private String 厂商;
    private String 商品名;
    private String 型号;
    private BigDecimal 单价;
    private int 数量;
    private BigDecimal 总金额;
    private int 退货年;
    private int 退货月;
    private int 退货日;
    private int 业务员编号;

    public Retreat() {}

    public int get退货编号() { return 退货编号; }
    public void set退货编号(int 退货编号) { this.退货编号 = 退货编号; }

    public String get厂商() { return 厂商; }
    public void set厂商(String 厂商) { this.厂商 = 厂商; }

    public String get商品名() { return 商品名; }
    public void set商品名(String 商品名) { this.商品名 = 商品名; }

    public String get型号() { return 型号; }
    public void set型号(String 型号) { this.型号 = 型号; }

    public BigDecimal get单价() { return 单价; }
    public void set单价(BigDecimal 单价) { this.单价 = 单价; }

    public int get数量() { return 数量; }
    public void set数量(int 数量) { this.数量 = 数量; }

    public BigDecimal get总金额() { return 总金额; }
    public void set总金额(BigDecimal 总金额) { this.总金额 = 总金额; }

    public int get退货年() { return 退货年; }
    public void set退货年(int 退货年) { this.退货年 = 退货年; }

    public int get退货月() { return 退货月; }
    public void set退货月(int 退货月) { this.退货月 = 退货月; }

    public int get退货日() { return 退货日; }
    public void set退货日(int 退货日) { this.退货日 = 退货日; }

    public int get业务员编号() { return 业务员编号; }
    public void set业务员编号(int 业务员编号) { this.业务员编号 = 业务员编号; }
}
