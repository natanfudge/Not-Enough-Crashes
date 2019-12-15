package fudge.notenoughcrashes.api;

import net.fabricmc.loader.api.FabricLoader;

/**
 * When the game crashes, Not Enough Crashes will keep the game running and allow the player to keep on playing.
 * The downside of this is that any mod state might get stuck in an invalid state after an exception occurs.
 * This class is to allow these mods to reset their state back to the default state, whenever the game must stop and go back to the main menu.
 * You do NOT need to check if Not Enough Crashes is installed. It is enough to {@code include} this module and use these methods.
 *
 * For most things {@link MinecraftCrashes#onEveryCrash(Runnable)} is the desired method.
 */
public class MinecraftCrashes {
    private static final boolean NECInstalled = FabricLoader.getInstance().isModLoaded("notenoughcrashes");

    /**
     * Run some code every time game crashes.
     * <p>
     *  Use this to clean up global state, that gets initialized once at the start, and that won't get cleaned up by itself.
     *  Every global/static object should only call this ONCE, and the callback will be kept and executed whenever the game crashes.
     * </p>
     *
     * @param disposer Code that will reset your mod state back to a valid state.
     */
    public static void onEveryCrash(Runnable disposer) {
        if (NECInstalled) NotEnoughCrashesApi.onEveryCrash(disposer);
    }

    /**
     * Run some code the next time the game crashes.
     * <p>
     *  Use this to clean up instance state that will only need to be cleaned up once, and that won't get cleaned up by itself
     *  (usually this is not the case and this method is not necessary)
     *  This may be called multiple times by different instances, and the callback will not be kept after the game crashes and it is called.
     * </p>
     *
     * @param disposer Code that will reset your mod state back to a valid state.
     */
    public static void onNextCrash(Runnable disposer) {
        if (NECInstalled) NotEnoughCrashesApi.onNextCrash(disposer);
    }
}
