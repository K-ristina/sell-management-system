package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.Sell;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 销售流水表数据访问对象（DAO）— 含库存校验与扣减逻辑
 *
 * 功能：封装对 sell 销售表的所有数据库操作，包括：
 *   - 查询全部销售记录（getAll）
 *   - 销售登记（sell）：在事务中「校验库存 + 扣减库存 + 插入销售记录」
 *   - 销售统计（getStatsByPeriod / getSummaryByManufacturer / getTotalAmount）
 *   - 员工业绩（getAllEmployeeSales / getSalesByEmployee）
 *   - 查询商品最近销售单价（getLatestSalePrice，供退货界面自动带价）
 */
public class SellDAO {

    /** 查询全部销售记录（按商品编号升序返回） */
    public List<Sell> getAll() {
        List<Sell> list = new ArrayList<>();
        String sql = "SELECT 商品编号, 生产厂商, 商品名, 型号, 单价, 数量, 总金额, 销售年, 销售月, 销售日, 业务员编号 FROM sell ORDER BY 商品编号";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，逐行封装成 Sell 对象
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("查询销售记录失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 销售登记 — 含库存校验 + 扣减。
     *
     * 整个操作在一个数据库事务中完成：
     *   1. 按「厂商+商品名+型号」在 goods 表中查找库存充足（数量 ≥ 销售数量）的商品；
     *   2. 若商品存在但库存不足，返回具体库存不足信息；不存在则返回未找到信息；
     *   3. 找到后扣减库存并同步刷新总金额；
     *   4. 向 sell 表插入销售记录；
     *   5. 全部成功则提交（commit），任一环节异常则回滚（rollback）。
     * @return 结果消息（空串表示成功，否则为错误信息）
     */
    public String sell(Sell sell) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // 开启事务

            // 1. 查找库存中匹配的商品（条件含 数量 >= 销售数量，即库存必须充足）
            String findSql = "SELECT 商品编号, 数量 FROM goods WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ? AND 数量 >= ?";
            PreparedStatement findPs = conn.prepareStatement(findSql);
            findPs.setString(1, sell.get生产厂商());
            findPs.setString(2, sell.get商品名());
            findPs.setString(3, sell.get型号());
            findPs.setInt(4, sell.get数量());
            ResultSet rs = findPs.executeQuery();

            if (!rs.next()) {
                // 库存不足或商品不存在：进一步区分原因给出友好提示
                String checkSql = "SELECT 数量 FROM goods WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ?";
                PreparedStatement checkPs = conn.prepareStatement(checkSql);
                checkPs.setString(1, sell.get生产厂商());
                checkPs.setString(2, sell.get商品名());
                checkPs.setString(3, sell.get型号());
                ResultSet checkRs = checkPs.executeQuery();
                if (checkRs.next()) {
                    // 商品存在但库存不够
                    return "库存不足！当前库存: " + checkRs.getBigDecimal("数量").toString()
                        + "，销售数量: " + sell.get数量();
                }
                // 商品根本不存在
                return "库存中未找到匹配商品: " + sell.get生产厂商() + " " + sell.get商品名() + " " + sell.get型号();
            }

            int goodsId = rs.getInt("商品编号");

            // 2. 扣减库存：原数量 - 销售数量，并同步刷新总金额
            String deductSql = "UPDATE goods SET 数量 = 数量 - ?, 总金额 = 单价 * (数量 - ?) WHERE 商品编号 = ?";
            PreparedStatement deductPs = conn.prepareStatement(deductSql);
            deductPs.setInt(1, sell.get数量());
            deductPs.setInt(2, sell.get数量());
            deductPs.setInt(3, goodsId);
            deductPs.executeUpdate();

            // 3. 插入销售记录
            String insertSql = "INSERT INTO sell (生产厂商, 商品名, 型号, 单价, 数量, 总金额, 销售年, 销售月, 销售日, 业务员编号) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            insertPs.setString(1, sell.get生产厂商());
            insertPs.setString(2, sell.get商品名());
            insertPs.setString(3, sell.get型号());
            insertPs.setBigDecimal(4, sell.get单价());
            insertPs.setInt(5, sell.get数量());
            insertPs.setBigDecimal(6, sell.get总金额());
            insertPs.setInt(7, sell.get销售年());
            insertPs.setInt(8, sell.get销售月());
            insertPs.setInt(9, sell.get销售日());
            insertPs.setInt(10, sell.get业务员编号());
            insertPs.executeUpdate();

            // 4. 全部成功，提交事务
            conn.commit();
            return ""; // 成功
        } catch (SQLException e) {
            // 出错：回滚事务，保证库存与销售记录的一致性
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("销售登记失败: " + e.getMessage());
            return "销售登记失败: " + e.getMessage();
        } finally {
            // 无论成功失败都恢复自动提交并关闭连接
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException e) {}
        }
    }

    // ==================== 销售统计 ====================

    /** 按时间段查询销售明细。
     *  通过 year + 起止月份（startMonth~endMonth）+ 可选具体日 过滤，
     *  结果按生产厂商分组、金额降序排列。 */
    public List<Sell> getStatsByPeriod(int year, int startMonth, int endMonth, Integer day) {
        List<Sell> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT 商品编号, 生产厂商, 商品名, 型号, 单价, 数量, 总金额, 销售年, 销售月, 销售日, 业务员编号 FROM sell WHERE 销售年 = ? AND 销售月 BETWEEN ? AND ?");
        if (day != null) sql.append(" AND 销售日 = ?");
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
            System.err.println("销售统计查询失败: " + e.getMessage());
        }
        return list;
    }

    /** 按厂商汇总销售金额。
     * 返回装字符串数组的List，每个元素为 [厂商名称, 合计销售金额]，
     * 按合计金额降序排列，供统计界面的汇总表使用。 */
    public List<String[]> getSummaryByManufacturer(int year, int startMonth, int endMonth, Integer day) {
        List<String[]> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT 生产厂商, SUM(总金额) AS 合计金额 FROM sell WHERE 销售年 = ? AND 销售月 BETWEEN ? AND ?");
        if (day != null) sql.append(" AND 销售日 = ?");
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
            System.err.println("销售汇总失败: " + e.getMessage());
        }
        return list;
    }

    /** 时间段内总销售金额。
     * 用 ISNULL 保证无记录时返回 0，不返回 null。 */
    public BigDecimal getTotalAmount(int year, int startMonth, int endMonth, Integer day) {
        StringBuilder sql = new StringBuilder(
            "SELECT ISNULL(SUM(总金额), 0) FROM sell WHERE 销售年 = ? AND 销售月 BETWEEN ? AND ?");
        if (day != null) sql.append(" AND 销售日 = ?");

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
            System.err.println("销售总额查询失败: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // ==================== 员工业绩 ====================

    /** 全员销售总额排行。
     * 用 LEFT JOIN 把员工表与销售表关联，未发生销售的员工也列出（销售总额为 0），
     * 结果按销售总额降序排列，供"全员业绩"界面使用。 */
    public List<String[]> getAllEmployeeSales() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT s.业务员编号, e.员工姓名, ISNULL(SUM(s.总金额), 0) AS 销售总额 " +
                    "FROM employee e LEFT JOIN sell s ON e.员工编号 = s.业务员编号 " +
                    "GROUP BY s.业务员编号, e.员工姓名 ORDER BY 销售总额 DESC";
        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // 未发生销售的员工业务员编号可能为 null，显示为 "-"
                String id = rs.getString("业务员编号");
                list.add(new String[]{
                    id != null ? id : "-",
                    rs.getString("员工姓名"),
                    rs.getString("销售总额")
                });
            }
        } catch (SQLException e) {
            System.err.println("业绩查询失败: " + e.getMessage());
        }
        return list;
    }

    /** 按员工编号查询销售明细。
     * 返回某业务员的所有销售记录，按销售日期倒序（最近的在前）。 */
    public List<Sell> getSalesByEmployee(int employeeId) {
        List<Sell> list = new ArrayList<>();
        String sql = "SELECT * FROM sell WHERE 业务员编号 = ? ORDER BY 销售年 DESC, 销售月 DESC, 销售日 DESC";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                // 遍历结果集，逐行封装
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("员工销售查询失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 查询某商品（生产厂商+商品名+型号）最近一次销售的单价。
     * 供退货登记界面自动带出"退款单价"使用；无销售记录时返回 null。
     */
    public BigDecimal getLatestSalePrice(String manufacturer, String name, String model) {
        String sql = "SELECT TOP 1 单价 FROM sell WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ? "
                   + "ORDER BY 销售年 DESC, 销售月 DESC, 销售日 DESC, 商品编号 DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manufacturer);
            ps.setString(2, name);
            ps.setString(3, model);
            try (ResultSet rs = ps.executeQuery()) {
                // 命中则返回最近一次销售单价
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("查询最近销售单价失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 将结果集当前行封装成 Sell 对象。
     */
    private Sell mapRow(ResultSet rs) throws SQLException {
        Sell s = new Sell();
        s.set商品编号(rs.getInt("商品编号"));
        s.set生产厂商(rs.getString("生产厂商"));
        s.set商品名(rs.getString("商品名"));
        s.set型号(rs.getString("型号"));
        s.set单价(rs.getBigDecimal("单价"));
        s.set数量(rs.getInt("数量"));
        s.set总金额(rs.getBigDecimal("总金额"));
        s.set销售年(rs.getInt("销售年"));
        s.set销售月(rs.getInt("销售月"));
        s.set销售日(rs.getInt("销售日"));
        s.set业务员编号(rs.getInt("业务员编号"));
        return s;
    }
}
