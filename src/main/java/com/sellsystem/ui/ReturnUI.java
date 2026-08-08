package com.sellsystem.ui;

import com.sellsystem.dao.EmployeeDAO;
import com.sellsystem.dao.GoodsDAO;
import com.sellsystem.dao.ManufacturerDAO;
import com.sellsystem.dao.RetreatDAO;
import com.sellsystem.dao.SellDAO;
import com.sellsystem.model.Employee;
import com.sellsystem.model.Manufacturer;
import com.sellsystem.model.Retreat;
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
 * 退货登记界面 — Flip7 风格（coral 强调条）
 *
 * 功能：录入一笔退货业务。选择厂商后，商品名、型号以「联动下拉框」
 * 的方式从进货（库存）表中选取；型号确定后自动从销售表带出最近一次
 * 销售的单价作为退款单价（不可手改）；填写退货数量时实时计算退款总额；
 * 提交后调用 RetreatDAO.retreat() 完成退货入库。
 */
public class ReturnUI {

    // 数据访问对象：分别负责厂商、员工、商品（库存）、销售、退货表
    private final ManufacturerDAO manufacturerDAO = new ManufacturerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final GoodsDAO goodsDAO = new GoodsDAO();
    private final SellDAO sellDAO = new SellDAO();
    private final RetreatDAO retreatDAO = new RetreatDAO();

    // 退货表单的输入控件
    private ComboBox<Manufacturer> manufacturerCombo;  // 厂商下拉框
    private ComboBox<String> productNameCombo;         // 商品名下拉框（随厂商联动）
    private ComboBox<String> modelCombo;               // 型号下拉框（随商品名联动）
    private ComboBox<Employee> employeeCombo;          // 业务员下拉框
    private TextField priceField, quantityField;       // 退款单价（自动带出）、退货数量
    private Label totalLabel, msgLabel;                // 退款总额、操作提示标签

    /**
     * 构建"退货登记"面板（VBox），供主界面以 Tab 形式嵌入。
     */
    public VBox createPane() {
        // 根容器：纵向排列，带内边距，底色为页面背景色
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 区块标题
        root.getChildren().add(sectionTitle("↩", "退货登记"));

        // ── 表单卡片（coral 左强调条） ──
        // 退货登记表单：厂商/商品名/型号/单价/数量/退款总额/业务员
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);

        // 厂商下拉框（数据来自厂商表）：选中后联动加载该厂商的商品名
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
        modelCombo.valueProperty().addListener((obs, o, n) -> onModelChanged());

        // 退款单价输入框：由系统从销售表自动带出，不允许手动修改
        priceField = new TextField();
        priceField.setEditable(false);
        priceField.setPromptText("选型号后自动获取");
        priceField.textProperty().addListener((obs, o, n) -> updateTotal());

        // 退货数量输入框：内容变化时实时重算退款总额
        quantityField = new TextField(); quantityField.setPromptText("退货数量");
        quantityField.textProperty().addListener((obs, o, n) -> updateTotal());

        // 退款总额标签（红色高亮，实时显示单价×数量）
        totalLabel = new Label("0.00");
        totalLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 16));
        totalLabel.setStyle("-fx-text-fill: " + CORAL + ";");

        // 业务员下拉框（数据来自员工表）
        employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("请选择业务员");
        employeeCombo.setPrefWidth(220);
        employeeCombo.setItems(FXCollections.observableArrayList(employeeDAO.getAll()));

        // 将标签与输入控件按行排入网格
        form.add(fieldLabel("厂商"), 0, 0);      form.add(manufacturerCombo, 1, 0);
        form.add(fieldLabel("商品名"), 0, 1);    form.add(productNameCombo, 1, 1);
        form.add(fieldLabel("型号"), 0, 2);      form.add(modelCombo, 1, 2);
        form.add(fieldLabel("单价(元)"), 0, 3);  form.add(priceField, 1, 3);
        form.add(fieldLabel("数量"), 0, 4);      form.add(quantityField, 1, 4);
        form.add(fieldLabel("退款总额(元)"), 0, 5); form.add(totalLabel, 1, 5);
        form.add(fieldLabel("业务员"), 0, 6);    form.add(employeeCombo, 1, 6);

        // 表单装入白色卡片，左侧加 coral 强调条
        VBox formCard = new VBox(14, form);
        formCard.setPadding(new Insets(20));
        HBox accentCard = accentCard(formCard, CORAL);
        root.getChildren().add(accentCard);

        // ── 按钮 + 消息 ──
        // 确认退货按钮：点击后执行 doRetreat() 完成退货
        Button submitBtn = pillButton("↩ 确认退货", Variant.CORAL);
        submitBtn.setPrefWidth(140);
        submitBtn.setOnAction(e -> doRetreat());

        // 操作提示信息（成功/失败/校验提示均显示于此）
        msgLabel = new Label();
        msgLabel.setFont(Font.font(FONT_FAMILY, 13));
        msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(Double.MAX_VALUE);

        // 按钮与提示信息水平排列，提示信息占据剩余宽度
        HBox bottomBox = new HBox(15, submitBtn, msgLabel);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(msgLabel, Priority.ALWAYS);
        root.getChildren().add(bottomBox);

        return root;
    }

    /**
     * 厂商变化：清空商品名、型号选择与退款单价，并重新加载该厂商的商品名列表。
     */
    private void onManufacturerChanged() {
        // 清空下级联动项与单价、总额
        productNameCombo.getItems().clear();
        modelCombo.getItems().clear();
        productNameCombo.setValue(null);
        modelCombo.setValue(null);
        priceField.clear();
        updateTotal();

        Manufacturer mfr = manufacturerCombo.getValue();
        if (mfr == null) return;
        // 加载该厂商下所有去重后的商品名
        productNameCombo.setItems(FXCollections.observableArrayList(
                goodsDAO.getDistinctNamesByManufacturer(mfr.get厂商名称())));
    }

    /**
     * 商品名变化：清空型号选择与退款单价，并加载该厂商+该商品的型号列表。
     */
    private void onProductChanged() {
        // 清空型号选择与单价、总额
        modelCombo.getItems().clear();
        modelCombo.setValue(null);
        priceField.clear();
        updateTotal();

        Manufacturer mfr = manufacturerCombo.getValue();
        String name = productNameCombo.getValue();
        if (mfr == null || name == null) return;
        // 加载该厂商+该商品下所有去重后的型号
        modelCombo.setItems(FXCollections.observableArrayList(
                goodsDAO.getDistinctModels(mfr.get厂商名称(), name)));
    }

    /**
     * 型号变化：自动从销售表带出该商品最近一次销售的单价作为退款单价，
     * 并实时刷新退款总额。无销售记录则清空单价。
     */
    private void onModelChanged() {
        Manufacturer mfr = manufacturerCombo.getValue();
        String name = productNameCombo.getValue();
        String model = modelCombo.getValue();

        if (mfr == null || name == null || model == null) {
            priceField.clear();   // 条件不齐则清空单价
            updateTotal();
            return;
        }
        // 从销售表查询最近一次销售的单价并填入
        BigDecimal price = sellDAO.getLatestSalePrice(mfr.get厂商名称(), name, model);
        priceField.setText(price != null ? price.toString() : "");
        updateTotal();
    }

    /**
     * 实时计算并刷新退款总额：单价 × 数量，保留两位小数。
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
     * 确认退货的主流程：
     * 1) 校验厂商、商品名、型号、业务员均已选择；
     * 2) 解析自动带出的单价与数量（数量必须为正整数），计算退款总额；
     * 3) 构造 Retreat 对象，写入当前日期与业务员编号；
     * 4) 调用 RetreatDAO.retreat()：成功返回空字符串并增加库存，
     *    失败（如库存不足）返回错误信息字符串。
     */
    private void doRetreat() {
        // 读取下拉框选中值
        Manufacturer mfr = manufacturerCombo.getValue();
        Employee emp = employeeCombo.getValue();
        String name = productNameCombo.getValue();
        String model = modelCombo.getValue();

        // 逐项校验必填内容
        if (mfr == null) { msgLabel.setText("请选择厂商"); return; }
        if (name == null) { msgLabel.setText("请选择商品名"); return; }
        if (model == null) { msgLabel.setText("请选择型号"); return; }
        if (emp == null) { msgLabel.setText("请选择业务员"); return; }

        try {
            // 解析自动带出的价格与数量，数量必须为正整数
            BigDecimal price = new BigDecimal(priceField.getText());
            int quantity = Integer.parseInt(quantityField.getText());
            if (quantity <= 0) { msgLabel.setText("退货数量必须大于0"); return; }
            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
            LocalDate now = LocalDate.now();

            // 组装 Retreat 对象（退货记录）
            Retreat retreat = new Retreat();
            retreat.set厂商(mfr.get厂商名称());
            retreat.set商品名(name);
            retreat.set型号(model);
            retreat.set单价(price);
            retreat.set数量(quantity);
            retreat.set总金额(total);
            retreat.set退货年(now.getYear());
            retreat.set退货月(now.getMonthValue());
            retreat.set退货日(now.getDayOfMonth());
            retreat.set业务员编号(emp.get员工编号());

            // 调用 DAO 执行退货：返回空串表示成功，否则为错误原因
            String result = retreatDAO.retreat(retreat);
            if (result.isEmpty()) {
                // 成功：绿色提示，清空数量并重置退款总额
                msgLabel.setStyle("-fx-text-fill: " + SUCCESS + ";");
                msgLabel.setText("退货登记成功！" + retreat.get商品名() + " " + retreat.get型号()
                        + " ×" + quantity + "  退款总额: ¥" + total);
                quantityField.clear(); totalLabel.setText("0.00");
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
