package com.dsh.iafhostility;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 让「灾厄村民」主动把冰与火的龙族当作攻击目标。
 *
 * <p>背景：冰与火只提供了「龙 → 目标」这一侧的数据驱动列表
 * （#iceandfire:dragon_targets），反向没有任何数据层机制
 * （#iceandfire:fear_dragons / villagers 是"逃跑"而非"攻击"）。
 * 因此这里用冰与火自己也在用的同一套办法：在 EntityJoinLevelEvent 里
 * 给原版生物 addGoal —— 见 IafServerEvents#onEntityJoinWorld。
 *
 * <p>1.0.1 修复：不再锁定**已经处于死亡状态**的冰与火龙。
 * IAF 的龙死后不会立刻消失（{@code DragonBaseEntity#tickDeath} 把 deathTime 钉在 0，
 * 直到 getDeathStage() >= getAgeInDays()/5 才真正 remove），成年龙的尸体会留在世界上很久；
 * 这期间 isMobDead() 为 true。IAF 自己也用同一个判断避免龙互相攻击尸体
 * （DragonBaseEntity#shouldTarget 里的 {@code !isMobDead()}），本补丁与之对齐。
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

    private static volatile Method mobDeadMethod;
    private static volatile boolean mobDeadResolved;

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

    @SuppressWarnings("unchecked")
    private static Goal makeGoal(Mob mob, Class<?> dragonType) {
        Predicate<LivingEntity> filter = IllagerDragonHostility::isAttackableDragon;
        return new NearestAttackableTargetGoal<>(mob, (Class<LivingEntity>) dragonType, false, filter);
    }

    /**
     * 只锁定"活着、且不在冰与火死亡状态"的龙。
     *
     * <p>两道检查：{@code isAlive()} 拦住血量已空的；{@code isMobDead()} 拦住
     * IAF 的死亡动画/尸体状态（成年龙可能长时间停留在此状态）。
     */
    private static boolean isAttackableDragon(LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        return !isIafMobDead(target);
    }

    private static boolean isIafMobDead(LivingEntity target) {
        Method getter = mobDeadMethod();
        if (getter == null) {
            return false;   // 拿不到判断方法就不拦，保持"没有本补丁"时的行为
        }
        try {
            return Boolean.TRUE.equals(getter.invoke(target));
        } catch (Throwable t) {
            return false;
        }
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

    private static Method mobDeadMethod() {
        if (!mobDeadResolved) {
            Class<?> type = dragonType();
            if (type != null) {
                try {
                    mobDeadMethod = type.getMethod("isMobDead");
                } catch (Throwable ignored) {
                    mobDeadMethod = null;
                }
            }
            mobDeadResolved = true;
        }
        return mobDeadMethod;
    }
}
