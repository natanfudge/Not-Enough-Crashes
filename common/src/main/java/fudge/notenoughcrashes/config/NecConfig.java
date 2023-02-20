package fudge.notenoughcrashes.config;


import fudge.notenoughcrashes.platform.NecPlatform;

public record NecConfig(boolean disableReturnToMainMenu, boolean catchInitializationCrashes,
                        boolean debugModIdentification,int crashLimit) {
    
    public static NecConfig getCurrent() {
        NecConfig guiResult = NecPlatform.instance().getCurrentConfig();
        OldNecConfig oldResult = OldNecConfig.instance();

        OldNecConfig defaultConfig = new OldNecConfig();
        
        //TODO: remove this logic and just use new (old config will be ignored)
        
        // For compatibility - If the new config value is non-default, use it, otherwise, use old config value.
        return new NecConfig(
                guiResult.disableReturnToMainMenu == defaultConfig.disableReturnToMainMenu ? oldResult.disableReturnToMainMenu : guiResult.disableReturnToMainMenu,
                guiResult.catchInitializationCrashes == defaultConfig.catchInitializationCrashes ? oldResult.catchInitializationCrashes :guiResult.catchInitializationCrashes,
                guiResult.debugModIdentification == defaultConfig.debugModIdentification ? oldResult.debugModIdentification : guiResult.debugModIdentification,
                guiResult.crashLimit == defaultConfig.crashLimit ? oldResult.crashLimit : guiResult.crashLimit
        );
    }
}
