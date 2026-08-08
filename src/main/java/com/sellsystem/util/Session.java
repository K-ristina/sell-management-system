package com.sellsystem.util;

import com.sellsystem.model.User;

/**
 * 会话管理 — 保存当前登录用户信息
 */
public class Session {

    private static User currentUser;

    //私有构造方法，不能创建对象
    private Session() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.get用户编号() : 0;
    }

    public static String getCurrentUsername() {
        return currentUser != null ? currentUser.get用户名() : "";
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * 注销当前会话
     */
    public static void logout() {
        currentUser = null;
    }
}
