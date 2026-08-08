package com.sellsystem.ui;

import com.sellsystem.config.DBConnection;
import com.sellsystem.dao.UserDAO;
import com.sellsystem.model.User;
import com.sellsystem.util.Session;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import static com.sellsystem.ui.Theme.*;

/**
 * 登录界面 — Flip7 风格
 *
 * 功能：作为程序启动后的首个界面，完成用户身份认证：
 *   1) 显示数据库连接状态（绿点=已连接，红点=连接失败）；
 *   2) 提供用户名 / 密码输入框与登录按钮，校验通过后保存登录
 *      会话（Session）并打开主界面；
 *   3) 提供"注册新用户"入口，弹出注册对话框。
 */
public class LoginUI {

    // 数据访问对象：负责用户登录校验
    private final UserDAO userDAO = new UserDAO();
    // 登录表单输入控件与提示标签
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;

    /**
     * 构建登录场景（Scene），供启动入口直接展示。
     */
    public Scene createScene() {
        // ── 标题 ──
        // 顶部大图标 + 系统名称 + 副标题
        Label emojiLabel = new Label("📊");
        emojiLabel.setFont(Font.font(FONT_FAMILY, 36));

        Label titleLabel = new Label("销售管理信息系统");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        Label subtitleLabel = new Label("进 · 销 · 存 · 退 · 查  一体化管理");
        subtitleLabel.setFont(Font.font(FONT_FAMILY, 13));
        subtitleLabel.setStyle("-fx-text-fill: " + PRIMARY_LIGHT + ";");

        // ── 连接状态 ──
        // 启动时探测数据库连接，用圆点颜色表示连接是否成功
        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER);
        Label dotLabel = new Label();
        Label dbStatusLabel = new Label();
        dbStatusLabel.setFont(Font.font(FONT_FAMILY, 12));
        if (DBConnection.testConnection()) {
            // 连接成功：绿色圆点 + 成功文案
            dotLabel.setText("●");
            dotLabel.setStyle("-fx-text-fill: " + SUCCESS + "; -fx-font-size: 10px;");
            dbStatusLabel.setText("数据库已连接");
            dbStatusLabel.setStyle("-fx-text-fill: " + SUCCESS + "; -fx-font-size: 12px;");
        } else {
            // 连接失败：红色圆点 + 失败文案
            dotLabel.setText("●");
            dotLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 10px;");
            dbStatusLabel.setText("数据库连接失败");
            dbStatusLabel.setStyle("-fx-text-fill: " + ERROR + "; -fx-font-size: 12px;");
        }
        statusBox.getChildren().addAll(dotLabel, dbStatusLabel);

        // ── 表单卡片 ──
        // GridPane 布局：用户名 / 密码两行输入
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER);

        Label userLabel = new Label("👤 用户名");
        userLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        userLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(220);

        Label pwdLabel = new Label("🔑 密  码");
        pwdLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        pwdLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");

        // 标签在左、输入框在右
        form.add(userLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(pwdLabel, 0, 1);
        form.add(passwordField, 1, 1);

        // 表单包进白色卡片：带大圆角与投影阴影
        VBox formCard = new VBox(20, form);
        formCard.setPadding(new Insets(24));
        formCard.setStyle(
            "-fx-background-color: " + SURFACE_CARD + ";" +
            "-fx-background-radius: " + RADIUS_LG + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(43,168,162,0.10), 20, 0, 0, 4);"
        );
        formCard.setAlignment(Pos.CENTER);
        formCard.setMaxWidth(340);

        // ── 按钮 ──
        // 登录按钮：点击后执行 handleLogin()
        Button loginBtn = pillButton("登  录", Variant.PRIMARY);
        loginBtn.setPrefWidth(140);
        loginBtn.setOnAction(e -> handleLogin());

        // 注册新用户按钮：点击后弹出注册对话框
        Button registerBtn = pillButton("注册新用户", Variant.OUTLINE);
        registerBtn.setPrefWidth(140);
        registerBtn.setOnAction(e -> showRegisterDialog());

        // 两个按钮水平居中排列
        HBox buttonBox = new HBox(12, loginBtn, registerBtn);
        buttonBox.setAlignment(Pos.CENTER);

        // ── 消息提示 ──
        // 用于显示登录失败等错误提示
        messageLabel = new Label();
        messageLabel.setFont(Font.font(FONT_FAMILY, 12));
        messageLabel.setStyle("-fx-text-fill: " + CORAL + ";");
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(Double.MAX_VALUE);

        // ── 组装 ──
        // 将所有区块纵向排列，整体居中，底色为页面背景
        VBox root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + SURFACE_BASE + ";");
        root.getChildren().addAll(emojiLabel, titleLabel, subtitleLabel, statusBox, formCard, buttonBox, messageLabel);

        // 回车键登录：在密码框按回车等价于点击登录
        passwordField.setOnAction(e -> handleLogin());

        Scene scene = new Scene(root, 440, 480);
        // 加载全局 CSS；样式缺失时忽略，不影响功能
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}
        return scene;
    }

    /**
     * 处理登录：
     * 1) 校验用户名与密码非空；
     * 2) 调用 UserDAO.login() 校验身份，成功则返回 User 对象；
     * 3) 校验成功后写入 Session（保持登录状态），打开主界面并关闭登录窗口；
     *    失败则提示"用户名或密码错误"。
     */
    private void handleLogin() {
        // 读取输入并去除首尾空格
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // 校验输入非空
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("请输入用户名和密码");
            return;
        }

        // 调用 DAO 校验用户名密码
        User user = userDAO.login(username, password);
        if (user != null) {
            // 登录成功：写入会话，记录当前登录用户
            Session.setCurrentUser(user);  //保持登录状态
            messageLabel.setText("");
            // 打开主界面窗口
            MainUI mainUI = new MainUI();
            Stage mainStage = new Stage();
            mainUI.start(mainStage);  //打开主窗口
            // 关闭当前登录窗口
            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();
        } else {
            // 登录失败
            messageLabel.setText("用户名或密码错误");
        }
    }

    /**
     * 弹出注册新用户对话框（委托给 RegisterUI）。
     */
    private void showRegisterDialog() {
        RegisterUI registerUI = new RegisterUI();
        registerUI.showDialog();
    }
}
