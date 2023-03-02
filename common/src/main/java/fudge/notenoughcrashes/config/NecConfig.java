package fudge.notenoughcrashes.config;


public record NecConfig(boolean disableReturnToMainMenu, boolean catchInitializationCrashes,
                        boolean debugModIdentification, int crashLimit) {

    public static NecConfig getCurrent() {
        return new NecConfig(NecMidnightConfig.disableReturnToMainMenu, NecMidnightConfig.catchInitializationCrashes,
                NecMidnightConfig.debugModIdentification, NecMidnightConfig.crashLimit);
    }
}
