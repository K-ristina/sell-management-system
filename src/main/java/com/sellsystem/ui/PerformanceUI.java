package com.sellsystem.ui;

import com.sellsystem.dao.EmployeeDAO;
import com.sellsystem.dao.SellDAO;
import com.sellsystem.model.Employee;
import com.sellsystem.model.Sell;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;

import static com.sellsystem.ui.Theme.*;

/**
 * 员工业绩界面 — Flip7 风格
 *
 * 功能：提供两种业绩查看方式：
 *   1) createAllPane()        — 全员销售业绩总览：以表格列出每位业务员的销售总额；
 *   2) createByEmployeePane() — 按员工查询：选中一名员工后，列出其全部销售明细，
 *                                并汇总显示合计销售金额。
 */
public class PerformanceUI {

    // 数据访问对象：负责销售记录与员工信息的查询
    private final SellDAO sellDAO = new SellDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    /** 全员业绩总览面板：表格列出所有业务员及其销售总额 */
    public VBox createAllPane() {
        // 根容器：纵向排列
        VBox root = new VBox(14);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("📈", "全员销售业绩总览"));

        // 业绩表格：三列（业务员编号 / 员工姓名 / 销售总额）
        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(400);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<String[], String> idCol = new TableColumn<>("业务员编号");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        idCol.setPrefWidth(120);

        TableColumn<String[], String> nameCol = new TableColumn<>("员工姓名");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        nameCol.setPrefWidth(200);

        TableColumn<String[], String> amtCol = new TableColumn<>("销售总额(元)");
        amtCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        amtCol.setPrefWidth(200);

        table.getColumns().addAll(idCol, nameCol, amtCol);

        // 数据加载逻辑：从 DAO 拉取全员业绩并填入表格
        Runnable load = () -> table.setItems(FXCollections.observableArrayList(sellDAO.getAllEmployeeSales()));
        load.run(); // 首次加载

        // 刷新按钮：点击后重新拉取数据
        Button refreshBtn = pillButton("🔄 刷新", Variant.OUTLINE);
        refreshBtn.setOnAction(e -> load.run());

        HBox topBox = new HBox(10, refreshBtn);
        topBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(topBox, table);
        return root;
    }

    /** 按员工编号查询面板：选择员工后展示其销售明细与合计金额 */
    public VBox createByEmployeePane() {
        // 根容器：纵向排列
        VBox root = new VBox(14);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("🔍", "按员工查询销售记录"));

        // 查询栏：员工下拉框 + 查询按钮
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // 员工下拉框（数据来自员工表）
        ComboBox<Employee> empCombo = new ComboBox<>();
        empCombo.setPromptText("请选择员工");
        empCombo.setPrefWidth(200);
        empCombo.setItems(FXCollections.observableArrayList(employeeDAO.getAll()));

        Button searchBtn = pillButton("🔍 查询", Variant.TEAL);
        searchBox.getChildren().addAll(fieldLabel("选择员工"), empCombo, searchBtn);

        // 员工信息标签：查询后显示选中员工的基本信息
        Label empInfoLabel = new Label();
        empInfoLabel.setFont(Font.font(FONT_FAMILY, 12));
        empInfoLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        empInfoLabel.setPadding(new Insets(4, 0, 4, 0));

        // 销售明细表：查询后列出该员工的全部销售记录
        TableView<Sell> detailTable = new TableView<>();
        detailTable.setPrefHeight(300);
        VBox.setVgrow(detailTable, Priority.ALWAYS);

        detailTable.getColumns().addAll(
                sellCol("销售编号", s -> String.valueOf(s.get商品编号()), 80),
                sellCol("生产厂商", Sell::get生产厂商, 100),
                sellCol("商品名", Sell::get商品名, 80),
                sellCol("型号", Sell::get型号, 80),
                sellCol("单价", s -> s.get单价() != null ? s.get单价().toString() : "", 80),
                sellCol("数量", s -> String.valueOf(s.get数量()), 60),
                sellCol("总金额", s -> s.get总金额() != null ? s.get总金额().toString() : "", 100),
                sellCol("最后一次销售日期", s -> s.get销售年() + "-" + s.get销售月() + "-" + s.get销售日(), 120)
        );

        // 合计金额 badge：查询后汇总该员工销售总额
        Label totalLabel = totalBadge("💰 合计销售金额", "0.00");

        // 查询按钮点击事件
        searchBtn.setOnAction(e -> {
            Employee emp = empCombo.getValue();
            if (emp == null) return; // 未选择员工则不处理

            // 1) 显示员工基本信息
            empInfoLabel.setText("👤 员工: " + emp.get员工姓名() + " (编号: " + emp.get员工编号() + ")"
                    + "  📞 电话: " + emp.get员工电话() + "  📍 地址: " + emp.get员工地址());

            // 2) 查询该员工的销售明细并填入表格
            java.util.List<Sell> sales = sellDAO.getSalesByEmployee(emp.get员工编号());
            detailTable.setItems(FXCollections.observableArrayList(sales));

            // 3) 用流式汇总计算销售总额（空金额按 0 处理）
            BigDecimal total = sales.stream()
                    .map(s -> s.get总金额() != null ? s.get总金额() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalLabel.setText("💰 合计销售金额：¥ " + total.toString());
            // 重新套用金色 badge 样式
            totalLabel.setStyle(
                "-fx-background-color: " + ACCENT_GOLD + ";" +
                "-fx-text-fill: " + PRIMARY_DARK + ";" +
                "-fx-padding: 8px 20px;" +
                "-fx-background-radius: " + RADIUS_ROUND + ";" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;"
            );
        });

        root.getChildren().addAll(searchBox, empInfoLabel, detailTable, totalLabel);
        return root;
    }

    /**
     * 明细表列工厂方法：根据取值函数 fn 和列宽 w 生成一个 Sell 表列。
     */
    private static TableColumn<Sell, String> sellCol(String t, java.util.function.Function<Sell, String> fn, int w) {
        TableColumn<Sell, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    /**
     * 表单字段左侧的标签工厂方法：统一风格生成加粗深青色小标签。
     */
    private static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        lbl.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        return lbl;
    }
}
