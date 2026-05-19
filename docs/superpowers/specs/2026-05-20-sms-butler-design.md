# 短信助手 App — 设计文档

> 记录 Android 端收到的短信，提取发送方和手机号，方便用户查询手机号绑定了哪些服务

## 技术栈

| 项目 | 选择 |
|------|------|
| 平台 | Android 原生 |
| 语言 | Kotlin |
| 最低 SDK | Android 12 (API 31) |
| UI | Jetpack Compose + Material Design 3 (Material You) |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt |
| 本地存储 | Room + DataStore (设置) |
| 异步 | Kotlin Coroutines + Flow |
| 导航 | Compose Navigation |
| 图片 | Coil |

## 架构

```
app/
├── data/
│   ├── local/           # Room DB + DAO
│   ├── model/           # 数据实体
│   └── repository/      # Repository 实现
├── domain/
│   ├── model/           # 领域模型
│   ├── repository/      # Repository 接口
│   └── usecase/         # 业务用例
├── service/
│   └── SmsNotificationListener  # 通知监听服务 (获取短信来源)
├── ui/
│   ├── navigation/      # 导航
│   ├── screen/          # 各页面
│   │   ├── home/        # 首页 - 记录列表
│   │   ├── detail/      # 号码详情页
│   │   ├── search/      # 搜索页
│   │   ├── settings/    # 设置页
│   │   └── stats/       # 分类统计页
│   └── theme/           # Material You 主题
│   └── component/       # 公共 Compose 组件
└── di/                  # Hilt 依赖注入模块
```

## 数据模型

### SmsRecord (Room Entity)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK) | 自增主键 |
| phoneNumber | String | 发送方手机号 |
| sender | String | 发送方名称 |
| content | String? | 短信内容 (用户可选记录) |
| recordedContent | Boolean | 是否记录了内容 |
| category | String? | 分类 (验证码/通知/广告) |
| receivedAt | Long | 接收时间戳 |
| isStarred | Boolean | 是否收藏 |
| appLabel | String? | 关联 app 名称 |

### Bookmark (Room Entity)

| 字段 | 类型 | 说明 |
|------|------|------|
| recordId | Long (PK/FK) | 关联的短信记录 |
| note | String? | 用户备注 |
| createdAt | Long | 创建时间 |

### UserPreferences (DataStore)

| 字段 | 类型 | 默认 | 说明 |
|------|------|------|------|
| recordContent | Boolean | false | 是否记录短信内容 |
| notificationPermissionGranted | Boolean | false | 通知监听权限 |
| themeMode | Enum | SYSTEM | 主题模式 |
| exportFormat | Enum | CSV | 导出格式 |

## 核心数据流

```
SMS Notification
     ↓
SmsNotificationListener (系统通知监听)
     ↓
ViewModel.onNewSms(phoneNumber, sender, content?)
     ↓
Repository.insert(record)
     ↓
Room DB → Flow<List<SmsRecord>>
     ↓
ViewModel → UiState
     ↓
Compose UI 渲染
```

## 页面结构

### 底部导航 (3 Tab)
1. **记录** — 时间倒序展示所有记录，每条显示手机号、发送方、分类标签、收藏状态、时间
2. **搜索** — 按手机号/发送方模糊搜索
3. **设置** — 隐私开关、权限引导、导出、主题、关于

### 页面详情
- **首页 (记录列表)** — `LazyColumn`，下拉刷新，点击进入详情
- **号码详情页** — 聚合展示某手机号所有记录，可收藏/备注，显示"共 X 条，来自 Y 个发送方"
- **搜索页** — 输入即搜，模糊匹配
- **设置页** — 开关控制、权限引导、导出按钮
- **统计页** — 按手机号聚合，显示各号码的短信数量排行

## 隐私设计

- **默认不记录短信内容** — 用户需在设置中主动开启
- **数据纯本地** — 无网络请求，无云端同步
- **导出功能** — 用户可选择 CSV/JSON 导出到本地文件
- **权限最小化** — 仅使用 NotificationListenerService，不需要 READ_SMS 权限

## 非功能性需求

- 通知监听服务需在引导页教会用户授权
- 导出数据不含隐私敏感字段（除非用户明确选择）
- 支持 Android 深色模式
- 响应式设计，适配手机和平板
