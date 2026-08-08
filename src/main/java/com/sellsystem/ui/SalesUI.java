package com.sellsystem.ui;

import com.sellsystem.dao.EmployeeDAO;
import com.sellsystem.dao.GoodsDAO;
import com.sellsystem.dao.ManufacturerDAO;
import com.sellsystem.dao.SellDAO;
import com.sellsystem.model.Employee;
import com.sellsystem.model.Goods;
import com.sellsystem.model.Manufacturer;
import com.sellsystem.model.Sell;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.sellsystem.ui.Theme.*;

/**
 * 销售登记界面 — Flip7 风格（gold 强调条）
 *
 * 功能：录入一笔销售业务。选择生产厂商后，商品名、型号以「联动下拉框」
 * 的方式从进货（库存）表中选取；商品名与型号都确定后自动查询并显示
 * 当前库存；填写单价、数量时实时计算总金额；提交后调用 SellDAO.sell()
 * 完成销售出库，同时扣减对应库存。
 */
public class SalesUI {

    // 数据访问对象：分别负责厂商、员工、商品（库存）、销售表
    private final ManufacturerDAO manufacturerDAO = new ManufacturerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final GoodsDAO goodsDAO = new GoodsDAO();
    private final SellDAO sellDAO = new SellDAO();

    // 销售表单的输入控件
    private ComboBox<Manufacturer> manufacturerCombo;  // 生产厂商下拉框
    private ComboBox<String> productNameCombo;         // 商品名下拉框（随厂商联动）
    private ComboBox<String> modelCombo;               // 型号下拉框（随商品名联动）
    private ComboBox<Employee> employeeCombo;          // 业务员下拉框
    private TextField priceField, quantityField;       // 单价 / 数量
    private Label totalLabel, stockLabel, msgLabel;    // 总金额、当前库存、操作提示标签

    /**
     * 构建"销售登记"面板（VBox），供主界面以 Tab 形式嵌入。
     */
    public VBox createPane() {
        // 根容器：纵向排列，带内边距，底色为页面背景色
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("💵", "销售登记"));

        // ── 表单卡片（gold 左强调条） ──
        // 销售登记表单：厂商/商品名/型号/当前库存/单价/数量/总金额/业务员
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);

        // 生产厂商下拉框（数据来自厂商表）：选中后联动加载该厂商的商品名
        manufacturerCombo = new ComboBox<>();
        manufacturerCombo.setPromptText("请选择厂商");
        manufacturerCombo.setPrefWidth(220);
        manufacturerCombo.setItems(FXCollections.observableArrayList(manufacturerDAO.getAll()));
        manufacturerCombo.valueProperty().addListener((obs, o, n) -> onManufacturerChanged());

        // 商品名下拉框：数据来自该厂商在进货表中已有的商品名（去重）
        productNameCombo = new ComboBox<>();
        productNameCombo.setPromptText("请选择商品");
        productNameCombo.setPrefWidth(220);
        productNameCombo.valueProperty().addListener((obs, o, n) -> onProductChanged());

        // 型号下拉框：数据来自该厂商+该商品在进货表中已有的型号（去重）
        modelCombo = new ComboBox<>();
        modelCombo.setPromptText("请选择型号");
        modelCombo.setPrefWidth(220);
        modelCombo.valueProperty().addListener((obs, o, n) -> checkStock());

        // 当前库存标签：商品名与型号都选定后自动查询显示
        stockLabel = new Label("—");
        stockLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 14));
        stockLabel.setStyle("-fx-text-fill: " + PRIMARY_TEAL + ";");

        // 销售单价与数量输入框：内容变化时实时重算总金额
        priceField = new TextField(); priceField.setPromptText("销售单价");
        priceField.textProperty().addListener((obs, o, n) -> updateTotal());
        quantityField = new TextField(); quantityField.setPromptText("销售数量（不能超过库存）");
        quantityField.textProperty().addListener((obs, o, n) -> updateTotal());

        // 总金额标签（金色高亮，实时显示单价×数量）
        totalLabel = new Label("0.00");
        totalLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 16));
        totalLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        // 业务员下拉框（数据来自员工表）
        employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("请选择业务员");
        employeeCombo.setPrefWidth(220);
        employeeCombo.setItems(FXCollections.observableArrayList(employeeDAO.getAll()));

        // 将标签与输入控件按行排入网格
        form.add(fieldLabel("生产厂商"), 0, 0); form.add(manufacturerCombo, 1, 0);
        form.add(fieldLabel("商品名"), 0, 1);   form.add(productNameCombo, 1, 1);
        form.add(fieldLabel("型号"), 0, 2);     form.add(modelCombo, 1, 2);
        form.add(fieldLabel("当前库存"), 0, 3); form.add(stockLabel, 1, 3);
        form.add(fieldLabel("单价(元)"), 0, 4); form.add(priceField, 1, 4);
        form.add(fieldLabel("数量"), 0, 5);     form.add(quantityField, 1, 5);
        form.add(fieldLabel("总金额(元)"), 0, 6); form.add(totalLabel, 1, 6);
        form.add(fieldLabel("业务员"), 0, 7);   form.add(employeeCombo, 1, 7);

        // 表单装入白色卡片，左侧加 gold 强调条
        VBox formCard = new VBox(14, form);
        formCard.setPadding(new Insets(20));
        HBox accentCard = accentCard(formCard, ACCENT_GOLD);
        root.getChildren().add(accentCard);

        // ── 按钮 + 消息 ──
        // 确认销售按钮：点击后执行 doSell() 完成销售
        Button submitBtn = pillButton("💰 确认销售", Variant.TEAL);
        submitBtn.setPrefWidth(140);
        submitBtn.setOnAction(e -> doSell());

        // 操作提示信息（成功/失败/校验提示均显示于此）
        msgLabel = new Label();
        msgLabel.setFont(Font.font(FONT_FAMILY, 13));
        msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(Double.MAX_VALUE);

        // 按钮与提示信息水平排列，提示信息占据剩余宽度
        HBox bottomBox = new HBox(12, submitBtn, msgLabel);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(msgLabel, Priority.ALWAYS);
        root.getChildren().add(bottomBox);

        return root;
    }

    /**
     * 厂商变化：清空商品名、型号选择，并重新加载该厂商在进货表中的商品名列表。
     */
    private void onManufacturerChanged() {
        // 清空下级联动项与库存显示
        productNameCombo.getItems().clear();
        modelCombo.getItems().clear();
        productNameCombo.setValue(null);
        modelCombo.setValue(null);
        stockLabel.setText("—");

        Manufacturer mfr = manufacturerCombo.getValue();
        if (mfr == null) return;
        // 加载该厂商下所有去重后的商品名
        productNameCombo.setItems(FXCollections.observableArrayList(
                goodsDAO.getDistinctNamesByManufacturer(mfr.get厂商名称())));
    }

    /**
     * 商品名变化：清空型号选择，并加载该厂商+该商品在进货表中的型号列表。
     */
    private void onProductChanged() {
        // 清空型号选择与库存显示
        modelCombo.getItems().clear();
        modelCombo.setValue(null);
        stockLabel.setText("—");

        Manufacturer mfr = manufacturerCombo.getValue();
        String name = productNameCombo.getValue();
        if (mfr == null || name == null) return;
        // 加载该厂商+该商品下所有去重后的型号
        modelCombo.setItems(FXCollections.observableArrayList(
                goodsDAO.getDistinctModels(mfr.get厂商名称(), name)));
    }

    /**
     * 根据厂商 + 商品名 + 型号自动查询当前库存并显示：
     * 任一条件未选则显示 "—"，查不到对应商品则显示 "无库存"。
     */
    private void checkStock() {
        // 读取当前表单中已选定的条件
        Manufacturer mfr = manufacturerCombo.getValue();
        String name = productNameCombo.getValue();
        String model = modelCombo.getValue();
        if (mfr == null || name == null || model == null) { stockLabel.setText("—"); return; }

        // 调用 DAO 精确匹配一条库存记录
        Goods goods = goodsDAO.findByManufacturerAndNameAndModel(mfr.get厂商名称(), name, model);
        stockLabel.setText(goods != null ? goods.get数量().toString() : "无库存");
    }

    /**
     * 实时计算并刷新总金额：单价 × 数量，保留两位小数。
     * 输入非法数字时显示"格式错误"。
     */
    private void updateTotal() {
        try {
            BigDecimal price = new BigDecimal(priceField.getText().isEmpty() ? "0" : priceField.getText());
            BigDecimal qty = new BigDecimal(quantityField.getText().isEmpty() ? "0" : quantityField.getText());
            totalLabel.setText(price.multiply(qty).setScale(2).toString());
        } catch (NumberFormatException e) {
            totalLabel.setText("格式错误");
        }
    }

    /**
     * 确认销售的主流程：
     * 1) 校验厂商、商品名、型号、业务员均已选择；
     * 2) 解析单价与数量（数量必须为正整数）；
     * 3) 构造 Sell 对象，写入当前日期与业务员编号；
     * 4) 调用 SellDAO.sell()：成功返回空字符串并自动扣减库存，
     *    失败（如库存不足）返回错误信息字符串。
     */
    private void doSell() {
        // 读取下拉框选中值
        Manufacturer mfr = manufacturerCombo.getValue();
        Employee emp = employeeCombo.getValue();
        String name = productNameCombo.getValue();
        String model = modelCombo.getValue();

        // 逐项校验必填内容
        if (mfr == null) { msgLabel.setText("请选择生产厂商"); return; }
        if (name == null) { msgLabel.setText("请选择商品名"); return; }
        if (model == null) { msgLabel.setText("请选择型号"); return; }
        if (emp == null) { msgLabel.setText("请选择业务员"); return; }

        try {
            // 解析价格与数量，数量必须为正整数
            BigDecimal price = new BigDecimal(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) { msgLabel.setText("销售数量必须大于0"); return; }
            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
            LocalDate now = LocalDate.now();

            // 组装 Sell 对象（销售记录）
            Sell sell = new Sell();
            sell.set生产厂商(mfr.get厂商名称());
            sell.set商品名(name);
            sell.set型号(model);
            sell.set单价(price);
            sell.set数量(quantity);
            sell.set总金额(total);
            sell.set销售年(now.getYear());
            sell.set销售月(now.getMonthValue());
            sell.set销售日(now.getDayOfMonth());
            sell.set业务员编号(emp.get员工编号());

            // 调用 DAO 执行销售：返回空串表示成功，否则为错误原因
            String result = sellDAO.sell(sell);
            if (result.isEmpty()) {
                // 成功：绿色提示，清空价格数量并刷新库存显示
                msgLabel.setStyle("-fx-text-fill: " + SUCCESS + ";");
                msgLabel.setText("销售登记成功！" + sell.get商品名() + " " + sell.get型号()
                        + " ×" + quantity + "  总金额: ¥" + total);
                priceField.clear(); quantityField.clear(); totalLabel.setText("0.00");
                checkStock();
            } else {
                // 失败：红色提示（通常为库存不足）
                msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
                msgLabel.setText(result);
            }
        } catch (NumberFormatException e) {
            // 单价或数量不是合法数字
            msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
            msgLabel.setText("请输入有效的单价和数量");
        }
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
