package com.dsh.iafhostility;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 让「灾厄村民」主动把冰与火的龙族当作攻击目标（1.0.0）。
 *
 * <p>冰与火只提供了「龙 → 目标」这一侧的数据驱动列表（#iceandfire:dragon_targets），
 * 反向没有任何数据层机制（#iceandfire:fear_dragons / villagers 是"逃跑"而非"攻击"），
 * 所以必须注入 AI。这里沿用冰与火自己也在用的同一套办法：在 EntityJoinLevelEvent 里
 * 给原版生物 addGoal —— 见 IafServerEvents#onEntityJoinWorld。
 *
 * <p>注：1.0.0 的「龙 → 灾厄村民」需要手动改冰与火 jar 里的 dragon_targets；
 * 1.0.1 起改由本模组自带的数据包 tag 承担，不再需要改动冰与火。
 */
@EventBusSubscriber(modid = IllagerDragonHostility.MODID)
public final class IllagerDragonHostility {

    public static final String MODID = "iaf_illager_hostility";

    /** 存档内的去重标记。 */
    private static final String MARK = "iaf_illager_hostility";

    /** 与原版灾厄村民「攻击村民 / 铁傀儡」同级的优先级（玩家更优先）。 */
    private static final int PRIORITY = 3;

    private static final String DRAGON_CLASS = "com.iafenvoy.iceandfire.entity.DragonBaseEntity";

    private static volatile Class<?> dragonType;
    private static volatile boolean dragonTypeResolved;

    private IllagerDragonHostility() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        try {
            if (event.getLevel().isClientSide()) {
                return;
            }
            if (!(event.getEntity() instanceof Mob mob)) {
                return;
            }
            if (!mob.getType().is(EntityTypeTags.ILLAGER)) {
                return;
            }
            CompoundTag data = mob.getPersistentData();
            if (data.getBoolean(MARK)) {
                return;
            }
            Class<?> type = dragonType();
            if (type == null) {
                return;
            }
            mob.targetSelector.addGoal(PRIORITY, makeGoal(mob, type));
            data.putBoolean(MARK, true);
        } catch (Throwable ignored) {
            // 补丁自身出问题也绝不外抛，避免影响其它模组
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Goal makeGoal(Mob mob, Class<?> dragonType) {
        return new NearestAttackableTargetGoal(mob, (Class) dragonType, false);
    }

    private static Class<?> dragonType() {
        if (!dragonTypeResolved) {
            try {
                dragonType = Class.forName(DRAGON_CLASS);
            } catch (Throwable ignored) {
                dragonType = null;
            }
            dragonTypeResolved = true;
        }
        return dragonType;
    }
}
