//package fudge.notenoughcrashes.forge;
//
//import net.minecraftforge.common.ForgeConfigSpec;
//
////public record NecConfig(boolean disableReturnToMainMenu, boolean catchInitializationCrashes,
////                        boolean debugModIdentification,int crashLimit) {
//public class NecForgeConfig {
//    public static final ForgeConfigSpec CONFIG_SPEC;
//    public static final ForgeConfigSpec.BooleanValue disableReturnToMainMenu;
//    public static final ForgeConfigSpec.BooleanValue debugModIdentification;
//    public static final ForgeConfigSpec.IntValue crashLimit;
//
//    static {
//        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
//
//        disableReturnToMainMenu = builder.comment("If true, the \"Return To Main Menu\" button will be disabled when crashing, meaning you cannot recover from a crash.")
//                .define("disableReturnToMainMenu", false);
//        debugModIdentification = builder.comment("If true, additional info will be logged for the mod developer.")
//                .define("debugModIdentification",false);
//        crashLimit = builder.comment("How many times NEC will try to prevent the game from closing in one session.")
//                .defineInRange("crashLimit",10,0,1000);
//        CONFIG_SPEC = builder.build();
//    }
//}
