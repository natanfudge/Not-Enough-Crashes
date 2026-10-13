package io.github.natanfudge.nectest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.nio.file.Files;

/** Opt-in, repeatable client crash/rejoin test; never loaded in release artifacts. */
public final class MaintenanceScenario {
    private static String mode;
    private static volatile boolean armed;
    private static java.util.concurrent.CompletableFuture<Void> diagnostics;
    public static boolean consumeCrash(String expected) {
        if (armed && expected.equals(mode)) { armed = false; return true; }
        return false;
    }
    private static int starts;
    private static int phase;
    private static int ticks;
    private static final String WORLD = "NEC-maintenance";

    public static void register(String selectedMode) {
        mode = selectedMode;
        if (!mode.startsWith("maintenance_")) return;
        Thread watchdog = new Thread(() -> {
            try { Thread.sleep(180_000); } catch (InterruptedException ignored) { return; }
            if (phase != 5) {
                System.out.println("NEC_TEST FAIL timeout phase=" + phase);
                Minecraft.getInstance().stop();
            }
        }, "NEC test watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    public static void recordStart() {
        starts++;
        System.out.println("NEC_TEST lifecycle starts=" + starts);
    }

    public static void tick(Minecraft client) {
        if (mode == null || !mode.startsWith("maintenance_")) return;
        ticks++;
        if (phase == 0 && client.gui.screen() instanceof TitleScreen && client.gui.overlay() == null && ticks > 60) {
            if (mode.equals("maintenance_quit")) { finish(client, "PASS startup without a crash"); return; }
            phase = 1;
            ticks = 0;
            if (!mode.equals("maintenance_title")) openWorld(client);
        } else if (phase == 1 && ticks > 80 && (mode.equals("maintenance_title") || client.player != null)) {
            phase = 2;
            ticks = 0;
            System.out.println("NEC_TEST triggering " + mode);
            if (mode.equals("maintenance_diagnostics")) {
                phase = 8;
                diagnostics = java.util.concurrent.CompletableFuture.runAsync(() -> MaintenanceDiagnostics.run(client.getSingleplayerServer()), client.getSingleplayerServer());
                return;
            }
            if (mode.equals("maintenance_render") || mode.equals("maintenance_entity") || mode.equals("maintenance_server")) {
                armed = true;
                return;
            }
            if (mode.equals("maintenance_screen")) {
                client.gui.setScreen(new net.minecraft.client.gui.screens.Screen(Component.literal("NEC failing screen")) {
                    @Override public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
                        throw new NecTestCrash("NEC repeatedly failing custom screen");
                    }
                });
                return;
            }
            if (mode.equals("maintenance_stack")) throw new StackOverflowError("NEC test stack overflow");
            throw new NecTestCrash("NEC maintenance crash");
        } else if (phase == 2 && ticks > 40) {
            String screen = client.gui.screen() == null ? "null" : client.gui.screen().getClass().getSimpleName();
            System.out.println("NEC_TEST recovered screen=" + screen + " starts=" + starts
                    + " connection=" + (client.getConnection() != null) + " world=" + (client.level != null)
                    + " server=" + (client.getSingleplayerServer() != null));
            Screenshot.grab(client.gameDirectory, mode + "-crash.png", client.gameRenderer.mainRenderTarget(), 1, msg -> {});
            if (!screen.equals("CrashScreen")) { finish(client, "FAIL missing crash screen"); return; }
            if (starts != 1) { finish(client, "FAIL repeated CLIENT_STARTED"); return; }
            if (client.getConnection() != null || client.level != null || client.getSingleplayerServer() != null) {
                finish(client, "FAIL stale world or connection"); return;
            }
            if (mode.equals("maintenance_links")) {
                var parent = client.gui.screen();
                press(client, "notenoughcrashes.gui.modpackIssues");
                if (!(client.gui.screen() instanceof net.minecraft.client.gui.screens.ConfirmLinkScreen)) { finish(client, "FAIL modpack link confirmation"); return; }
                client.gui.setScreen(parent);
                boolean found = false;
                for (var child : parent.children()) {
                    if (child instanceof net.minecraft.client.gui.components.PlainTextButton button
                            && button.active && button.getMessage().getString().contains("Not Enough Crashes Test")) {
                        button.onPress(null);
                        found = client.gui.screen() instanceof net.minecraft.client.gui.screens.ConfirmLinkScreen;
                        break;
                    }
                }
                client.gui.setScreen(parent);
                if (!found) { finish(client, "FAIL suspected mod link"); return; }
                System.out.println("NEC_TEST PASS modpack and suspected mod buttons open confirmation screens");
            }
            if (mode.equals("maintenance_export")) {
                phase = 6;
                ticks = 0;
                press(client, "notenoughcrashes.export.title");
                return;
            }
            for (var child : client.gui.screen().children()) {
                if (child instanceof Button button && button.getMessage().getString().equals(Component.translatable("gui.toTitle").getString())) {
                    phase = 3;
                    ticks = 0;
                    button.onPress(null);
                    return;
                }
            }
            finish(client, "FAIL return button missing");
        } else if (phase == 6 && ticks == 10) {
            for (var child : client.gui.screen().children()) {
                if (child instanceof net.minecraft.client.gui.components.EditBox input) input.setValue("Open world, trigger test crash.");
                if (child instanceof Button button && (button.getMessage().getString().startsWith("Include configs")
                        || button.getMessage().getString().startsWith("Include crashed world"))) button.onPress(null);
            }
            Screenshot.grab(client.gameDirectory, "export-options.png", client.gameRenderer.mainRenderTarget(), 1, msg -> {});
            press(client, "notenoughcrashes.export.create");
        } else if (phase == 6 && ticks > 40) {
            for (var child : client.gui.screen().children()) {
                if (child instanceof Button button && button.getMessage().getString().equals(Component.translatable("notenoughcrashes.export.openFolder").getString())) {
                    try (var files = Files.list(client.gameDirectory.toPath().resolve("crash-reports"))) {
                        var archive = files.filter(path -> path.toString().endsWith(".zip")).max(java.util.Comparator.comparing(Path -> Path.toFile().lastModified())).orElseThrow();
                        try (var zip = new java.util.zip.ZipFile(archive.toFile())) {
                            if (zip.getEntry("saves/" + WORLD + "/level.dat") == null || zip.getEntry("config/nec_test_mode.txt") == null
                                    || zip.getEntry("mods.txt") == null || zip.getEntry("README.txt") == null) throw new IllegalStateException("Missing ZIP context");
                        }
                        System.out.println("NEC_TEST PASS local ZIP with notes, mod manifest, configs and stopped world");
                    } catch (Exception error) { finish(client, "FAIL export " + error); return; }
                    client.gui.screen().onClose();
                    phase = 3;
                    ticks = 0;
                    press(client, "gui.toTitle");
                    return;
                }
            }
        } else if (phase == 3 && ticks > 30 && client.gui.screen() instanceof TitleScreen) {
            if (mode.equals("maintenance_title")) { finish(client, "PASS recovered and returned to title"); return; }
            phase = 4;
            ticks = 0;
            openWorld(client);
        } else if (phase == 4 && ticks > 100 && client.player != null && client.gui.screen() == null) {
            client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            phase = 7;
            ticks = 0;
        } else if (phase == 8 && diagnostics.isDone()) {
            try { diagnostics.join(); finish(client, "PASS diagnostics in running server"); }
            catch (Exception error) { error.printStackTrace(); finish(client, "FAIL diagnostics " + error); }
        } else if (phase == 7 && ticks > 40) {
            Screenshot.grab(client.gameDirectory, mode + "-rejoined.png", client.gameRenderer.mainRenderTarget(), 1, msg -> {});
            finish(client, "PASS rejoined world; invisible=" + client.player.isInvisible());
        }
    }

    private static void press(Minecraft client, String key) {
        for (var child : client.gui.screen().children()) {
            if (child instanceof Button button && button.getMessage().getString().equals(Component.translatable(key).getString())) {
                button.onPress(null);
                return;
            }
        }
        throw new IllegalStateException("Button missing: " + key);
    }

    private static void openWorld(Minecraft client) {
        if (mode.equals("maintenance_multiplayer")) {
            String address = "127.0.0.1:25569";
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new TitleScreen(), client,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                    new net.minecraft.client.multiplayer.ServerData("NEC local test", address, net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
            return;
        }
        if (Files.exists(client.gameDirectory.toPath().resolve("saves").resolve(WORLD).resolve("level.dat"))) {
            client.createWorldOpenFlows().openWorld(WORLD, () -> finish(client, "FAIL world load cancelled"));
        } else {
            client.createWorldOpenFlows().createFreshLevel(WORLD,
                    new LevelSettings(WORLD, GameType.CREATIVE,
                            new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                    new WorldOptions(0L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
        }
    }

    private static void finish(Minecraft client, String result) {
        phase = 5;
        System.out.println("NEC_TEST " + result);
        client.stop();
    }
}
