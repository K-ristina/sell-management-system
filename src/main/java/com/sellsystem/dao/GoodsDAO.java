package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.Goods;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 现存货品/进货表数据访问对象（DAO）— 库存核心
 *
 * 功能：封装对 goods 进货表（库存表）的所有数据库操作，包括：
 *   - 查询全部进货记录（getAll）
 *   - 进货登记（purchase）：已有同款商品则累加库存，否则新增记录
 *   - 更新库存数量（updateQuantity，delta 可正可负）
 *   - 按厂商+商品名+型号匹配库存（findByManufacturerAndNameAndModel）
 *   - 下拉框联动数据：按厂商取商品名列表 / 按厂商+商品名取型号列表
 *   - 进货统计查询（getStatsByPeriod / getSummaryByManufacturer / getTotalAmount）
 */
public class GoodsDAO {

    /** 查询全部进货记录（按商品编号升序返回） */
    public List<Goods> getAll() {
        List<Goods> list = new ArrayList<>();
        String sql = "SELECT 商品编号, 生产厂商, 商品名, 型号, 单价, 数量, 总金额, 进货年, 进货月, 进货日, 业务员编号 FROM goods ORDER BY 商品编号";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，逐行封装成 Goods 对象
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("查询进货记录失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 进货登记 — 插入进货记录。
     * 若已存在同厂商+同商品+同型号的库存记录，则在原数量上累加并
     * 重新计算总金额（单价×新数量），并刷新进货日期与业务员；
     * 否则作为新商品插入一条完整的新记录。
     */
    public boolean purchase(Goods goods) {
        // 查找是否已有相同商品（同厂商+同商品名+同型号）
        String findSql = "SELECT 商品编号, 数量 FROM goods WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ?";
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement findPs = conn.prepareStatement(findSql);
            findPs.setString(1, goods.get生产厂商());
            findPs.setString(2, goods.get商品名());
            findPs.setString(3, goods.get型号());
            ResultSet rs = findPs.executeQuery();

            if (rs.next()) {
                // 已有库存 → 累加数量和金额
                int id = rs.getInt("商品编号");
                BigDecimal oldQty = rs.getBigDecimal("数量");
                BigDecimal newQty = oldQty.add(goods.get数量());      // 新数量 = 原库存 + 本次进货量
                BigDecimal newTotal = goods.get单价().multiply(newQty); // 新总金额 = 单价 × 新数量


                //得到新的金额和数量，更新数据库
                String updateSql = "UPDATE goods SET 数量 = ?, 总金额 = ?, 单价 = ?, 进货年 = ?, 进货月 = ?, 进货日 = ?, 业务员编号 = ? WHERE 商品编号 = ?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setBigDecimal(1, newQty);
                updatePs.setBigDecimal(2, newTotal);
                updatePs.setBigDecimal(3, goods.get单价());
                updatePs.setInt(4, goods.get进货年());
                updatePs.setInt(5, goods.get进货月());
                updatePs.setInt(6, goods.get进货日());
                updatePs.setInt(7, goods.get业务员编号());
                updatePs.setInt(8, id);
                // 受影响行数 >0 表示更新成功
                return updatePs.executeUpdate() > 0;
            } else {
                // 新商品 → 插入新记录
                String insertSql = "INSERT INTO goods (生产厂商, 商品名, 型号, 单价, 数量, 总金额, 进货年, 进货月, 进货日, 业务员编号) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setString(1, goods.get生产厂商());
                insertPs.setString(2, goods.get商品名());
                insertPs.setString(3, goods.get型号());
                insertPs.setBigDecimal(4, goods.get单价());
                insertPs.setBigDecimal(5, goods.get数量());
                insertPs.setBigDecimal(6, goods.get总金额());
                insertPs.setInt(7, goods.get进货年());
                insertPs.setInt(8, goods.get进货月());
                insertPs.setInt(9, goods.get进货日());
                insertPs.setInt(10, goods.get业务员编号());
                // 受影响行数 >0 表示插入成功
                return insertPs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("进货登记失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新库存数量。
     * delta 为正值表示增加库存（如退货恢复），负值表示减少库存（如销售扣减），
     * 同时用「单价 × 最新数量」刷新总金额，保证金额始终与数量一致。
     */
    public boolean updateQuantity(int goodsId, BigDecimal delta) {
        String sql = "UPDATE goods SET 数量 = 数量 + ?, 总金额 = 单价 * (数量 + ?) WHERE 商品编号 = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setBigDecimal(2, delta);
            ps.setInt(3, goodsId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新库存失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 按条件查询库存（用于销售时匹配商品）。
     * 通过「生产厂商 + 商品名 + 型号」精确定位一条库存记录。
     */
    public Goods findByManufacturerAndNameAndModel(String manufacturer, String name, String model) {
        String sql = "SELECT * FROM goods WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ?";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            ps.setString(2, name);
            ps.setString(3, model);
            try (ResultSet rs = ps.executeQuery()) {

                //定位成功，则把查询结果存入Goods对象中
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("查询库存失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 查询某生产厂商在进货表中所有的商品名（去重）。
     * 供销售登记界面的"商品名"下拉框使用。
     */
    public List<String> getDistinctNamesByManufacturer(String manufacturer) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT 商品名 FROM goods WHERE 生产厂商 = ? ORDER BY 商品名";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("查询商品名失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 查询某生产厂商 + 某商品名在进货表中所有的型号（去重）。
     * 供销售登记界面的"型号"下拉框使用。
     */
    public List<String> getDistinctModels(String manufacturer, String name) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT 型号 FROM goods WHERE 生产厂商 = ? AND 商品名 = ? ORDER BY 型号";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("查询型号失败: " + e.getMessage());
        }
        return list;
    }

    // ==================== 进货统计 ====================

    /** 按时间段查询进货明细（day=null 则不筛选日）。
     *  通过 year + 起止月份（startMonth~endMonth）+ 可选具体日 过滤，
     *  结果按生产厂商分组、金额降序排列。 */
    public List<Goods> getStatsByPeriod(int year, int startMonth, int endMonth, Integer day) {
        List<Goods> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT 商品编号, 生产厂商, 商品名, 型号, 单价, 数量, 总金额, 进货年, 进货月, 进货日, 业务员编号 FROM goods WHERE 进货年 = ? AND 进货月 BETWEEN ? AND ?");
        //动态判断是否传天数
            if (day != null) sql.append(" AND 进货日 = ?");
        sql.append(" ORDER BY 生产厂商, 总金额 DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, year);
            ps.setInt(2, startMonth);
            ps.setInt(3, endMonth);
            if (day != null) ps.setInt(4, day);
            try (ResultSet rs = ps.executeQuery()) {
                // 遍历结果集，逐行封装
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("进货统计查询失败: " + e.getMessage());
        }
        return list;
    }

    /** 按厂商汇总进货金额。
     * 返回装字符串数组的List，每个元素为 [厂商名称, 合计进货金额]，
     * 按合计金额降序排列，供统计界面的汇总表使用。
    */
    public List<String[]> getSummaryByManufacturer(int year, int startMonth, int endMonth, Integer day) {
        List<String[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT 生产厂商, SUM(总金额) AS 合计金额 FROM goods WHERE 进货年 = ? AND 进货月 BETWEEN ? AND ?");
        if (day != null) sql.append(" AND 进货日 = ?");
        sql.append(" GROUP BY 生产厂商 ORDER BY 合计金额 DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, year);
            ps.setInt(2, startMonth);
            ps.setInt(3, endMonth);
            if (day != null) ps.setInt(4, day);
            try (ResultSet rs = ps.executeQuery()) {
                // 每个厂商一行：厂商名 + 合计金额字符串
                while (rs.next()) {
                    list.add(new String[]{rs.getString("生产厂商"), rs.getString("合计金额")});
                }
            }
        } catch (SQLException e) {
            System.err.println("进货汇总失败: " + e.getMessage());
        }
        return list;
    }

    /** 时间段内总进货金额。
     * 用 ISNULL 保证无记录时返回 0，不返回 null。 */
    public BigDecimal getTotalAmount(int year, int startMonth, int endMonth, Integer day) {
        StringBuilder sql = new StringBuilder(
            "SELECT ISNULL(SUM(总金额), 0) FROM goods WHERE 进货年 = ? AND 进货月 BETWEEN ? AND ?");
        if (day != null) sql.append(" AND 进货日 = ?");

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, year);
            ps.setInt(2, startMonth);
            ps.setInt(3, endMonth);
            if (day != null) ps.setInt(4, day);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("进货总额查询失败: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    /**
     * 将结果集当前行封装成 Goods 对象。
     * 供 getAll / findByManufacturerAndNameAndModel / getStatsByPeriod 复用。
     */
    private Goods mapRow(ResultSet rs) throws SQLException {
        Goods g = new Goods();
        g.set商品编号(rs.getInt("商品编号"));
        g.set生产厂商(rs.getString("生产厂商"));
        g.set商品名(rs.getString("商品名"));
        g.set型号(rs.getString("型号"));
        g.set单价(rs.getBigDecimal("单价"));
        g.set数量(rs.getBigDecimal("数量"));
        g.set总金额(rs.getBigDecimal("总金额"));
        g.set进货年(rs.getInt("进货年"));
        g.set进货月(rs.getInt("进货月"));
        g.set进货日(rs.getInt("进货日"));
        g.set业务员编号(rs.getInt("业务员编号"));
        return g;
    }
}
