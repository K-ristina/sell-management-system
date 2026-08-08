package com.sellsystem.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接管理类
 * <p>
 * 负责管理 SQL Server 数据库连接，包括：
 * <ul>
 *   <li>加载 JDBC 驱动</li>
 *   <li>提供数据库连接</li>
 *   <li>测试数据库连接状态</li>
 * </ul>
 * <p>
 * 安全说明：连接参数（URL / 用户名 / 密码）不硬编码在源码中，
 * 而是从类路径下的 db.properties 读取。该文件是本地私密配置，
 * 已加入 .gitignore 不会随代码上传，避免数据库凭据泄露。
 */
public class DBConnection {

    // ==================== 连接参数 ====================
    // 配置来源：classpath 根目录下的 db.properties（本地文件，不入库）
    // 键名：db.url / db.user / db.password
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // ==================== 静态初始化 ====================

    /**
     * 读取本地配置文件 db.properties，加载连接参数与 JDBC 驱动。
     * 配置文件缺失时参数保持为 null，后续 getConnection() 会给出明确提示。
     */
    static {
        // 从 classpath 读取 db.properties
        String url = null, user = null, password = null;
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
            }
        } catch (Exception e) {
            System.err.println("读取数据库配置失败: " + e.getMessage());
        }
        URL = url;
        USER = user;
        PASSWORD = password;

        // 加载 JDBC 驱动
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC 驱动加载失败: " + e.getMessage());
        }
    }

    // ==================== 公共方法 ====================

    /**
     * 获取数据库连接
     * <p>
     * 每次调用返回一个新的连接对象，调用方负责在使用完毕后关闭连接。
     *
     * @return 数据库连接
     * @throws SQLException 如果连接失败
     */
    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException(
                    "数据库连接参数未配置：请在 classpath 下创建 db.properties（可参考 db.properties.example）");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 测试数据库连接是否正常
     * <p>
     * 在登录界面初始化时调用，用于显示数据库连接状态。
     *
     * @return true 表示连接成功，false 表示连接失败
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }
}
