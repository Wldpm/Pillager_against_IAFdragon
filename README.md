# 冰与火：刌民 & 龙族互殴

> **Pillager Against IAF Dragon** · modId `iaf_illager_hostility`

让 [Ice and Fire（冰与火）](https://github.com/IAFEnvoy/IceAndFire-CE) 的**龙族**与
**灾厄村民**（刌民：掠夺者 / 卫道士 / 唤魔者 / 幻术师）**互相敌对** —— 龙会攻击刌民，刌民也会主动攻击龙。

| 项目 | 值 |
| --- | --- |
| 游戏版本 | Minecraft **1.21.1** |
| 加载器 | NeoForge **21.1.252**（依赖声明 `[21.1,)`） |
| modId | `iaf_illager_hostility` |
| 许可证 | **MIT** |

---

## 功能

| 方向 | 实现方式 |
| --- | --- |
| **龙 → 灾厄村民** | 本模组自带的**数据包 tag**（`replace: false`）往 `#iceandfire:dragon_targets` 追加 `#minecraft:illager` |
| **灾厄村民 → 龙** | 在 `EntityJoinLevelEvent` 里给灾厄村民注入「攻击龙」的目标 AI |

**冰与火的 jar 保持官方原样，一个字都不改** —— 两个方向都由本模组自己承担，
所以冰与火升级时不会与本模组冲突，卸载也只需删掉这一个 jar。

冰与火的龙判断"打不打某个非龙生物"完全由 tag 决定
（`DragonUtils.isDragonTargetable(e, tag)` 的实现就是 `e.getType().is(tag)`），所以龙侧只需补 tag；
而原版灾厄村民的攻击目标是硬编码的（`NearestAttackableTargetGoal<Player>` 等），
**没有任何数据层机制能扩展**，因此灾厄村民侧必须注入 AI —— 本模组沿用冰与火自己也在用的同一套范式
（`ServerEvents.onEntityJoinWorld`）。

### 行为细节

- **优先级 3**：与原版"攻击村民 / 铁傀儡"同级，**低于**攻击玩家（刌民仍会先打玩家）。
- **不需要视线**（`mustSee = false`）：龙常在空中或隔着地形，要求视线会让效果大打折扣。
- **不会锁定已死亡的龙**（1.0.1 起）：IAF 的龙死后**不会立刻消失** ——
  `tickDeath()` 把 `deathTime` 钉在 0，直到 `getDeathStage() >= getAgeInDays()/5` 才真正 `remove()`，
  成年龙的尸体会在世界里停留很久；这期间用反射调 `isMobDead()` 过滤掉，
  与 IAF 自身 `shouldTarget` 里 `!isMobDead()` 的判断保持一致。
- **对冰与火无编译期依赖**：龙基类用 `Class.forName` 反射获取并缓存；反射拿不到时静默失效。
- **不会重复叠加**：是否已注入写在实体的 `getPersistentData()` 里，区块反复加载也只加一次。
- **全程 `try/catch (Throwable)`**：任何异常都不外抛，保证不影响其它模组。

### 范围

`#minecraft:illager` = **掠夺者 / 卫道士 / 唤魔者 / 幻术师**。

**不含**女巫与劫掠兽（那两者在 `#minecraft:raiders` 里）。若要一并纳入，
把本模组 `data/iceandfire/tags/entity_type/dragon_targets.json` 里的 `#minecraft:illager`
换成 `#minecraft:raiders` 即可（代码侧不用动）。

---

## 安装

1. Minecraft **1.21.1** + NeoForge **21.1.252**（或更高的 21.1.x）；
2. 安装 [Ice and Fire: Community Edition](https://github.com/IAFEnvoy/IceAndFire-CE) **2.1.3+**
   及其前置 `uranus`（2.3.2+）与 `jupiter`（2.3+）；
3. 把 `pillager_against_IAFdragon-neoforge1.21.1-<版本>.jar` 丢进 `mods/`；
4. 启动游戏。

> 本模组把 `iceandfire` / `uranus` / `jupiter` 声明为**硬依赖**：
> 缺少其中任何一个，游戏会提示本模组无法加载。

---

## 从源码构建

需要 **JDK 21** 与 **Gradle 8.8+**，首次构建需联网下载依赖：

```bash
gradle build
# 产物：build/libs/pillager_against_IAFdragon-<版本>.jar
```

也可以在 IDE（IntelliJ IDEA / Eclipse）里直接以 Gradle 项目导入。

> ⚠️ 本仓库的 `build.gradle` 依据
> [NeoForged 官方 ModDevGradle 文档](https://docs.neoforged.net/toolchain/docs/plugins/mdg/)编写，
> 但**未在离线环境实测**；插件版本可在
> [ModDevGradle releases](https://projects.neoforged.net/neoforged/ModDevGradle) 查询后调整。

---

## 目录结构

```
src/main/java/com/dsh/iafhostility/
├── IafHostilityMod.java            # 极简 @Mod 主类
└── IllagerDragonHostility.java     # 全部逻辑：EntityJoinLevelEvent → addGoal
src/main/resources/
├── META-INF/neoforge.mods.toml     # modId / 版本 / 依赖声明
└── data/iceandfire/tags/entity_type/dragon_targets.json   # 「龙 → 灾厄村民」那一半
```

---

## 许可证

[MIT](LICENSE) © 2026 Lmhui17

Ice and Fire 及其前置（uranus / jupiter）归其各自作者所有，本模组与它们没有从属关系。
