package fudge.notenoughcrashes.fabric.config;

//TODO: config Migration path.
// Current version:
//  - If the new config value is non-default, use it, otherwise, use old config value.
//  - Log a warning in the console to use the new format.
// Next MC Version:
// - Log an error in the console that the old config will stop working in the next Minecraft version.
// MC version after that:
// - Remove old config.

public class NecMidnightConfig extends MidnightConfig {
    @Comment
    public static Comment disableReturnToMainMenuComment1;
    @Comment
    public static Comment disableReturnToMainMenuComment2;
    @Entry
    public static boolean disableReturnToMainMenu;

    @Comment
    public static Comment catchInitializationCrashesComment1;
    @Comment
    public static Comment catchInitializationCrashesComment2;
    @Entry
    public static boolean catchInitializationCrashes = true;

    @Comment
    public static Comment debugModIdentificationComment;
    @Entry
    public static boolean debugModIdentification = false;

    @Comment
    public static Comment crashLimitComment;
    @Entry
    public static int crashLimit = 20;

}