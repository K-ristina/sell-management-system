# 销售管理信息系统

面向中小型销售公司的「**进 · 销 · 存 · 退 · 查**」一体化桌面管理系统。
基于 **JavaFX** 构建图形界面，数据存储于 **SQL Server**，采用三层架构，UI 为统一的 Flip7 风格。

##  功能特性

-  **用户管理**：登录 / 注册 / 修改密码（Session 保持登录状态）
-  **进货登记**：同款商品自动累加库存；厂商不在列表中可一键登记新厂商
-  **销售登记**：厂商 → 商品名 → 型号 **三级联动下拉**，实时显示当前库存，**防止超卖**
-  **退货登记**：联动下拉选择商品，退款单价**自动带出最近一次销售价**（不可手改）
-  **统计报表**：今日 / 本月 / 本季度 / 本年度 的进货与销售统计（明细 + 按厂商汇总 + 总额）
-  **员工业绩**：全员销售业绩排行、按员工查询销售明细及合计
-  **数据管理**：员工表 / 厂商表增删改；进货 / 销售 / 退货表查询
-  **仪表盘首页**：今日/本月销售与进货额统计卡片 + 快捷操作

## 🛠 技术栈

| 技术 | 说明 |
|------|------|
| Java 25 | 开发语言 |
| JavaFX 21 | 桌面 UI 框架 |
| SQL Server | 数据库 |
| JDBC（mssql-jdbc 12.6.3） | 数据访问 |
| Maven | 构建工具 |

##  系统架构

采用三层架构，UI 层与数据库完全解耦：

```
表现层（ui 包：JavaFX 界面）
      │ 调用
数据访问层（dao 包：DAO 封装全部 SQL 与事务）
      │ JDBC
数据层（config 包：DBConnection 统一管理连接）
      │
SQL Server
```

- 所有 SQL 均使用 `PreparedStatement` 参数化查询，**防 SQL 注入**；
- 销售 / 退货在**数据库事务**内完成「改库存 + 写流水」，保证数据一致性；
- 金额全程使用 `BigDecimal`（数据库 `money`/`numeric`），无浮点误差。

##  数据库设计

共 6 张表：

| 表 | 含义 | 说明 |
|----|------|------|
| userdb | 系统用户 | 登录账号 |
| employee | 员工 | 业务员信息 |
| manufacturer | 厂商 | 以厂商名称为主键 |
| goods | 进货 / 库存 | 库存核心表 |
| sell | 销售流水 | 记录每笔销售 |
| retreat | 退货记录 | 记录每笔退货 |

- goods / sell / retreat 通过「业务员编号」关联 employee，通过「生产厂商 / 厂商」关联 manufacturer（共 6 个外键）；
- 商品以「生产厂商 + 商品名 + 型号」联合唯一定位；
- 表结构详见 `图片/ER图.svg`。

## 快速开始

### 环境要求

- JDK 25
- Maven 3.x
- SQL Server（已还原 `sellsystem` 数据库）

### 1. 准备数据库

用 SQL Server Management Studio 还原根目录下的 `sellsystem.bak`（数据库名为 `sellsystem`）。

### 2. 配置数据库连接

将 `src/main/resources/db.properties.example` 复制为 `src/main/resources/db.properties`，填入真实的连接参数：

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=sellsystem;encrypt=false;trustServerCertificate=true;
db.user=你的数据库用户名
db.password=你的数据库密码
```

>  `db.properties` 已加入 `.gitignore`，**不会被提交**，连接凭据由你本地保管。

### 3. 运行

```bash
mvn javafx:run
```

或在 IDE 中直接运行主类 `com.sellsystem.MainApp`。

##  项目结构

```
src/main/java/com/sellsystem/
├── MainApp.java          # 程序入口
├── config/               # DBConnection 数据库连接管理
├── dao/                  # 数据访问层（6 个 DAO）
├── model/                # 实体类（与表对应）
├── ui/                   # JavaFX 界面
│   ├── LoginUI / MainUI  # 登录与主界面
│   ├── Purchase/Sales/ReturnUI   # 三大登记界面
│   ├── SalesStatsUI / PurchaseStatsUI / PerformanceUI  # 统计与业绩
│   ├── DataTableUI       # 数据表维护
│   └── Theme             # 统一主题样式
└── util/                 # Session 会话管理
src/main/resources/
├── css/style.css         # 全局样式
└── db.properties(.example)  # 数据库连接配置（本地，不入库）
```

##  打包为可执行文件

可使用 JDK 自带的 `jpackage` 将项目打包为 Windows 可执行程序。非模块化 JavaFX 应用打包时需要一个不继承 `Application` 的启动类（如 `Launcher`）作为入口，并配合 `--runtime-image` 使用，详情可参考 JDK 文档。

##  说明

本项目为「数据库与信息系统基础课程设计」作品，用于学习与演示。数据库连接凭据不入库，请在本地配置后运行。
