package com.sellsystem;

import com.sellsystem.ui.LoginUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 销售管理信息系统 — 主入口
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("销售管理信息系统");

        // 从登录界面开始
        LoginUI loginUI = new LoginUI();
        Scene loginScene = loginUI.createScene();

        // 加载全局样式
        String css = getClass().getResource("/css/style.css").toExternalForm();
        loginScene.getStylesheets().add(css);

        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
