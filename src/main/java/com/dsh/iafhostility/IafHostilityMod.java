package com.dsh.iafhostility;

import net.neoforged.fml.common.Mod;

/**
 * 极简主类：只用于让 NeoForge 正常注册本模组的容器
 * （事件监听在 {@link IllagerDragonHostility} 里由 @EventBusSubscriber 自动注册）。
 */
@Mod(IllagerDragonHostility.MODID)
public final class IafHostilityMod {

    public IafHostilityMod() {
    }
}
