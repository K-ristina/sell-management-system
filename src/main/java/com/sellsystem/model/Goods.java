package com.sellsystem.model;

import java.math.BigDecimal;

/**
 * 现存货品/进货记录表 — goods（库存核心表）
 */
public class Goods {
    private int 商品编号;
    private String 生产厂商;
    private String 商品名;
    private String 型号;
    private BigDecimal 单价;
    private BigDecimal 数量;
    private BigDecimal 总金额;
    private int 进货年;
    private int 进货月;
    private int 进货日;
    private int 业务员编号;

    public Goods() {}

    // ---- getter / setter ----

    public int get商品编号() { return 商品编号; }
    public void set商品编号(int 商品编号) { this.商品编号 = 商品编号; }

    public String get生产厂商() { return 生产厂商; }
    public void set生产厂商(String 生产厂商) { this.生产厂商 = 生产厂商; }

    public String get商品名() { return 商品名; }
    public void set商品名(String 商品名) { this.商品名 = 商品名; }

    public String get型号() { return 型号; }
    public void set型号(String 型号) { this.型号 = 型号; }

    public BigDecimal get单价() { return 单价; }
    public void set单价(BigDecimal 单价) { this.单价 = 单价; }

    public BigDecimal get数量() { return 数量; }
    public void set数量(BigDecimal 数量) { this.数量 = 数量; }

    public BigDecimal get总金额() { return 总金额; }
    public void set总金额(BigDecimal 总金额) { this.总金额 = 总金额; }

    public int get进货年() { return 进货年; }
    public void set进货年(int 进货年) { this.进货年 = 进货年; }

    public int get进货月() { return 进货月; }
    public void set进货月(int 进货月) { this.进货月 = 进货月; }

    public int get进货日() { return 进货日; }
    public void set进货日(int 进货日) { this.进货日 = 进货日; }

    public int get业务员编号() { return 业务员编号; }
    public void set业务员编号(int 业务员编号) { this.业务员编号 = 业务员编号; }
}
