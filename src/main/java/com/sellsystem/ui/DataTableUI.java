package com.sellsystem.ui;

import com.sellsystem.dao.*;
import com.sellsystem.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.sellsystem.ui.Theme.*;

/**
 * 数据表查看界面
 *
 * 功能：提供对系统中 5 张核心表的查看与维护：
 *   进货表 / 销售表 / 退货表（只读，带刷新按钮）；
 *   员工表 / 厂商表（可增删改，通过模态对话框编辑）。
 * 所有面板均为静态工厂方法，供主界面以 Tab 形式嵌入。
 */
public class DataTableUI {

    // ═══════════════════════════════════════════════════════
    // 进货表（只读）
    // ═══════════════════════════════════════════════════════

    /**
     * 进货表 / 库存表面板：以表格展示全部进货记录（只读），
     * 顶部提供刷新按钮可重新拉取数据库数据。
     */
    public static VBox createGoodsTablePane() {
        GoodsDAO dao = new GoodsDAO();
        // 根容器：纵向排列
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("📦", "进货表 / 库存表"));

        // 进货表：列出编号/厂商/商品名/型号/单价/数量/总金额/进货日期/业务员
        TableView<Goods> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
                gCol("商品编号", g -> str(g.get商品编号()), 80),
                gCol("生产厂商", Goods::get生产厂商, 100),
                gCol("商品名", Goods::get商品名, 80),
                gCol("型号", Goods::get型号, 80),
                gCol("单价", g -> g.get单价() != null ? g.get单价().toString() : "", 80),
                gCol("数量", g -> g.get数量() != null ? g.get数量().toString() : "", 60),
                gCol("总金额", g -> g.get总金额() != null ? g.get总金额().toString() : "", 100),
                gCol("进货日期", g -> g.get进货年() + "-" + g.get进货月() + "-" + g.get进货日(), 100),
                gCol("业务员编号", g -> str(g.get业务员编号()), 90)
        );

        // 刷新按钮：重新查询并填充表格
        Button refreshBtn = pillButton("🔄 刷新", Variant.OUTLINE);
        refreshBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(dao.getAll())));
        table.setItems(FXCollections.observableArrayList(dao.getAll()));

        root.getChildren().addAll(refreshBtn, table);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    // 销售表（只读）
    // ═══════════════════════════════════════════════════════

    /**
     * 销售表面板：以表格展示全部销售记录（只读），带刷新按钮。
     */
    public static VBox createSellTablePane() {
        SellDAO dao = new SellDAO();
        // 根容器：纵向排列
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("💵", "销售表"));

        // 销售表：列出编号/厂商/商品名/型号/单价/数量/总金额/销售日期/业务员
        TableView<Sell> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
                sCol("销售编号", s -> str(s.get商品编号()), 80),
                sCol("生产厂商", Sell::get生产厂商, 100),
                sCol("商品名", Sell::get商品名, 80),
                sCol("型号", Sell::get型号, 80),
                sCol("单价", s -> s.get单价() != null ? s.get单价().toString() : "", 80),
                sCol("数量", s -> str(s.get数量()), 60),
                sCol("总金额", s -> s.get总金额() != null ? s.get总金额().toString() : "", 100),
                sCol("销售日期", s -> s.get销售年() + "-" + s.get销售月() + "-" + s.get销售日(), 100),
                sCol("业务员编号", s -> str(s.get业务员编号()), 90)
        );

        // 刷新按钮：重新查询并填充表格
        Button refreshBtn = pillButton("🔄 刷新", Variant.OUTLINE);
        refreshBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(dao.getAll())));
        table.setItems(FXCollections.observableArrayList(dao.getAll()));

        root.getChildren().addAll(refreshBtn, table);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    // 退货表（只读）
    // ═══════════════════════════════════════════════════════

    /**
     * 退货表面板：以表格展示全部退货记录（只读），带刷新按钮。
     */
    public static VBox createRetreatTablePane() {
        RetreatDAO dao = new RetreatDAO();
        // 根容器：纵向排列
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("↩", "退货表"));


        // 退货表：列出编号/厂商/商品名/型号/单价/数量/总金额/退货日期/业务员
        TableView<Retreat> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);
        table.getColumns().addAll(
                rCol("退货编号", r -> str(r.get退货编号()), 80),
                rCol("厂商", Retreat::get厂商, 100),
                rCol("商品名", Retreat::get商品名, 80),
                rCol("型号", Retreat::get型号, 80),
                rCol("单价", r -> r.get单价() != null ? r.get单价().toString() : "", 80),
                rCol("数量", r -> str(r.get数量()), 60),
                rCol("总金额", r -> r.get总金额() != null ? r.get总金额().toString() : "", 100),
                rCol("退货日期", r -> r.get退货年() + "-" + r.get退货月() + "-" + r.get退货日(), 100),
                rCol("业务员编号", r -> str(r.get业务员编号()), 90)
        );

        // 刷新按钮：重新查询并填充表格
        Button refreshBtn = pillButton("🔄 刷新", Variant.OUTLINE);
        refreshBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(dao.getAll())));
        table.setItems(FXCollections.observableArrayList(dao.getAll()));

        root.getChildren().addAll(refreshBtn, table);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    // 员工表（可编辑）
    // ═══════════════════════════════════════════════════════

    /**
     * 员工表面板：可对员工进行新增、编辑、删除维护。
     * 先选中表格行再点击对应按钮，通过模态对话框完成增改。
     */
    public static VBox createEmployeeTablePane() {
        EmployeeDAO dao = new EmployeeDAO();
        // 根容器：纵向排列
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("👥", "员工表 "));

        // 员工表：编号 / 姓名 / 电话 / 地址
        TableView<Employee> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Employee, String> c1 = new TableColumn<>("员工编号");
        c1.setCellValueFactory(d -> new SimpleStringProperty(str(d.getValue().get员工编号())));
        c1.setPrefWidth(80);

        TableColumn<Employee, String> c2 = new TableColumn<>("员工姓名");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get员工姓名()));
        c2.setPrefWidth(120);

        TableColumn<Employee, String> c3 = new TableColumn<>("员工电话");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get员工电话()));
        c3.setPrefWidth(150);

        TableColumn<Employee, String> c4 = new TableColumn<>("员工地址");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get员工地址()));
        c4.setPrefWidth(300);

        table.getColumns().addAll(c1, c2, c3, c4);

        // 数据重载逻辑：重新查询员工表并填充
        Runnable reload = () -> table.setItems(FXCollections.observableArrayList(dao.getAll()));

        // 新增员工按钮：打开空表单对话框
        Button addBtn = pillButton("＋ 新增员工", Variant.PRIMARY);
        addBtn.setOnAction(e -> showEmployeeDialog(null, dao, reload));

        // 编辑选中按钮：将选中行数据带进对话框修改
        Button editBtn = pillButton("✎ 编辑选中", Variant.TEAL);
        editBtn.setOnAction(e -> {
            Employee sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warnAlert("请先在表格中选中一条员工记录"); return; }
            showEmployeeDialog(sel, dao, reload);
        });

        // 删除选中按钮：二次确认后删除该员工
        Button delBtn = pillButton("✕ 删除选中", Variant.CORAL);
        delBtn.setOnAction(e -> {
            Employee sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warnAlert("请先在表格中选中一条员工记录"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "确定删除员工 " + sel.get员工姓名() + " 吗？", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES && dao.delete(sel.get员工编号())) reload.run();
            });
        });

        // 操作按钮横向排列
        HBox btnBox = new HBox(10, addBtn, editBtn, delBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        reload.run();
        root.getChildren().addAll(btnBox, table);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    // 厂商表（可编辑）
    // ═══════════════════════════════════════════════════════

    /**
     * 厂商表面板：可对厂商进行新增、编辑、删除维护。
     * 与员工表结构类似，字段为编号/名称/法人代表/电话/地址。
     */
    public static VBox createManufacturerTablePane() {
        ManufacturerDAO dao = new ManufacturerDAO();
        // 根容器：纵向排列
        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("🏭", "厂商表"));

        // 厂商表：编号 / 名称 / 法人代表 / 电话 / 地址
        TableView<Manufacturer> table = new TableView<>();
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Manufacturer, String> c1 = new TableColumn<>("厂商编号");
        c1.setCellValueFactory(d -> new SimpleStringProperty(str(d.getValue().get厂商编号())));
        c1.setPrefWidth(80);

        TableColumn<Manufacturer, String> c2 = new TableColumn<>("厂商名称");
        c2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get厂商名称()));
        c2.setPrefWidth(150);

        TableColumn<Manufacturer, String> c3 = new TableColumn<>("法人代表");
        c3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get法人代表()));
        c3.setPrefWidth(100);

        TableColumn<Manufacturer, String> c4 = new TableColumn<>("电话");
        c4.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get电话()));
        c4.setPrefWidth(150);

        TableColumn<Manufacturer, String> c5 = new TableColumn<>("厂商地址");
        c5.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get厂商地址()));
        c5.setPrefWidth(250);

        table.getColumns().addAll(c1, c2, c3, c4, c5);

        // 数据重载逻辑：重新查询厂商表并填充
        Runnable reload = () -> table.setItems(FXCollections.observableArrayList(dao.getAll()));

        // 新增厂商按钮：打开空表单对话框
        Button addBtn = pillButton("＋ 新增厂商", Variant.PRIMARY);
        addBtn.setOnAction(e -> showManufacturerDialog(null, dao, reload));

        // 编辑选中按钮：将选中行数据带进对话框修改
        Button editBtn = pillButton("✎ 编辑选中", Variant.TEAL);
        editBtn.setOnAction(e -> {
            Manufacturer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warnAlert("请先在表格中选中一条厂商记录"); return; }
            showManufacturerDialog(sel, dao, reload);
        });

        // 删除选中按钮：二次确认后删除该厂商
        Button delBtn = pillButton("✕ 删除选中", Variant.CORAL);
        delBtn.setOnAction(e -> {
            Manufacturer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { warnAlert("请先在表格中选中一条厂商记录"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "确定删除厂商 " + sel.get厂商名称() + " 吗？", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES && dao.delete(sel.get厂商编号())) reload.run();
            });
        });

        // 操作按钮横向排列
        HBox btnBox = new HBox(10, addBtn, editBtn, delBtn);
        btnBox.setAlignment(Pos.CENTER_LEFT);

        reload.run();
        root.getChildren().addAll(btnBox, table);
        return root;
    }

    // ═══════════════════════════════════════════════════════
    // 对话框
    // ═══════════════════════════════════════════════════════

    /**
     * 员工新增/编辑共用对话框。
     * @param emp       当前编辑的员工；为 null 表示新增
     * @param dao       员工数据访问对象
     * @param onSuccess 保存成功后的回调（用于刷新表格）
     */
    private static void showEmployeeDialog(Employee emp, EmployeeDAO dao, Runnable onSuccess) {
        boolean isNew = (emp == null); // 依据传入对象是否为 null 判断新增/编辑
        // 创建模态对话框
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(isNew ? "新增员工" : "编辑员工");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        // 表单：姓名 / 电话 / 地址（编辑时预填原值）
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField(isNew ? "" : emp.get员工姓名());
        TextField phoneField = new TextField(isNew ? "" : emp.get员工电话());
        TextField addrField = new TextField(isNew ? "" : emp.get员工地址());

        form.add(fieldLabel("姓名"), 0, 0); form.add(nameField, 1, 0);
        form.add(fieldLabel("电话"), 0, 1); form.add(phoneField, 1, 1);
        form.add(fieldLabel("地址"), 0, 2); form.add(addrField, 1, 2);

        // 保存按钮：新增则 insert，编辑则 update，成功后回调并关闭
        Button saveBtn = pillButton("💾 保存", Variant.PRIMARY);
        saveBtn.setOnAction(evt -> {
            if (isNew) {
                // 新增：构造新员工对象并插入数据库
                Employee newEmp = new Employee();
                newEmp.set员工姓名(nameField.getText().trim());
                newEmp.set员工电话(phoneField.getText().trim());
                newEmp.set员工地址(addrField.getText().trim());
                if (dao.insert(newEmp)) { onSuccess.run(); dialog.close(); }
            } else {
                // 编辑：用表单内容更新当前员工对象并写入数据库
                emp.set员工姓名(nameField.getText().trim());
                emp.set员工电话(phoneField.getText().trim());
                emp.set员工地址(addrField.getText().trim());
                if (dao.update(emp)) { onSuccess.run(); dialog.close(); }
            }
        });

        // 组装对话框界面
        VBox box = new VBox(15, form, saveBtn);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + SURFACE_CARD + ";");
        dialog.setScene(new javafx.scene.Scene(box, 420, 260));
        dialog.showAndWait();
    }

    /**
     * 厂商新增/编辑共用对话框。
     * @param mfr       当前编辑的厂商；为 null 表示新增
     * @param dao       厂商数据访问对象
     * @param onSuccess 保存成功后的回调（用于刷新表格）
     */
    private static void showManufacturerDialog(Manufacturer mfr, ManufacturerDAO dao, Runnable onSuccess) {
        boolean isNew = (mfr == null); // 依据传入对象是否为 null 判断新增/编辑
        // 创建模态对话框
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(isNew ? "新增厂商" : "编辑厂商");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        // 表单：厂商名称 / 法人代表 / 电话 / 地址（编辑时预填原值）
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setPadding(new Insets(20));

        TextField nameField = new TextField(isNew ? "" : mfr.get厂商名称());
        TextField repField = new TextField(isNew ? "" : mfr.get法人代表());
        TextField phoneField = new TextField(isNew ? "" : mfr.get电话());
        TextField addrField = new TextField(isNew ? "" : mfr.get厂商地址());

        form.add(fieldLabel("厂商名称"), 0, 0); form.add(nameField, 1, 0);
        form.add(fieldLabel("法人代表"), 0, 1); form.add(repField, 1, 1);
        form.add(fieldLabel("电话"), 0, 2);     form.add(phoneField, 1, 2);
        form.add(fieldLabel("地址"), 0, 3);     form.add(addrField, 1, 3);

        // 保存按钮：新增则 insert（编号取自下一个自增值），编辑则 update
        Button saveBtn = pillButton("💾 保存", Variant.PRIMARY);
        saveBtn.setOnAction(evt -> {
            if (isNew) {
                // 新增：构造新厂商对象并插入数据库（厂商编号由数据库自增生成）
                Manufacturer newMfr = new Manufacturer();
                newMfr.set厂商名称(nameField.getText().trim());
                newMfr.set法人代表(repField.getText().trim());
                newMfr.set电话(phoneField.getText().trim());
                newMfr.set厂商地址(addrField.getText().trim());
                if (dao.insert(newMfr)) { onSuccess.run(); dialog.close(); }
            } else {
                // 编辑：用表单内容更新当前厂商对象并写入数据库
                mfr.set厂商名称(nameField.getText().trim());
                mfr.set法人代表(repField.getText().trim());
                mfr.set电话(phoneField.getText().trim());
                mfr.set厂商地址(addrField.getText().trim());
                if (dao.update(mfr)) { onSuccess.run(); dialog.close(); }
            }
        });

        // 组装对话框界面
        VBox box = new VBox(15, form, saveBtn);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: " + SURFACE_CARD + ";");
        dialog.setScene(new javafx.scene.Scene(box, 420, 300));
        dialog.showAndWait();
    }

    // ═══════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════

    /** coral 警告卡片：左侧强调条 + 文字，用于展示警告信息 */
    private static HBox warnCard(String text) {
        Label label = new Label(text);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: " + CORAL_DARK + ";");
        label.setPadding(new Insets(10, 14, 10, 14));
        label.setMaxWidth(Double.MAX_VALUE);

        // 左侧 5px 强调竖条
        Pane bar = new Pane();
        bar.setMinWidth(5); bar.setPrefWidth(5); bar.setMaxWidth(5);
        bar.setStyle("-fx-background-color: " + CORAL + "; -fx-background-radius: 3px;");

        // 组装卡片并让文字占满剩余宽度
        HBox card = new HBox(0, bar, label);
        card.setStyle(
            "-fx-background-color: " + SURFACE_CARD + ";" +
            "-fx-background-radius: " + RADIUS_MD + ";" +
            "-fx-border-color: " + CORAL_LIGHT + ";" +
            "-fx-border-radius: " + RADIUS_MD + ";" +
            "-fx-border-width: 1px;"
        );
        HBox.setHgrow(label, Priority.ALWAYS);
        return card;
    }

    /** 弹出一个警告对话框 */
    private static void warnAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).showAndWait();
    }

    /** 表单字段左侧的标签工厂方法：统一风格生成加粗深青色小标签 */
    private static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        lbl.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        return lbl;
    }

    /** int 转字符串的工具方法 */
    private static String str(int v) { return String.valueOf(v); }

    /** Goods 表列工厂方法：按取值函数与列宽生成列 */
    private static TableColumn<Goods, String> gCol(String t, java.util.function.Function<Goods, String> fn, int w) {
        TableColumn<Goods, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    /** Sell 表列工厂方法：按取值函数与列宽生成列 */
    private static TableColumn<Sell, String> sCol(String t, java.util.function.Function<Sell, String> fn, int w) {
        TableColumn<Sell, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }

    /** Retreat 表列工厂方法：按取值函数与列宽生成列 */
    private static TableColumn<Retreat, String> rCol(String t, java.util.function.Function<Retreat, String> fn, int w) {
        TableColumn<Retreat, String> c = new TableColumn<>(t);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        return c;
    }
}
