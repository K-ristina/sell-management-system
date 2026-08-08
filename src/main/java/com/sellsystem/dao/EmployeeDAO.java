package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.Employee;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工表数据访问对象（DAO）
 *
 * 功能：封装对 employee 员工表的所有数据库操作，提供增删改查：
 *   - 查询全部员工（getAll）
 *   - 按编号查询单个员工（getById）
 *   - 新增员工（insert）
 *   - 更新员工信息（update）
 *   - 删除员工（delete）
 * 所有方法都使用参数化 SQL，防止 SQL 注入。
 */
public class EmployeeDAO {

    /** 查询全部员工（按员工编号升序返回） */
    public List<Employee> getAll() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT 员工编号, 员工姓名, 员工电话, 员工地址 FROM employee ORDER BY 员工编号";
        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，把每一行封装成 Employee 对象加入列表
            while (rs.next()) {
                Employee e = new Employee();
                e.set员工编号(rs.getInt("员工编号"));
                e.set员工姓名(rs.getString("员工姓名"));
                e.set员工电话(rs.getString("员工电话"));
                e.set员工地址(rs.getString("员工地址"));
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("查询员工失败: " + e.getMessage());
        }
        return list;
    }

    /** 按编号查询单个员工；不存在返回 null */
    public Employee getById(int id) {
        String sql = "SELECT 员工编号, 员工姓名, 员工电话, 员工地址 FROM employee WHERE 员工编号 = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                // 命中则封装返回，未命中返回 null
                if (rs.next()) {
                    Employee e = new Employee();
                    e.set员工编号(rs.getInt("员工编号"));
                    e.set员工姓名(rs.getString("员工姓名"));
                    e.set员工电话(rs.getString("员工电话"));
                    e.set员工地址(rs.getString("员工地址"));
                    return e;
                }
            }
        } catch (SQLException e) {
            System.err.println("查询员工失败: " + e.getMessage());
        }
        return null;
    }

    /* 新增员工（员工编号由数据库自增生成） */
    public boolean insert(Employee emp) {
        String sql = "INSERT INTO employee (员工姓名, 员工电话, 员工地址) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.get员工姓名());
            ps.setString(2, emp.get员工电话());
            ps.setString(3, emp.get员工地址());
            // 受影响行数 >0 表示插入成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("新增员工失败: " + e.getMessage());
            return false;
        }
    }

    /** 更新员工信息（按员工编号定位更新） */
    public boolean update(Employee emp) {
        String sql = "UPDATE employee SET 员工姓名 = ?, 员工电话 = ?, 员工地址 = ? WHERE 员工编号 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emp.get员工姓名());
            ps.setString(2, emp.get员工电话());
            ps.setString(3, emp.get员工地址());
            ps.setInt(4, emp.get员工编号());
            // 受影响行数 >0 表示更新成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新员工失败: " + e.getMessage());
            return false;
        }
    }

    /** 删除员工（按员工编号删除） */
    public boolean delete(int id) {
        String sql = "DELETE FROM employee WHERE 员工编号 = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            // 受影响行数 >0 表示删除成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除员工失败: " + e.getMessage());
            return false;
        }
    }
}
