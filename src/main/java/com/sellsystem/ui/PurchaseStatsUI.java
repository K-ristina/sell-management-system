package com.sellsystem.ui;

import com.sellsystem.dao.GoodsDAO;
import com.sellsystem.model.Goods;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.IsoFields;

import static com.sellsystem.ui.Theme.*;

/**
 * 进货统计界面 — Flip7 风格
 *
 * 功能：按不同时间段（今日 / 本月 / 本季度 / 本年度）统计进货情况。
 * 每个面板包含三部分：
 *   1) 进货明细表格：列出该时间段内每笔进货记录；
 *   2) 按厂商汇总表格：统计各生产厂商的合计进货金额；
 *   3) 底部金色 badge：展示该时间段进货总金额。
 * 所有数据由 GoodsDAO 按年/月起止区间查询得到。
 */
public class PurchaseStatsUI {

    // 静态数据访问对象：负责商品（进货）表的统计查询
    private static final GoodsDAO goodsDAO = new GoodsDAO();

    /** 今日进货统计面板：按 当年/当月/当日 区间查询 */
    public static VBox createTodayPane() {
        LocalDate today = LocalDate.now();
        return createStatsPane("📥 今日进货统计",
                goodsDAO.getStatsByPeriod(today.getYear(), today.getMonthValue(), today.getMonthValue(), today.getDayOfMonth()),
                goodsDAO.getSummaryByManufacturer(today.getYear(), today.getMonthValue(), today.getMonthValue(), today.getDayOfMonth()),
                goodsDAO.getTotalAmount(today.getYear(), today.getMonthValue(), today.getMonthValue(), today.getDayOfMonth()));
    }

    /** 本月进货统计面板：按 当年/当月 全月区间查询（day 传 null 表示不限日） */
    public static VBox createMonthPane() {
        LocalDate today = LocalDate.now();
        return createStatsPane("📥 本月进货统计",
                goodsDAO.getStatsByPeriod(today.getYear(), today.getMonthValue(), today.getMonthValue(), null),
                goodsDAO.getSummaryByManufacturer(today.getYear(), today.getMonthValue(), today.getMonthValue(), null),
                goodsDAO.getTotalAmount(today.getYear(), today.getMonthValue(), today.getMonthValue(), null));
    }

    /** 本季度进货统计面板：计算当前季度对应的起止月份后查询 */
    public static VBox createQuarterPane() {
        LocalDate today = LocalDate.now();
        // 用 ISO 季度字段计算当前属于第几季度，并换算起止月份
        int quarter = today.get(IsoFields.QUARTER_OF_YEAR);
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;
        return createStatsPane("📥 第" + quarter + "季度进货统计",
                goodsDAO.getStatsByPeriod(today.getYear(), startMonth, endMonth, null),
                goodsDAO.getSummaryByManufacturer(today.getYear(), startMonth, endMonth, null),
                goodsDAO.getTotalAmount(today.getYear(), startMonth, endMonth, null));
    }

    /** 本年度进货统计面板：按 1~12 月整年区间查询 */
    public static VBox createYearPane() {
        LocalDate today = LocalDate.now();
        return createStatsPane("📥 本年度进货统计",
                goodsDAO.getStatsByPeriod(today.getYear(), 1, 12, null),
                goodsDAO.getSummaryByManufacturer(today.getYear(), 1, 12, null),
                goodsDAO.getTotalAmount(today.getYear(), 1, 12, null));
    }

    /**
     * 统计面板的公共构建方法：
     * 组装「明细表格 + 厂商汇总表格 + 总金额 badge」三段式布局。
     * @param title   面板标题
     * @param details 该时间段的进货明细记录列表
     * @param summary 按厂商汇总的二维数组（[厂商名, 合计金额]）
     * @param total   该时间段进货总金额
     */
    private static VBox createStatsPane(String title, java.util.List<Goods> details,
                                        java.util.List<String[]> summary, BigDecimal total) {
        // 根容器：纵向排列
        VBox root = new VBox(14);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 标题
        root.getChildren().add(sectionTitle("📥", title.replace("📥 ", "")));

        // 明细表格
        Label detailLabel = new Label("进货明细（按厂商和金额排序）");
        detailLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        detailLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        // 进货明细表：列出编号/厂商/商品名/型号/单价/数量/总金额/日期/业务员
        TableView<Goods> detailTable = new TableView<>();
        detailTable.setPrefHeight(250);

        detailTable.getColumns().addAll(
                gCol("编号", g -> String.valueOf(g.get商品编号()), 70),
                gCol("生产厂商", Goods::get生产厂商, 100),
                gCol("商品名", Goods::get商品名, 80),
                gCol("型号", Goods::get型号, 80),
                gCol("单价", g -> g.get单价() != null ? g.get单价().toString() : "", 80),
                gCol("数量", g -> g.get数量() != null ? g.get数量().toString() : "", 60),
                gCol("总金额", g -> g.get总金额() != null ? g.get总金额().toString() : "", 100),
                gCol("进货日期", g -> g.get进货年() + "-" + g.get进货月() + "-" + g.get进货日(), 100),
                gCol("业务员", g -> String.valueOf(g.get业务员编号()), 80)
        );
        detailTable.setItems(FXCollections.observableArrayList(details));

        root.getChildren().addAll(detailLabel, detailTable);
        root.getChildren().add(dashedSeparator());

        // 汇总表格
        Label sumLabel = new Label("按厂商汇总");
        sumLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        sumLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        // 汇总表：两列（厂商名称 / 合计进货金额）
        TableView<String[]> summaryTable = new TableView<>();
        summaryTable.setPrefHeight(180);

        TableColumn<String[], String> mfrCol = new TableColumn<>("厂商名称");
        mfrCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        mfrCol.setPrefWidth(250);
        TableColumn<String[], String> amtCol = new TableColumn<>("合计进货金额(元)");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        amtCol.setPrefWidth(200);
        summaryTable.getColumns().addAll(mfrCol, amtCol);
        summaryTable.setItems(FXCollections.observableArrayList(summary));

        root.getChildren().addAll(sumLabel, summaryTable);

        // 总金额 badge
        root.getChildren().add(totalBadge("📦 总进货金额", total != null ? total.toString() : "0.00"));

        return root;
    }

    /**
     * 明细表列工厂方法：根据取值函数 fn 和列宽 w 生成一个 Goods 表列，
     * 用字符串属性包装实际单元格值。
     */
    private static TableColumn<Goods, String> gCol(String t, java.util.function.Function<Goods, String> fn, int w) {
        TableColumn<Goods, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }
}
