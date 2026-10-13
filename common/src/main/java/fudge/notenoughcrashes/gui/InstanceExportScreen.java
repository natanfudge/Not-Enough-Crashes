package fudge.notenoughcrashes.gui;

import com.mojang.blaze3d.Blaze3D;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.NecPlatform;
import fudge.notenoughcrashes.upload.InstanceExport;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.ReportType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class InstanceExportScreen extends Screen {
    private final ProblemScreen parent;
    private boolean includeConfig, includeWorld, exporting;
    private String steps = "";
    private Path result;
    private boolean failed;

    public InstanceExportScreen(ProblemScreen parent) {
        super(text("title"));
        this.parent = parent;
    }

    private static Component text(String key) { return NecLocalization.translatedText("notenoughcrashes.export." + key); }
    private Component toggle(String key, boolean enabled) {
        return text(key).copy().append(": ").append(Component.translatable(enabled ? "options.on" : "options.off"));
    }
    private void line(int y, String key) {
        var label = new StringWidget(text(key), font);
        label.setX(width / 2 - label.getWidth() / 2);
        label.setY(y);
        addRenderableWidget(label);
    }
    @Override protected void init() {
        int x = width / 2 - 155, y = Math.max(10, height / 2 - 115);
        line(y, "title");
        line(y + 20, "contents");
        line(y + 32, "privacy");
        line(y + 44, "review");
        line(y + 64, "steps");
        var input = new EditBox(font, x, y + 78, 310, 20, text("steps"));
        input.setMaxLength(4000);
        input.setValue(steps);
        input.setResponder(value -> steps = value);
        input.active = !exporting && result == null;
        addRenderableWidget(input);
        var configs = Button.builder(toggle("configs", includeConfig), button -> {
            includeConfig = !includeConfig;
            button.setMessage(toggle("configs", includeConfig));
        }).bounds(x, y + 104, 150, 20).build();
        configs.active = !exporting && result == null;
        addRenderableWidget(configs);
        var world = Button.builder(toggle("world", includeWorld), button -> {
            includeWorld = !includeWorld;
            button.setMessage(toggle("world", includeWorld));
        }).bounds(x + 160, y + 104, 150, 20).build();
        world.active = parent.world != null && !exporting && result == null;
        addRenderableWidget(world);
        var export = Button.builder(text(result != null ? "openFolder" : exporting ? "working" : failed ? "retry" : "create"), button -> {
            if (result != null) Blaze3D.openPath(result.getParent());
            else export();
        }).bounds(x, y + 134, 310, 20).build();
        export.active = !exporting;
        addRenderableWidget(export);
        if (result != null) line(y + 162, "saved");
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> onClose())
                .bounds(x, y + 184, 310, 20).build());
    }
    private void export() {
        exporting = true;
        String report = parent.report.getFriendlyReport(ReportType.CRASH);
        String mods = NecPlatform.instance().getAllMods().stream().sorted(java.util.Comparator.comparing(m -> m.id()))
                .map(m -> m.id() + " " + m.version()).collect(Collectors.joining("\n"));
        Path game = Minecraft.getInstance().gameDirectory.toPath();
        Path config = NecPlatform.instance().getConfigDirectory();
        rebuildWidgets();
        CompletableFuture.supplyAsync(() -> {
            try { return InstanceExport.create(game, config, parent.world, includeConfig, includeWorld, report, mods, steps); }
            catch (Exception e) { throw new java.util.concurrent.CompletionException(e); }
        }).whenComplete((file, error) -> Minecraft.getInstance().execute(() -> {
            exporting = false;
            failed = error != null;
            result = file;
            if (error != null) NotEnoughCrashes.getLogger().error("Could not export reproduction bundle", error);
            rebuildWidgets();
        }));
    }
    @Override public void onClose() { Minecraft.getInstance().gui.setScreen(parent); }
}
