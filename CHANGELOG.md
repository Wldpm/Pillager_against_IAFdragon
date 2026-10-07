# 更新日志

本项目的所有重要变更都会记录在此文件。

版本号遵循语义化版本（`主版本.次版本.修订号`）。

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
