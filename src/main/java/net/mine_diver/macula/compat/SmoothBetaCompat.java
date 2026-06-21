package net.mine_diver.macula.compat;

import net.fabricmc.loader.api.FabricLoader;

public final class SmoothBetaCompat {
    public static final boolean LOADED = FabricLoader.getInstance().isModLoaded("smoothbeta");
}
