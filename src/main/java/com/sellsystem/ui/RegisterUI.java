package com.sellsystem.ui;

import com.sellsystem.dao.UserDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static com.sellsystem.ui.Theme.*;

/**
 * 用户注册界面 — Flip7 风格
 *
 * 功能：以模态对话框的形式提供新用户注册功能。
 * 用户输入用户名、密码、确认密码后，校验格式并调用 UserDAO
 * 将新用户写入数据库，注册成功后自动关闭对话框。
 */
public class RegisterUI {

    // 数据访问对象：负责与数据库中的用户表交互
    private final UserDAO userDAO = new UserDAO();

    /**
     * 显示注册对话框（模态，阻塞主窗口操作，注册完成前无法切换）。
     */
    public void showDialog() {
        // 创建模态对话框窗口
        Stage dialog = new Stage();
        dialog.setTitle("注册新用户");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        // ── 标题 ──
        // 顶部图标 + 大标题，居中展示
        Label iconLabel = new Label("📝");
        iconLabel.setFont(Font.font(FONT_FAMILY, 28));
        Label titleLabel = new Label("注册新用户");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        VBox headerBox = new VBox(4, iconLabel, titleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // ── 表单 ──
        // 使用 GridPane 布局用户名、密码、确认密码三个输入项
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER);

        // 用户名输入框（提示字符数上限为 10）
        TextField usernameField = new TextField();
        usernameField.setPromptText("请输入用户名（≤10字符）");
        usernameField.setPrefWidth(220);

        // 密码输入框（PasswordField 隐藏明文）
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码（≤10字符）");

        // 确认密码输入框：与密码框比对以预防输入错误
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("请再次输入密码");

        // 将标签与输入框按行排入网格：第0列标签，第1列输入框
        form.add(fieldLabel("👤 用户名"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(fieldLabel("🔑 密  码"), 0, 1);
        form.add(passwordField, 1, 1);
        form.add(fieldLabel("✅ 确认密码"), 0, 2);
        form.add(confirmField, 1, 2);

        // ── 消息 ──
        // 用于显示校验失败或注册失败的错误提示文字
        Label msgLabel = new Label();
        msgLabel.setFont(Font.font(FONT_FAMILY, 12));
        msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(Double.MAX_VALUE);

        // ── 按钮 ──
        // 注册按钮：点击后执行输入校验与数据库写入
        Button registerBtn = pillButton("注  册", Variant.PRIMARY);
        registerBtn.setPrefWidth(140);
        registerBtn.setOnAction(e -> {
            // 读取三个输入框的内容
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            // 校验1：用户名和密码不能为空
            if (username.isEmpty() || password.isEmpty()) {
                msgLabel.setText("用户名和密码不能为空"); return;
            }
            // 校验2：两次输入的密码必须一致
            if (!password.equals(confirm)) {
                msgLabel.setText("两次输入的密码不一致"); return;
            }
            // 校验3：用户名和密码长度均不能超过 10 个字符
            if (username.length() > 10 || password.length() > 10) {
                msgLabel.setText("用户名和密码均不能超过10个字符"); return;
            }

            // 调用 DAO 尝试注册，返回是否成功
            boolean success = userDAO.register(username, password);
            if (success) {
                // 注册成功：弹出提示并关闭对话框
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "注册成功！请使用新账号登录。");
                alert.showAndWait();
                dialog.close();
            } else {
                // 注册失败（多为用户名重复）
                msgLabel.setText("用户名已存在，请更换");
            }
        });

        // ── 组装界面 ──
        // 将所有区块纵向排列并设置整体背景为卡片白
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + SURFACE_CARD + ";");
        root.getChildren().addAll(headerBox, form, registerBtn, msgLabel);

        // 创建场景并加载全局样式表；CSS 缺失时忽略，不影响功能
        Scene scene = new Scene(root, 400, 370);
        try {
            String css = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) {}
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * 表单字段左侧的标签工厂方法：
     * 统一生成加粗、深青色的小号标签，保持各界面风格一致。
     */
    private static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 13));
        lbl.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        return lbl;
    }
}
