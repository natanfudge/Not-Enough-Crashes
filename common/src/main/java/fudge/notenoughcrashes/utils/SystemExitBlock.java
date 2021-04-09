package fudge.notenoughcrashes.utils;

import java.security.Permission;

public class SystemExitBlock {

    public static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().startsWith("exitVM")  ) {
                    throw new SystemExitBlockedException("An attempt was made to forcefully close the game with no stack trace (see stack trace)." +
                            " Not Enough Crashes made the game simply crash instead since the forceCrashScreen option is enabled.") ;
                }
            }
        } ;
        System.setSecurityManager( securityManager ) ;
    }

    private static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }
}
