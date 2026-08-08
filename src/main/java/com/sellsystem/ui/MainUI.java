package com.sellsystem.ui;

import com.sellsystem.dao.GoodsDAO;
import com.sellsystem.dao.SellDAO;
import com.sellsystem.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.sellsystem.ui.Theme.*;

/**
 * 主界面 — 侧边栏导航 + TabPane 内容区 + 仪表盘首页 + 状态栏
 *
 * 整体布局（BorderPane）：
 *   - 左侧：侧边栏导航（Logo + 可折叠功能分组 + 底部操作按钮）；
 *   - 中央：TabPane 内容区，默认打开仪表盘首页，导航点击可开/切 Tab；
 *   - 底部：状态栏，显示当前登录用户信息。
 *
 * 通过点击侧边栏各功能按钮，将对应业务面板以 Tab 形式打开，
 * 覆盖进货/销售/退货登记、统计报表、业绩查看、数据表维护等功能。
 */
public class MainUI {

    private TabPane tabPane;              // 中央内容区：管理所有功能 Tab
    private Stage stage;                  // 主窗口引用
    private final GoodsDAO goodsDAO = new GoodsDAO();  // 进货统计用 DAO
    private final SellDAO sellDAO = new SellDAO();     // 销售统计用 DAO

    /**
     * 主界面入口：组装布局并显示主窗口。
     * @param stage 由登录界面传入的新窗口
     */
    public void start(Stage stage) {
        this.stage = stage;
        // 窗口标题显示当前登录用户名
        stage.setTitle("销售管理信息系统 — 欢迎 " + Session.getCurrentUsername());

        // 根布局：BorderPane（左/中/下三区）
        BorderPane root = new BorderPane();

        // ── 左侧边栏（ScrollPane 包裹，内容多时可滚动）──
        VBox sidebar = createSidebar();
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setFitToHeight(true);
        sidebarScroll.setPrefWidth(230);
        sidebarScroll.setMinWidth(230);
        sidebarScroll.setMaxWidth(230);
        sidebarScroll.getStyleClass().add("sidebar-scroll");
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setLeft(sidebarScroll);

        // ── 中央内容区（默认显示仪表盘）──
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        openDashboard(); // 先打开首页 Tab
        root.setCenter(tabPane);

        // ── 状态栏 ──
        // 底部深青色横条，显示当前用户与编号信息
        Label statusLabel = new Label("  👤 当前用户: " + Session.getCurrentUsername()
                + "  |  🆔 用户编号: " + Session.getCurrentUserId()
                + "  |  ✨ 欢迎使用销售管理信息系统");
        statusLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));
        statusLabel.setStyle(
            "-fx-background-color: " + PRIMARY_DARK + ";" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8px 12px;"
        );
        root.setBottom(statusLabel);

        // 创建场景并加载全局 CSS
        Scene scene = new Scene(root, 1150, 720);
        String css = getClass().getResource("/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════
    //  侧边栏
    // ═══════════════════════════════════════════════════════════

    /**
     * 构建侧边栏：自上而下依次为
     * Logo、首页按钮、交易管理分组、统计报表分组、数据管理分组、
     * 弹性空白、以及底部的修改密码/退出登录/退出系统/关于按钮。
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.getStyleClass().add("sidebar");

        // Logo
        sidebar.getChildren().add(createLogo());

        // 首页
        Button homeBtn = createSidebarHomeButton("🏠", "首页概览", this::openDashboard);
        sidebar.getChildren().add(homeBtn);

        sidebar.getChildren().add(createSidebarSeparator());

        // ── 交易管理 ──
        // 展开式分组：进货登记 / 销售登记 / 退货登记
        sidebar.getChildren().add(createExpandableSection("💰", "交易管理", true,
            createNavButton("📦", "进货登记", () -> openTab("进货登记",
                wrapInScroll(new PurchaseUI().createPane()))),
            createNavButton("💵", "销售登记", () -> openTab("销售登记",
                wrapInScroll(new SalesUI().createPane()))),
            createNavButton("↩", "退货登记", () -> openTab("退货登记",
                wrapInScroll(new ReturnUI().createPane())))
        ));

        // ── 统计报表 ──
        // 业绩查看有子项，进货/销售统计直接打开组合视图
        VBox perfSubItems = new VBox(1,
            createNavButton("📋", "全员业绩", () -> openTab("全员业绩",
                wrapInScroll(new PerformanceUI().createAllPane()))),
            createNavButton("🔍", "按员工查询", () -> openTab("按员工查询",
                wrapInScroll(new PerformanceUI().createByEmployeePane())))
        );
        perfSubItems.setPadding(new Insets(0));

        // 统计报表分组：进货统计 / 销售统计 / 业绩查看（二级子分组）
        sidebar.getChildren().add(createExpandableSection("📊", "统计报表", true,
            createNavButton("📥", "进货统计", () -> openTab("进货统计",
                createPurchaseStatsCombined())),
            createNavButton("📊", "销售统计", () -> openTab("销售统计",
                createSalesStatsCombined())),
            createExpandableSubSection("📈", "业绩查看", false, perfSubItems)
        ));

        // ── 数据管理 ──
        // 展开式分组：进货表 / 销售表 / 退货表 / 员工表 / 厂商表
        sidebar.getChildren().add(createExpandableSection("🗄", "数据管理", true,
            createNavButton("📦", "进货表", () -> openTab("进货表",
                wrapInScroll(DataTableUI.createGoodsTablePane()))),
            createNavButton("💵", "销售表", () -> openTab("销售表",
                wrapInScroll(DataTableUI.createSellTablePane()))),
            createNavButton("↩", "退货表", () -> openTab("退货表",
                wrapInScroll(DataTableUI.createRetreatTablePane()))),
            createNavButton("👥", "员工表", () -> openTab("员工表",
                wrapInScroll(DataTableUI.createEmployeeTablePane()))),
            createNavButton("🏭", "厂商表", () -> openTab("厂商表",
                wrapInScroll(DataTableUI.createManufacturerTablePane())))
        ));

        // ── 弹性空白 ──
        // 将底部操作区推到侧边栏最下方
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // ── 底部操作 ──
        sidebar.getChildren().add(createSidebarSeparator());

        // 修改密码按钮：弹出修改密码对话框
        Button changePwdBtn = createSidebarBottomButton("⚙", "修改密码",
            () -> new ChangePasswordUI().showDialog());
        sidebar.getChildren().add(changePwdBtn);

        // 退出登录按钮：清空会话，关闭主窗口，重新打开登录界面
        Button logoutBtn = createSidebarBottomButton("🚪", "退出登录", () -> {
            Session.logout();
            stage.close();
            Stage loginStage = new Stage();
            loginStage.setTitle("销售管理信息系统");
            loginStage.setScene(new LoginUI().createScene());
            loginStage.setResizable(false);
            loginStage.show();
        });
        sidebar.getChildren().add(logoutBtn);

        // 退出系统按钮：直接结束整个进程
        Button exitBtn = createSidebarBottomButton("⏻", "退出系统", () -> System.exit(0));
        sidebar.getChildren().add(exitBtn);

        // 关于按钮：弹出软件信息对话框
        Button aboutBtn = createSidebarBottomButton("❓", "关于", () -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("关于");
            alert.setHeaderText("销售管理信息系统 v1.0");
            alert.setContentText("面向中小型销售公司的进-销-存-退-查一体化桌面管理工具。\n\n技术栈: Java 25 + JavaFX 21 + SQL Server + JDBC");
            alert.showAndWait();
        });
        sidebar.getChildren().add(aboutBtn);

        return sidebar;
    }

    // ── 侧边栏组件工厂 ──────────────────────────────────────

    /** Logo 区域：图标 + 系统名 + 版本号 */
    private VBox createLogo() {
        Label icon = new Label("📊");
        icon.setFont(Font.font(FONT_FAMILY, 26));

        Label name = new Label("销售管理系统");
        name.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 15));
        name.setStyle("-fx-text-fill: white;");

        Label version = new Label("v1.0");
        version.setFont(Font.font(FONT_FAMILY, 10));
        version.setStyle("-fx-text-fill: rgba(255,255,255,0.55);");

        VBox logo = new VBox(4, icon, name, version);
        logo.setAlignment(Pos.CENTER);
        logo.setPadding(new Insets(20, 12, 16, 12));
        logo.getStyleClass().add("sidebar-logo");
        return logo;
    }

    /**
     * 可展开分组：点击标题行切换子项的显示/隐藏。
     * 标题前的 ▼/▶ 标记同步切换，表示当前展开状态。
     * @param emoji      标题图标
     * @param text       标题文字
     * @param expanded   初始是否展开
     * @param subItems   子项节点（可变参数）
     */
    private VBox createExpandableSection(String emoji, String text, boolean expanded,
                                        javafx.scene.Node... subItems) {
        VBox container = new VBox(0);

        // 分组标题行
        Label header = new Label((expanded ? "▼  " : "▶  ") + emoji + "  " + text);
        header.getStyleClass().add("sidebar-section");
        header.setMaxWidth(Double.MAX_VALUE);

        // 子项容器：展开时显示并参与布局
        VBox body = new VBox(1);
        body.getChildren().addAll(subItems);
        body.setVisible(expanded);
        body.setManaged(expanded);

        // 点击标题：切换子项可见性并更新箭头标记
        header.setOnMouseClicked(e -> {
            boolean now = !body.isVisible();
            body.setVisible(now);
            body.setManaged(now);
            header.setText((now ? "▼  " : "▶  ") + emoji + "  " + text);
        });

        container.getChildren().addAll(header, body);
        return container;
    }

    /**
     * 二级可展开子分组（用于"业绩查看"等）。
     * 标题缩进更小，无左边距，实现嵌套的折叠菜单效果。
     */
    private VBox createExpandableSubSection(String emoji, String text, boolean expanded, VBox body) {
        VBox container = new VBox(0);

        // 二级标题行
        Label header = new Label((expanded ? "▼  " : "▶  ") + emoji + "  " + text);
        header.getStyleClass().add("sidebar-subsection");
        header.setMaxWidth(Double.MAX_VALUE);

        // 子项可见性控制
        body.setVisible(expanded);
        body.setManaged(expanded);

        // 点击标题：切换子项可见性并更新箭头标记
        header.setOnMouseClicked(e -> {
            boolean now = !body.isVisible();
            body.setVisible(now);
            body.setManaged(now);
            header.setText((now ? "▼  " : "▶  ") + emoji + "  " + text);
        });

        container.getChildren().addAll(header, body);
        return container;
    }

    /** 侧边栏子项导航按钮：左对齐、占满宽度，点击执行指定动作 */
    private Button createNavButton(String emoji, String text, Runnable action) {
        Button btn = new Button("  " + emoji + "  " + text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /** 首页按钮：比普通子项更突出（首页概览入口） */
    private Button createSidebarHomeButton(String emoji, String text, Runnable action) {
        Button btn = new Button(emoji + "  " + text);
        btn.getStyleClass().add("sidebar-home");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /** 底部操作按钮：修改密码 / 退出登录 / 退出系统 / 关于 */
    private Button createSidebarBottomButton(String emoji, String text, Runnable action) {
        Button btn = new Button(emoji + "  " + text);
        btn.getStyleClass().add("sidebar-bottom");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /** 侧边栏分隔线：用于分组之间的视觉分隔 */
    private Separator createSidebarSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-sep");
        return sep;
    }

    // ═══════════════════════════════════════════════════════════
    //  仪表盘首页
    // ═══════════════════════════════════════════════════════════

    /**
     * 构建仪表盘首页：
     * 欢迎语 + 当前日期 → 2×2 统计卡片（今日/本月销售与进货额）→ 快捷操作按钮。
     */
    private VBox createDashboard() {
        // 根容器：纵向排列
        VBox root = new VBox(20);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");

        // 欢迎语
        Label welcome = new Label("欢迎回来，" + Session.getCurrentUsername() + " 👋");
        welcome.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 24));
        welcome.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        // 当前日期与星期（中文格式）
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().getDisplayName(
            java.time.format.TextStyle.FULL, java.util.Locale.CHINESE);
        Label dateLabel = new Label("📅  " + today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                + "  ·  " + dayOfWeek);
        dateLabel.setFont(Font.font(FONT_FAMILY, 13));
        dateLabel.setStyle("-fx-text-fill: " + PRIMARY_LIGHT + ";");

        VBox headerBox = new VBox(4, welcome, dateLabel);

        // 统计卡片 2×2 网格
        GridPane cardGrid = new GridPane();
        cardGrid.setHgap(20);
        cardGrid.setVgap(20);

        // 查询数据：今日/本月的销售与进货总金额
        BigDecimal todaySales = safeGetTotal(sellDAO, today, today.getDayOfMonth());
        BigDecimal todayPurchase = safeGetTotal(goodsDAO, today, today.getDayOfMonth());
        BigDecimal monthSales = safeGetTotal(sellDAO, today, null);
        BigDecimal monthPurchase = safeGetTotal(goodsDAO, today, null);

        // 四个统计卡片（销售用金色强调，进货用 teal 强调）
        cardGrid.add(createStatCard("💰", "今日销售额", formatMoney(todaySales), ACCENT_GOLD), 0, 0);
        cardGrid.add(createStatCard("📦", "今日进货额", formatMoney(todayPurchase), PRIMARY_TEAL), 1, 0);
        cardGrid.add(createStatCard("📊", "本月销售额", formatMoney(monthSales), ACCENT_GOLD), 0, 1);
        cardGrid.add(createStatCard("📥", "本月进货额", formatMoney(monthPurchase), PRIMARY_TEAL), 1, 1);

        // 快捷操作
        Label quickLabel = new Label("⚡ 快捷操作");
        quickLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 15));
        quickLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        HBox quickActions = new HBox(12);
        quickActions.setAlignment(Pos.CENTER_LEFT);

        // 三个快捷按钮：直接打开对应登记 Tab
        Button quickPurchase = pillButton("📦 进货登记", Variant.TEAL);
        quickPurchase.setOnAction(e -> openTab("进货登记",
            wrapInScroll(new PurchaseUI().createPane())));

        Button quickSales = pillButton("💵 销售登记", Variant.PRIMARY);
        quickSales.setOnAction(e -> openTab("销售登记",
            wrapInScroll(new SalesUI().createPane())));

        Button quickReturn = pillButton("↩ 退货登记", Variant.CORAL);
        quickReturn.setOnAction(e -> openTab("退货登记",
            wrapInScroll(new ReturnUI().createPane())));

        quickActions.getChildren().addAll(quickPurchase, quickSales, quickReturn);

        root.getChildren().addAll(headerBox, cardGrid, dashedSeparator(), quickLabel, quickActions);
        return root;
    }

    /** 创建单个统计卡片：emoji + 标题 + 数值，左侧带彩色强调条 */
    private VBox createStatCard(String emoji, String title, String value, String accentColor) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("dashboard-card");
        // 顶部强调条
        card.setStyle(card.getStyle() +
            "-fx-border-color: transparent transparent transparent " + accentColor + ";" +
            "-fx-border-width: 0 0 0 4px;" +
            "-fx-border-radius: " + RADIUS_MD + ";" +
            "-fx-background-radius: " + RADIUS_MD + ";"
        );

        // 图标、标题、数值三行
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(32));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(FONT_FAMILY, 13));
        titleLabel.setStyle("-fx-text-fill: #999;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 26));
        valueLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        card.getChildren().addAll(emojiLabel, titleLabel, valueLabel);
        return card;
    }

    /**
     * 安全获取某时段总金额：
     * 根据 dao 实际类型（GoodsDAO 或 SellDAO）调用对应查询，
     * 任何异常或空结果都返回 BigDecimal.ZERO，避免仪表盘因数据缺失崩溃。
     */
    private BigDecimal safeGetTotal(Object dao, LocalDate today, Integer day) {
        try {
            if (dao instanceof GoodsDAO g) {
                BigDecimal result = g.getTotalAmount(today.getYear(),
                    today.getMonthValue(), today.getMonthValue(), day);
                return result != null ? result : BigDecimal.ZERO;
            } else if (dao instanceof SellDAO s) {
                BigDecimal result = s.getTotalAmount(today.getYear(),
                    today.getMonthValue(), today.getMonthValue(), day);
                return result != null ? result : BigDecimal.ZERO;
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }

    /** 金额格式化：0 显示为 "¥ 0.00"，非 0 带千分位 */
    private String formatMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "¥ 0.00";
        }
        return String.format("¥ %,.2f", amount);
    }

    // ═══════════════════════════════════════════════════════════
    //  组合统计视图
    // ═══════════════════════════════════════════════════════════

    /** 进货统计组合视图：用 TabPane 聚合 今日/本月/本季度/本年度 四个统计面板 */
    private TabPane createPurchaseStatsCombined() {
        TabPane tp = new TabPane();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tp.getTabs().addAll(
            createSubTab("今日进货", PurchaseStatsUI.createTodayPane()),
            createSubTab("本月进货", PurchaseStatsUI.createMonthPane()),
            createSubTab("本季度进货", PurchaseStatsUI.createQuarterPane()),
            createSubTab("本年度进货", PurchaseStatsUI.createYearPane())
        );
        return tp;
    }

    /** 销售统计组合视图：用 TabPane 聚合 今日/本月/本季度/本年度 四个统计面板 */
    private TabPane createSalesStatsCombined() {
        TabPane tp = new TabPane();
        tp.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tp.getTabs().addAll(
            createSubTab("今日销售", SalesStatsUI.createTodayPane()),
            createSubTab("本月销售", SalesStatsUI.createMonthPane()),
            createSubTab("本季度销售", SalesStatsUI.createQuarterPane()),
            createSubTab("本年度销售", SalesStatsUI.createYearPane())
        );
        return tp;
    }

    /**
     * 创建统计面板子 Tab：内容包进可滚动 ScrollPane，且不可关闭。
     */
    private Tab createSubTab(String title, VBox content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + SURFACE_BASE + ";");
        Tab tab = new Tab(title, sp);
        tab.setClosable(false);
        return tab;
    }

    // ═══════════════════════════════════════════════════════════
    //  Tab 管理
    // ═══════════════════════════════════════════════════════════

    /** 打开指定标题的 Tab，若已存在则选中（不重复创建） */
    private void openTab(String title, javafx.scene.Node content) {
        // 如果已存在同名 Tab，直接选中
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText() != null && tab.getText().equals(title)) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        // 确保 content 可滚动：非 ScrollPane/TabPane 的内容统一包裹
        javafx.scene.Node displayContent = content;
        if (!(content instanceof ScrollPane) && !(content instanceof TabPane)) {
            ScrollPane sp = new ScrollPane(content);
            sp.setFitToWidth(true);
            sp.setFitToHeight(true);
            sp.setStyle("-fx-background-color: " + SURFACE_BASE + "; -fx-border-width: 0;");
            displayContent = sp;
        }
        Tab tab = new Tab(title, displayContent);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    /** 切换到仪表盘首页：已存在则选中，否则新建并插入到最前 */
    private void openDashboard() {
        // 检查是否已有仪表盘 Tab
        for (Tab tab : tabPane.getTabs()) {
            if ("🏠 首页".equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }
        ScrollPane sp = new ScrollPane(createDashboard());
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + SURFACE_BASE + ";");
        Tab dashboardTab = new Tab("🏠 首页", sp);
        dashboardTab.setClosable(false);
        // 插入到最前面 
        tabPane.getTabs().add(0, dashboardTab);
        tabPane.getSelectionModel().select(dashboardTab);
    }

    /** 将内容包裹在 ScrollPane 中：保证内容超长时可滚动 */
    private ScrollPane wrapInScroll(javafx.scene.Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background-color: " + SURFACE_BASE + "; -fx-border-width: 0;");
        return sp;
    }
}
