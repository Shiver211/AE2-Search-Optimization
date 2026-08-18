# AE2 Search Optimization（AE2 搜索优化）

一个针对 **AE2UEL**的 Minecraft 1.12.2 客户端优化 Mod，提升 AE2 终端搜索的流畅度，并为带来的**最近搜索历史**功能。

---

## 功能特性

### 🔍 搜索性能优化

针对开启 Tooltip 搜索功能后 AE2 终端搜索在物品数量庞大时出现的卡顿问题，本 Mod 从两个层面进行优化：

- **Tooltip 搜索索引（Tooltip Search Index）**
  当启用 AE2 的「搜索 Tooltip」功能时，Mod 会为所有物品建立 tooltip 文本索引，使搜索可以快速匹配 tooltip 内容。索引的生成被拆分为**每 tick 的小时间片**（默认每 tick 最多 10ms），避免在单次界面刷新中执行完整的 tooltip 扫描导致明显卡顿。索引更新后，搜索结果会自动刷新。

- **搜索查询缓存（Search Query Cache）**
  缓存搜索时重复执行的正则表达式编译与字符串分割操作，避免对每个物品重复进行昂贵的编译开销，进一步降低搜索延迟。

- **测试效果**
  在拥有 3.4k+ 物品种类的AE终端内。
  优化前：每次打开终端后第一次搜索卡死 1-2s，开启保持标准搜索后打开终端卡死 1-2s。
  优化后：两次卡顿几乎不可察觉，代价是由于需要缓存 Tooltip，首次打开终端搜索会在 1-2s 内逐渐显示出Tooltip搜索结果(名称搜索和后续搜索不受影响)。

### 🕘 最近搜索历史

在 AE2 终端的搜索框下方显示一个**最近搜索下拉列表**，方便你快速复用之前的搜索词。该功能移植自 [AE2 Recent Search](https://github.com/zh5112/AE2-Recent-Search)（MIT License，Copyright (c) 2026 zh5112）。

- **收藏**：将常用搜索词置顶收藏，优先展示。
- **键盘导航**：使用上下方向键在历史条目间快速选择。
- **点击应用**：点击历史条目即可直接应用该搜索词。
- **单条删除 / 一键清空**：可删除单条记录，或一键清空全部历史。
- **内置设置面板**：在 AE2 容器内直接打开设置页。
- **按玩家独立存储**：历史记录按玩家分别保存，互不干扰。
- **同步外部搜索**：支持与JEI同步。

---

## 配置

配置文件位于 `config/ae2searchoptimization.cfg`，可在游戏内或手动编辑。

| 分类 | 配置项 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `performance` | `searchOptimizationEnabled` | `true` | 是否开启搜索性能优化 |
| `performance` | `tooltipIndexBudgetMillis` | `10` | 每 tick 用于生成 tooltip 索引的最大毫秒数（范围 1 ~ 1000）。数值越大索引建立越快，但可能引起轻微卡顿。 |
| `recentSearch` | `maxVisibleEntries` | `10` | 最近搜索下拉列表中最多显示的条目数。 |

---

## 许可

- 本 Mod 的搜索优化部分为原创代码。
- 最近搜索历史功能移植自 [AE2 Recent Search](https://github.com/zh5112/AE2-Recent-Search)，遵循 **MIT License**（Copyright (c) 2026 zh5112）。