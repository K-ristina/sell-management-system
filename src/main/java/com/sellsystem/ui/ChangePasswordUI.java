package com.sellsystem.ui;

import com.sellsystem.dao.UserDAO;
import com.sellsystem.util.Session;
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
 * 修改密码界面
 *
 * 功能：以模态对话框的形式让当前登录用户修改自己的登录密码。
 * 用户输入原密码、新密码、确认新密码后，先做本地校验，
 * 再调用 UserDAO 校验原密码并更新数据库中的密码。
 */
public class ChangePasswordUI {

    // 数据访问对象：负责用户表的查询与更新
    private final UserDAO userDAO = new UserDAO();

    /**
     * 显示修改密码对话框（模态，阻塞主窗口操作）。
     */
    public void showDialog() {
        // 创建模态对话框窗口
        Stage dialog = new Stage();
        dialog.setTitle("修改密码");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);

        // ── 标题 ──
        // 顶部图标 + 大标题，居中展示
        Label iconLabel = new Label("🔐");
        iconLabel.setFont(Font.font(FONT_FAMILY, 28));
        Label titleLabel = new Label("修改密码");
        titleLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");
        VBox headerBox = new VBox(4, iconLabel, titleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // ── 表单 ──
        // GridPane 布局：原密码 / 新密码 / 确认新密码 三行输入
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER);

        // 原密码输入框：用于校验旧密码是否正确
        PasswordField oldPwdField = new PasswordField();
        oldPwdField.setPromptText("请输入原密码");

        // 新密码输入框（长度上限 10 字符）
        PasswordField newPwdField = new PasswordField();
        newPwdField.setPromptText("请输入新密码（≤10字符）");

        // 确认新密码输入框：与 newPwdField 比对以预防输入错误
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("请再次输入新密码");

        // 将标签与输入框按行排入网格
        form.add(fieldLabel("🔒 原密码"), 0, 0);
        form.add(oldPwdField, 1, 0);
        form.add(fieldLabel("🔑 新密码"), 0, 1);
        form.add(newPwdField, 1, 1);
        form.add(fieldLabel("✅ 确认新密码"), 0, 2);
        form.add(confirmField, 1, 2);

        // ── 消息 ──
        // 用于显示校验失败或修改失败的错误提示
        Label msgLabel = new Label();
        msgLabel.setFont(Font.font(FONT_FAMILY, 12));
        msgLabel.setStyle("-fx-text-fill: " + CORAL + ";");
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setMaxWidth(Double.MAX_VALUE);

        // ── 按钮 ──
        // 确认修改按钮：点击后执行校验并调用 DAO 更新密码
        Button confirmBtn = pillButton("确认修改", Variant.PRIMARY);
        confirmBtn.setPrefWidth(140);
        confirmBtn.setOnAction(e -> {
            // 读取三个输入框的内容
            String oldPwd = oldPwdField.getText();
            String newPwd = newPwdField.getText();
            String confirm = confirmField.getText();

            // 校验1：原密码与新密码不能为空
            if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                msgLabel.setText("密码不能为空"); return;
            }
            // 校验2：两次输入的新密码必须一致
            if (!newPwd.equals(confirm)) {
                msgLabel.setText("两次输入的新密码不一致"); return;
            }
            // 校验3：新密码长度不能超过 10 个字符
            if (newPwd.length() > 10) {
                msgLabel.setText("密码不能超过10个字符"); return;
            }

            // 调用 DAO：传入当前登录用户 ID、原密码与新密码
            // 数据库层会校验原密码是否正确，正确则更新为新密码
            boolean success = userDAO.changePassword(Session.getCurrentUserId(), oldPwd, newPwd);
            if (success) {
                // 修改成功：弹出提示并关闭对话框
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "密码修改成功！");
                alert.showAndWait();
                dialog.close();
            } else {
                // 修改失败：多为原密码输入不正确
                msgLabel.setText("原密码不正确");
            }
        });

        // ── 组装界面 ──
        // 所有区块纵向排列，整体背景为卡片白
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + SURFACE_CARD + ";");
        root.getChildren().addAll(headerBox, form, confirmBtn, msgLabel);

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
