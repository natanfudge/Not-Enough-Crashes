//package fudge.notenoughcrashes.api;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class NotEnoughCrashesApi {
//    public static final List<Runnable> permanentDisposers = new ArrayList<>();
//    public static final List<Runnable> oneTimeDisposers = new ArrayList<>();
//
//    public static void onEveryCrash(Runnable disposer){
//        permanentDisposers.add(disposer);
//    }
//
//    public static void onNextCrash(Runnable disposer) {
//        oneTimeDisposers.add(disposer);
//    }
//}
//
