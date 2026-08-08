package com.sellsystem.dao;

import com.sellsystem.config.DBConnection;
import com.sellsystem.model.Retreat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 退货记录表数据访问对象（DAO）— 含库存恢复逻辑
 *
 * 功能：封装对 retreat 退货表的所有数据库操作：
 *   - 查询全部退货记录（getAll）
 *   - 退货登记（retreat）：在事务中「恢复库存 + 插入退货记录」，
 *     任一环节出错则整体回滚，保证数据一致性。
 */
public class RetreatDAO {

    /** 查询全部退货记录（按退货编号升序返回） */
    public List<Retreat> getAll() {
        List<Retreat> list = new ArrayList<>();
        String sql = "SELECT 退货编号, 厂商, 商品名, 型号, 单价, 数量, 总金额, 退货年, 退货月, 退货日, 业务员编号 FROM retreat ORDER BY 退货编号";
        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            // 遍历结果集，逐行封装成 Retreat 对象
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("查询退货记录失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 退货登记 — 插入退货记录 + 恢复库存。
     *
     * 整个操作在一个数据库事务中完成：
     *   1. 在 goods 库存表中按「厂商+商品名+型号」查找匹配商品；
     *   2. 找到后把退货数量累加回库存，并刷新总金额；
     *   3. 向 retreat 表插入一条退货记录；
     *   4. 全部成功则提交（commit），任一环节异常则回滚（rollback）。
     * @return 结果消息（空串表示成功）
     */
    public String retreat(Retreat retreat) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            //开启事务
            conn.setAutoCommit(false);

            // 1. 查找库存中匹配的商品并恢复数量
            String findSql = "SELECT 商品编号, 数量 FROM goods WHERE 生产厂商 = ? AND 商品名 = ? AND 型号 = ?";
            PreparedStatement findPs = conn.prepareStatement(findSql);
            findPs.setString(1, retreat.get厂商());
            findPs.setString(2, retreat.get商品名());
            findPs.setString(3, retreat.get型号());
            ResultSet rs = findPs.executeQuery();

            // 库存中没有该商品：回滚并返回错误信息
            if (!rs.next()) {
                conn.rollback();
                return "库存中未找到匹配商品: " + retreat.get厂商() + " " + retreat.get商品名() + " " + retreat.get型号();
            }

            //获取退货物品的商品编号
            int goodsId = rs.getInt("商品编号");

            // 2. 恢复库存：原数量 + 退货数量，并同步刷新总金额
            String restoreSql = "UPDATE goods SET 数量 = 数量 + ?, 总金额 = 单价 * (数量 + ?) WHERE 商品编号 = ?";
            PreparedStatement restorePs = conn.prepareStatement(restoreSql);
            restorePs.setInt(1, retreat.get数量());
            restorePs.setInt(2, retreat.get数量());
            restorePs.setInt(3, goodsId);
            restorePs.executeUpdate();

            // 3. 插入退货记录
            String insertSql = "INSERT INTO retreat (厂商, 商品名, 型号, 单价, 数量, 总金额, 退货年, 退货月, 退货日, 业务员编号) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            insertPs.setString(1, retreat.get厂商());
            insertPs.setString(2, retreat.get商品名());
            insertPs.setString(3, retreat.get型号());
            insertPs.setBigDecimal(4, retreat.get单价());
            insertPs.setInt(5, retreat.get数量());
            insertPs.setBigDecimal(6, retreat.get总金额());
            insertPs.setInt(7, retreat.get退货年());
            insertPs.setInt(8, retreat.get退货月());
            insertPs.setInt(9, retreat.get退货日());
            insertPs.setInt(10, retreat.get业务员编号());
            insertPs.executeUpdate();

            // 4. 全部成功，提交事务
            conn.commit();
            return "";
        } catch (SQLException e) {
            // 出错：回滚事务，保证库存与退货记录的一致性
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("退货登记失败: " + e.getMessage());
            return "退货登记失败: " + e.getMessage();
        } finally {
            // 无论成功失败都恢复自动提交并关闭连接
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException e) {}
        }//无论如何该代码一定运行
    }

    /**
     * 将结果集当前行封装成 Retreat 对象。
     */
    private Retreat mapRow(ResultSet rs) throws SQLException {
        Retreat r = new Retreat();
        r.set退货编号(rs.getInt("退货编号"));
        r.set厂商(rs.getString("厂商"));
        r.set商品名(rs.getString("商品名"));
        r.set型号(rs.getString("型号"));
        r.set单价(rs.getBigDecimal("单价"));
        r.set数量(rs.getInt("数量"));
        r.set总金额(rs.getBigDecimal("总金额"));
        r.set退货年(rs.getInt("退货年"));
        r.set退货月(rs.getInt("退货月"));
        r.set退货日(rs.getInt("退货日"));
        r.set业务员编号(rs.getInt("业务员编号"));
        return r;
    }
}
