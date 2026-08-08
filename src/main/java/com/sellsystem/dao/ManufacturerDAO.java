package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.Manufacturer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 厂商表数据访问对象（DAO）
 *
 * 功能：封装对 manufacturer 厂商表的所有数据库操作，提供增删改查：
 *   - 查询全部厂商（getAll）
 *   - 按名称查询厂商（getByName）
 *   - 新增 / 更新 / 删除厂商（insert / update / delete）
 * 注意：厂商编号为数据库自增列（IDENTITY），插入时由数据库自动生成，
 *      无需也不可显式赋值。
 */
public class ManufacturerDAO {

    /** 查询全部厂商（按厂商编号升序返回） */
    public List<Manufacturer> getAll() {
        List<Manufacturer> list = new ArrayList<>();
        String sql = "SELECT 厂商编号, 厂商名称, 法人代表, 电话, 厂商地址 FROM manufacturer ORDER BY 厂商编号";
        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，逐行通过 mapRow 封装成 Manufacturer 对象
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("查询厂商失败: " + e.getMessage());
        }
        return list;
    }

    /** 按名称查询厂商（用于新增前查重）；不存在返回 null */
    public Manufacturer getByName(String name) {
        String sql = "SELECT 厂商编号, 厂商名称, 法人代表, 电话, 厂商地址 FROM manufacturer WHERE 厂商名称 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                // 命中则封装返回，未命中返回 null
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询厂商失败: " + e.getMessage());
        }
        return null;
    }

    /** 新增厂商。
     *  注意：厂商编号是数据库自增列（IDENTITY），插入时无需也不可显式赋值，
     *  由数据库自动生成，因此这里只插入其余字段。 */
    public boolean insert(Manufacturer m) {
        String sql = "INSERT INTO manufacturer (厂商名称, 法人代表, 电话, 厂商地址) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.get厂商名称());
            ps.setString(2, m.get法人代表());
            ps.setString(3, m.get电话());
            ps.setString(4, m.get厂商地址());
            // 受影响行数 >0 表示插入成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("新增厂商失败: " + e.getMessage());
            return false;
        }
    }

    /** 更新厂商信息（按厂商编号定位更新） */
    public boolean update(Manufacturer m) {
        String sql = "UPDATE manufacturer SET 厂商名称 = ?, 法人代表 = ?, 电话 = ?, 厂商地址 = ? WHERE 厂商编号 = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.get厂商名称());
            ps.setString(2, m.get法人代表());
            ps.setString(3, m.get电话());
            ps.setString(4, m.get厂商地址());
            ps.setInt(5, m.get厂商编号());
            // 受影响行数 >0 表示更新成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新厂商失败: " + e.getMessage());
            return false;
        }
    }

    /** 删除厂商（按厂商编号删除） */
    public boolean delete(int id) {
        String sql = "DELETE FROM manufacturer WHERE 厂商编号 = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            // 受影响行数 >0 表示删除成功
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除厂商失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将查询到的结果集行内容填入厂商对象中，将这个过程封装成方法。
     * 供 getAll / getByName 等查询方法复用，避免重复的取值代码。
     * @param rs 已定位到某一行数据的 ResultSet
     * @return 封装好的 Manufacturer 对象
     * @throws SQLException 数据库访问异常
     */
    private Manufacturer mapRow(ResultSet rs) throws SQLException {
        Manufacturer m = new Manufacturer();
        m.set厂商编号(rs.getInt("厂商编号"));
        m.set厂商名称(rs.getString("厂商名称"));
        m.set法人代表(rs.getString("法人代表"));
        m.set电话(rs.getString("电话"));
        m.set厂商地址(rs.getString("厂商地址"));
        return m;
    }
}
