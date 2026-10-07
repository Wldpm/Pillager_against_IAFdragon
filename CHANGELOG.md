# 更新日志

本项目的所有重要变更都会记录在此文件。

版本号遵循语义化版本（`主版本.次版本.修订号`）。

---

## [1.0.1] — 2026-10-07

### 修复

- **不再锁定已经处于死亡状态的龙**。IAF 的龙死后 `tickDeath()` 会把 `deathTime` 钉在 0，
  直到 `getDeathStage() >= getAgeInDays()/5` 才真正 `remove()` ——
  成年龙的尸体会在世界里停留很久，此前会被灾厄村民反复锁定。
  现在通过反射调用 `isMobDead()` 过滤（并与 `isAlive()` 双重判断），
  与 IAF 自身 `shouldTarget` 里 `!isMobDead()` 的逻辑保持一致。

### 变更

- **「龙 → 灾厄村民」改由本模组自带的数据包 tag 承担**：
  新增 `data/iceandfire/tags/entity_type/dragon_targets.json`（`replace: false`，
  与冰与火自带的那份合并）。**冰与火的 jar 因此不再需要任何改动**，回滚为官方原样。
- 补全依赖声明：`iceandfire`（required，`ordering = AFTER`）、
  `uranus`（`[2.3.2,)`）、`jupiter`（`[2.3,)`）、`minecraft`（`[1.21.1]`）（版本范围照抄冰与火自己的声明）。
- 模组显示名改为 **Pillager Against IAF Dragon**。

---

## [1.0.0] — 2026-10-06

### 新增

- **灾厄村民 → 龙**：掠夺者 / 卫道士 / 唤魔者 / 幻术师 会主动把冰与火的龙族当作攻击目标
  （在 `EntityJoinLevelEvent` 里给它们注入 `NearestAttackableTargetGoal`）。
- **龙 → 灾厄村民**：需要手动在冰与火 jar 的
  `data/iceandfire/tags/entity_type/dragon_targets.json` 里追加 `"#minecraft:illager"`
  （该版本尚未把这一步收进模组本身）。

### 说明

- 目标 AI 的优先级为 3，与原版"攻击村民 / 铁傀儡"同级，低于攻击玩家。
- 龙基类通过反射获取，对冰与火无编译期依赖。
- 许可证：MIT。

[1.0.0]: ../../releases/tag/v1.0.0
