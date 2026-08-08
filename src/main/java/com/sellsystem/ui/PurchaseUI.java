package com.sellsystem.ui;

import com.sellsystem.dao.EmployeeDAO;
import com.sellsystem.dao.GoodsDAO;
import com.sellsystem.dao.ManufacturerDAO;
import com.sellsystem.model.Employee;
import com.sellsystem.model.Goods;
import com.sellsystem.model.Manufacturer;
import com.sellsystem.util.Session;
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
 * 进货登记界面 — Flip7 风格（teal 强调条）
 *
 * 功能：录入一笔进货业务。包含两大模块：
 *   1) 新厂商登记：当进货厂商尚未在厂商表中时，可先在此登记新厂商；
 *   2) 进货表单：选择厂商与业务员，填写商品名、型号、单价、数量，
 *      系统实时计算总金额，提交后调用 GoodsDAO 将商品写入进货/库存表。
 * 注：本类中 Session 与部分成员为预留/复用，核心逻辑在 createPane()。
 */
public class PurchaseUI {

    // 数据访问对象：分别负责厂商、员工、商品（进货）表
    private final ManufacturerDAO manufacturerDAO = new ManufacturerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final GoodsDAO goodsDAO = new GoodsDAO();

    // 进货表单的输入控件
    private ComboBox<Manufacturer> manufacturerCombo;  // 进货厂商下拉框
    private ComboBox<Employee> employeeCombo;          // 业务员下拉框
    private TextField productNameField, modelField, priceField, quantityField; // 商品名/型号/单价/数量
    private Label totalLabel, msgLabel;                // 总金额标签、操作提示标签

    // 新厂商登记的输入控件
    private TextField newMfrNameField, newMfrRepField, newMfrPhoneField, newMfrAddrField;

    /**
     * 构建"进货登记"面板（VBox），供主界面以 Tab 形式嵌入。
     */
    public VBox createPane() {
        // 根容器：纵向排列，带内边距，底色为页面背景色
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // ── 标题 ──
        // 区块标题（emoji + 文字 + 虚线）
        VBox titleBox = sectionTitle("📦", "进货登记");
        root.getChildren().add(titleBox);

        // ── 新厂商登记卡片 ──
        // 若需要进货的厂商不在下拉列表中，可在此登记新厂商
        Label newMfrTitle = new Label("🏭 新厂商登记（如进货厂商不在列表中，请在此登记）");
        newMfrTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        newMfrTitle.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        // 新厂商表单：厂商名称（必填）、法人代表、电话、地址
        GridPane newMfrForm = new GridPane();
        newMfrForm.setHgap(10);
        newMfrForm.setVgap(10);

        newMfrNameField = new TextField(); newMfrNameField.setPromptText("厂商全称（必填）");
        newMfrRepField = new TextField();
        newMfrPhoneField = new TextField();
        newMfrAddrField = new TextField();

        // 标签与输入框按行排入网格
        newMfrForm.add(fieldLabel("厂商名称"), 0, 0); newMfrForm.add(newMfrNameField, 1, 0);
        newMfrForm.add(fieldLabel("法人代表"), 0, 1); newMfrForm.add(newMfrRepField, 1, 1);
        newMfrForm.add(fieldLabel("电话"), 0, 2);     newMfrForm.add(newMfrPhoneField, 1, 2);
        newMfrForm.add(fieldLabel("地址"), 0, 3);     newMfrForm.add(newMfrAddrField, 1, 3);

        // 登记新厂商按钮
        Button addMfrBtn = pillButton("登记新厂商", Variant.OUTLINE);
        addMfrBtn.setOnAction(e -> registerNewManufacturer());

        // 将标题、表单、按钮装入一个白色圆角卡片
        VBox newMfrCard = new VBox(10, newMfrTitle, newMfrForm, addMfrBtn);
        newMfrCard.setPadding(new Insets(16));
        newMfrCard.setStyle(
            "-fx-background-color: " + SURFACE_CARD + ";" +
            "-fx-background-radius: " + RADIUS_MD + ";" +
            "-fx-border-color: " + PRIMARY_BG + ";" +
            "-fx-border-radius: " + RADIUS_MD + ";" +
            "-fx-border-width: 2px;"
        );

        root.getChildren().add(newMfrCard);
        root.getChildren().add(dashedSeparator()); // 区块间虚线分隔

        // ── 进货表单卡片（teal 左强调条） ──
        // 主体进货登记表单：厂商/商品名/型号/单价/数量/总金额/业务员
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);

        // 进货厂商下拉框（数据来自厂商表）
        manufacturerCombo = new ComboBox<>();
        manufacturerCombo.setPromptText("请选择厂商");
        manufacturerCombo.setPrefWidth(220);
        refreshManufacturers();

        // 商品名 / 型号输入框
        productNameField = new TextField(); productNameField.setPromptText("如: 手机");
        modelField = new TextField(); modelField.setPromptText("如: 8250");

        // 单价与数量输入框：内容变化时实时重算总金额
        priceField = new TextField(); priceField.setPromptText("进货单价");
        priceField.textProperty().addListener((obs, o, n) -> updateTotal());
        quantityField = new TextField(); quantityField.setPromptText("进货数量");
        quantityField.textProperty().addListener((obs, o, n) -> updateTotal());

        // 总金额标签（金色高亮，实时显示单价×数量）
        totalLabel = new Label("0.00");
        totalLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 16));
        totalLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        // 业务员下拉框（数据来自员工表）
        employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("请选择业务员");
        employeeCombo.setPrefWidth(220);
        refreshEmployees();

        // 将标签与输入控件按行排入网格（第0列标签，第1列输入）
        form.add(fieldLabel("进货厂商"), 0, 0); form.add(manufacturerCombo, 1, 0);
        form.add(fieldLabel("商品名"), 0, 1);   form.add(productNameField, 1, 1);
        form.add(fieldLabel("型号"), 0, 2);     form.add(modelField, 1, 2);
        form.add(fieldLabel("单价(元)"), 0, 3); form.add(priceField, 1, 3);
        form.add(fieldLabel("数量"), 0, 4);     form.add(quantityField, 1, 4);
        form.add(fieldLabel("总金额(元)"), 0, 5); form.add(totalLabel, 1, 5);
        form.add(fieldLabel("业务员"), 0, 6);   form.add(employeeCombo, 1, 6);

        // 表单装入白色卡片，左侧加 teal 强调条
        VBox formCard = new VBox(14, form);
        formCard.setPadding(new Insets(20));
        HBox accentCard = accentCard(formCard, PRIMARY_TEAL);

        root.getChildren().add(accentCard);

        // ── 按钮 + 消息 ──
        // 确认进货按钮：点击后执行 doPurchase() 完成入库
        Button submitBtn = pillButton("✅ 确认进货", Variant.PRIMARY);
        submitBtn.setPrefWidth(140);
        submitBtn.setOnAction(e -> doPurchase());

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
     * 刷新厂商下拉框：从数据库重新加载全部厂商。
     */
    private void refreshManufacturers() {
        manufacturerCombo.setItems(FXCollections.observableArrayList(manufacturerDAO.getAll()));
    }

    /**
     * 刷新业务员下拉框：从数据库重新加载全部员工。
     */
    private void refreshEmployees() {
        employeeCombo.setItems(FXCollections.observableArrayList(employeeDAO.getAll()));
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
     * 登记一个新厂商：
     * 校验厂商名称非空且不重复，构造 Manufacturer 对象并写入数据库，
     * 成功后刷新厂商下拉框供进货表单直接选用。
     */
    private void registerNewManufacturer() {
        String name = newMfrNameField.getText().trim();
        // 校验1：厂商名称必填
        if (name.isEmpty()) { msgLabel.setText("厂商名称不能为空"); return; }
        // 校验2：厂商名称不能重复
        Manufacturer existing = manufacturerDAO.getByName(name);
        if (existing != null) { msgLabel.setText("该厂商已存在"); return; }

        // 组装厂商对象（厂商编号由数据库自增生成，无需手动赋值）
        Manufacturer m = new Manufacturer();
        m.set厂商名称(name);
        m.set法人代表(newMfrRepField.getText().trim());
        m.set电话(newMfrPhoneField.getText().trim());
        m.set厂商地址(newMfrAddrField.getText().trim());

        // 写入数据库并处理结果
        if (manufacturerDAO.insert(m)) {
            // 成功：绿色提示，清空表单并刷新下拉框
            msgLabel.setStyle("-fx-text-fill: " + SUCCESS + ";");
            msgLabel.setText("新厂商 '" + name + "' 登记成功！");
            newMfrNameField.clear(); newMfrRepField.clear();
            newMfrPhoneField.clear(); newMfrAddrField.clear();
            refreshManufacturers();
        } else {
            // 失败：红色提示
            msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
            msgLabel.setText("厂商登记失败");
        }
    }

    /**
     * 确认进货的主流程：
     * 1) 校验厂商、商品名、型号、业务员均已填写；
     * 2) 解析单价与数量为 BigDecimal，计算总金额；
     * 3) 构造 Goods 对象，写入当前日期与业务员编号；
     * 4) 调用 GoodsDAO.purchase() 入库，成功则清空表单。
     */
    private void doPurchase() {
        // 读取下拉框选中值
        Manufacturer mfr = manufacturerCombo.getValue();
        Employee emp = employeeCombo.getValue();

        // 逐项校验必填内容
        if (mfr == null) { msgLabel.setText("请选择进货厂商"); return; }
        if (productNameField.getText().trim().isEmpty()) { msgLabel.setText("请输入商品名"); return; }
        if (modelField.getText().trim().isEmpty()) { msgLabel.setText("请输入型号"); return; }
        if (emp == null) { msgLabel.setText("请选择业务员"); return; }

        try {
            // 解析数字并计算总金额
            BigDecimal price = new BigDecimal(priceField.getText());
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal total = price.multiply(quantity);
            LocalDate now = LocalDate.now();

            // 组装 Goods 对象（进货记录）
            Goods goods = new Goods();
            goods.set生产厂商(mfr.get厂商名称());
            goods.set商品名(productNameField.getText().trim());
            goods.set型号(modelField.getText().trim());
            goods.set单价(price);
            goods.set数量(quantity);
            goods.set总金额(total);
            goods.set进货年(now.getYear());
            goods.set进货月(now.getMonthValue());
            goods.set进货日(now.getDayOfMonth());
            goods.set业务员编号(emp.get员工编号());

            // 调用 DAO 执行进货（写入商品/库存表）
            if (goodsDAO.purchase(goods)) {
                // 成功：绿色提示并清空表单、重置总金额
                msgLabel.setStyle("-fx-text-fill: " + SUCCESS + ";");
                msgLabel.setText("进货登记成功！" + goods.get商品名() + " " + goods.get型号()
                        + " ×" + quantity + "  总金额: ¥" + total);
                productNameField.clear(); modelField.clear();
                priceField.clear(); quantityField.clear();
                totalLabel.setText("0.00");
            } else {
                // 失败：红色提示
                msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
                msgLabel.setText("进货登记失败");
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
