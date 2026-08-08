package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户表数据访问对象（DAO）
 *
 * 功能：封装对 userdb 用户表的所有数据库操作，包括：
 *   - 登录验证（login）
 *   - 注册新用户（register）
 *   - 修改密码（changePassword）
 * 所有方法都使用 PreparedStatement 参数化查询，防止 SQL 注入；
 * 数据库连接统一由 DBConnection 提供。
 */
public class UserDAO {

    /**
     * 用户登录验证。
     * 按「用户名 + 密码」精确匹配查询 userdb 表，
     * 匹配成功则封装为 User 对象返回；用户名或密码错误返回 null。
     * @return 成功返回 User 对象，失败返回 null
     */
    public User login(String username, String password) {
        // 参数化查询：用户名和密码同时匹配才登录成功
        String sql = "SELECT 用户编号, 用户名, 密码 FROM userdb WHERE 用户名 = ? AND 密码 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            // 绑定查询参数，防止 SQL 注入
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                //执行查询
                if (rs.next()) {
                    // 查询到记录：将结果集封装成 User 对象
                    User user = new User();
                    user.set用户编号(rs.getInt("用户编号"));
                    user.set用户名(rs.getString("用户名"));
                    user.set密码(rs.getString("密码"));
                    return user;
                }
            }
        } catch (SQLException e) {
            // 数据库异常时打印错误信息
            System.err.println("登录查询失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 注册新用户。
     * 先检查用户名是否已存在（存在则拒绝注册），
     * 不存在则向 userdb 表插入一条新记录。
     * @return 成功返回 true，用户名已存在返回 false
     */
    public boolean register(String username, String password) {
        // 先检查用户名是否已存在
        String checkSql = "SELECT COUNT(*) FROM userdb WHERE 用户名 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // 计数大于 0 说明用户名已被占用
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 用户名已存在
                }
            }
        } catch (SQLException e) {
            System.err.println("用户名检查失败: " + e.getMessage());
            return false;
        }

        // 插入新用户
        String insertSql = "INSERT INTO userdb (用户名, 密码) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            // executeUpdate 返回受影响行数，>0 表示插入成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("用户注册失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 修改密码。
     * 先按「用户编号 + 原密码」验证原密码是否正确，
     * 验证通过后把密码更新为新密码。
     * @return 成功返回 true
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // 验证原密码
        String checkSql = "SELECT COUNT(*) FROM userdb WHERE 用户编号 = ? AND 密码 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setString(2, oldPassword);
            try (ResultSet rs = ps.executeQuery()) {
                // 查不到记录说明原密码不正确
                if (!rs.next() || rs.getInt(1) == 0) {
                    return false; // 原密码不正确
                }
            }
        } catch (SQLException e) {
            System.err.println("密码验证失败: " + e.getMessage());
            return false;
        }

        // 更新密码
        String updateSql = "UPDATE userdb SET 密码 = ? WHERE 用户编号 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateSql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            // 受影响行数 >0 表示更新成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("密码更新失败: " + e.getMessage());
            return false;
        }
    }
}
