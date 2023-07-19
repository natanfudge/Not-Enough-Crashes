package fudge.notenoughcrashes.config;


public class NecMidnightConfig extends MidnightConfig {
    @Comment
    public static Comment disableReturnToMainMenuComment1;
    @Comment
    public static Comment disableReturnToMainMenuComment2;
    @Entry
    public static boolean disableReturnToMainMenu = false;

    @Comment
    public static Comment catchInitializationCrashesComment1;
    @Comment
    public static Comment catchInitializationCrashesComment2;
    @Comment
    public static Comment catchInitializationCrashesComment3;
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

    @Comment
    public static Comment catchGameloopComment1;
    @Comment
    public static Comment catchGameloopComment2;
    @Entry
    public static boolean catchGameloop = true;

}