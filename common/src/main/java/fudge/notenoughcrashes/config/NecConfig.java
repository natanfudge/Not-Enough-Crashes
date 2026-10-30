package fudge.notenoughcrashes.config;

import java.util.List;


public record NecConfig(boolean disableReturnToMainMenu, boolean catchInitializationCrashes,
                        boolean debugModIdentification, int crashLimit, boolean catchGameloopCrashes,
                        List<String> recoveryExclusions) {

    public static NecConfig getCurrent() {
        return new NecConfig(NecMidnightConfig.disableReturnToMainMenu, NecMidnightConfig.catchInitializationCrashes,
                NecMidnightConfig.debugModIdentification, NecMidnightConfig.crashLimit, NecMidnightConfig.catchGameloop,
                NecMidnightConfig.recoveryExclusions);
    }
}
