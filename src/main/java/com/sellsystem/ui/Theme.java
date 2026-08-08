package com.sellsystem.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Flip7 风格主题常量与工厂方法。
 *
 * 语义约定：
 *   Teal  (#2BA8A2) — 主色：导航、header、统计、正向操作
 *   Gold  (#FFD23F) — 强调：CTA 按钮、关键金额、高亮
 *   Coral (#EF6C4A) — 警告：退货、删除、错误状态
 *   Cream (#FFF8E7) — 输入底色
 *   SurfaceBase (#EFF8F7) — 页面背景
 *
 * 本类职责：
 *   1) 统一集中定义全局的颜色、字体、圆角等设计常量，供整个 UI 复用，
 *      避免各界面中硬编码样式值，便于整体换肤与维护；
 *   2) 提供若干"工厂方法"（pillButton / sectionTitle / accentCard 等），
 *      封装常用的控件创建逻辑，使业务界面代码更简洁一致。
 */
public final class Theme {

    // ── 色彩 ──────────────────────────────────────────────
    // 主色调 Teal 系：用于导航栏、页头标题、统计卡片及正向操作按钮
    public static final String PRIMARY_TEAL    = "#2BA8A2";
    public static final String PRIMARY_LIGHT   = "#3CC4BD";
    public static final String PRIMARY_DARK    = "#1E8C86";
    public static final String PRIMARY_BG      = "#E8F6F5";

    // 强调色 Gold 系：用于 CTA 按钮、关键金额、高亮显示
    public static final String ACCENT_GOLD     = "#FFD23F";
    public static final String ACCENT_LIGHT    = "#FFE47A";
    public static final String ACCENT_DARK     = "#E6B800";

    // 警示色 Coral 系：用于退货、删除、错误状态
    public static final String CORAL           = "#EF6C4A";
    public static final String CORAL_LIGHT     = "#FF8A6A";
    public static final String CORAL_DARK      = "#D45233";

    // 辅助色：奶油色作输入框底色、天蓝色作点缀
    public static final String CREAM           = "#FFF8E7";
    public static final String SKY_BLUE        = "#5DADE2";

    // 背景色：页面基底与卡片底色
    public static final String SURFACE_BASE    = "#EFF8F7";
    public static final String SURFACE_CARD    = "#FFFFFF";

    // 状态色：成功 / 错误提示文字
    public static final String SUCCESS         = "#27AE60";
    public static final String ERROR           = "#E74C3C";

    // ── 字体 ──────────────────────────────────────────────
    // 全局字体统一使用微软雅黑，保证中文显示效果
    public static final String FONT_FAMILY = "Microsoft YaHei";

    // ── 圆角（px） ─────────────────────────────────────────
    // 圆角从大到小划分档位，供不同尺寸的卡片、按钮、徽标使用
    public static final String RADIUS_SM    = "8px";
    public static final String RADIUS_MD    = "16px";
    public static final String RADIUS_LG    = "24px";
    public static final String RADIUS_XL    = "32px";
    public static final String RADIUS_ROUND = "999px";

    private Theme() { /* 工具类禁止实例化 */ }

    // ═══════════════════════════════════════════════════════
    // 工厂方法
    // ═══════════════════════════════════════════════════════

    /** 变体枚举：定义按钮可用的 4 种色彩变体 */
    public enum Variant { PRIMARY, TEAL, CORAL, OUTLINE }

    /**
     * Pill 形按钮（胶囊/圆角按钮）。
     * 通过添加不同的 CSS 样式类来控制外观变体。
     * @param text    按钮文字
     * @param variant 色彩变体：PRIMARY=金色CTA, TEAL=主色, CORAL=警示, OUTLINE=描边
     */
    public static Button pillButton(String text, Variant variant) {
        // 创建按钮并挂载基础样式类
        Button btn = new Button(text);
        btn.getStyleClass().add("pill-button");

        // 根据变体选择对应的 CSS 类，实现不同颜色主题
        switch (variant) {
            case PRIMARY -> btn.getStyleClass().add("pill-primary");
            case TEAL    -> btn.getStyleClass().add("pill-teal");
            case CORAL   -> btn.getStyleClass().add("pill-coral");
            case OUTLINE -> btn.getStyleClass().add("pill-outline");
        }
        return btn;
    }

    /**
     * 带 emoji + 虚线底边的区块标题。
     * 返回一个 VBox：上行是「emoji + 加粗标题文字」，
     * 下行是虚线分隔符，用于界面各功能区的分组标题。
     */
    public static VBox sectionTitle(String emoji, String text) {
        // emoji 图标标签
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(FONT_FAMILY, 20));

        // 标题文字标签（加粗、深青色）
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 18));
        textLabel.setStyle("-fx-text-fill: " + PRIMARY_DARK + ";");

        // 将图标与文字水平排列
        HBox header = new HBox(8, emojiLabel, textLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        // 标题下方虚线下划线
        Separator line = new Separator();
        line.getStyleClass().add("dashed-separator");

        // 组装成纵向容器返回
        VBox box = new VBox(6, header, line);
        box.setPadding(new Insets(0));
        return box;
    }

    /**
     * 左侧带彩色强调条的白色卡片容器。
     * 卡片左侧是一条 6px 的彩色竖条，右侧是内容区域，
     * 用于突出展示表单、统计等核心信息。
     * @param content     卡片内容节点
     * @param accentColor 左侧条颜色（如 PRIMARY_TEAL / ACCENT_GOLD / CORAL）
     */
    public static HBox accentCard(Region content, String accentColor) {
        // 内容区填充并允许横向扩展
        content.setPadding(new Insets(16));
        HBox.setHgrow(content, Priority.ALWAYS);

        // 左侧彩色强调竖条（固定宽度 6px，圆角）
        Pane leftBar = new Pane();
        leftBar.setMinWidth(6);
        leftBar.setPrefWidth(6);
        leftBar.setMaxWidth(6);
        leftBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 3px;");

        // 组装卡片：左侧条 + 内容
        HBox card = new HBox(0, leftBar, content);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(0));
        return card;
    }

    /**
     * 金色合计金额 badge（总金额徽标）。
     * 金黄色圆角背景 + 深青色加粗金额文字，常用于统计页底部展示总额。
     */
    public static Label totalBadge(String prefix, String amount) {
        Label badge = new Label(prefix + " ¥ " + amount);
        badge.setFont(Font.font(FONT_FAMILY, FontWeight.EXTRA_BOLD, 16));
        badge.setStyle(
            "-fx-background-color: " + ACCENT_GOLD + ";" +
            "-fx-text-fill: " + PRIMARY_DARK + ";" +
            "-fx-padding: 8px 20px;" +
            "-fx-background-radius: " + RADIUS_ROUND + ";"
        );
        badge.setAlignment(Pos.CENTER_RIGHT);
        badge.setMaxWidth(Double.MAX_VALUE);
        return badge;
    }

    /**
     * 创建带虚线分割的下方区域。
     * 复用 CSS 类 "dashed-separator"，用于界面中分隔上下两个功能区块。
     */
    public static Separator dashedSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("dashed-separator");
        return sep;
    }
}
