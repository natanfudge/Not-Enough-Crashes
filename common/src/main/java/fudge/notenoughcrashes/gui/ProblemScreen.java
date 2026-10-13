package fudge.notenoughcrashes.gui;

import com.mojang.blaze3d.Blaze3D;
import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.config.NecMidnightConfig;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import fudge.notenoughcrashes.upload.PasteUpload;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class ProblemScreen extends Screen {
    private static final Set<String> IGNORED_MODS = Set.of(
            "minecraft", "fabricloader", "loadcatcher", "jumploader", "quilt_loader", "forge", "neoforge", "notenoughcrashes");
    protected final CrashReport report;
    protected final Path world;
    private String uploadedCrashLink;
    private boolean uploading;
    private boolean uploadFailed;
    protected int x;
    protected int y;

    protected ProblemScreen(CrashReport report, Path world) {
        super(Component.empty());
        this.report = report;
        this.world = world;
    }

    private void addSuspectedModsWidget() {
        var mods = ModIdentifier.getSuspectedModsOf(report).stream()
                .filter(mod -> !IGNORED_MODS.contains(mod.id()))
                .sorted(Comparator.comparing(CommonModMetadata::name)).toList();
        if (mods.isEmpty()) {
            Component text = NecLocalization.translatedText("notenoughcrashes.crashscreen.noModsErrored").copy().withColor(0xE0E000);
            var widget = new StringWidget(text, font);
            widget.setX(width / 2 - font.width(text) / 2);
            widget.setY(y + 29);
            addRenderableWidget(widget);
            return;
        }
        int cellWidth = 310 / mods.size();
        for (int i = 0; i < mods.size(); i++) {
            var mod = mods.get(i);
            URI uri = webUri(mod.issuesPage());
            Component label = Component.literal(font.plainSubstrByWidth(mod.name(), Math.max(1, cellWidth - 4))).withColor(0xE0E000);
            var button = new PlainTextButton(x + i * cellWidth, y + 29, cellWidth - 4, 10, label,
                    ignored -> { if (uri != null) ConfirmLinkScreen.confirmLinkNow(this, uri, false); }, font);
            button.active = uri != null;
            button.setTooltip(Tooltip.create(Component.literal(mod.name())));
            addRenderableWidget(button);
        }
    }

    private static URI webUri(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value);
            return uri.getHost() != null && ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) ? uri : null;
        } catch (IllegalArgumentException ignored) { return null; }
    }

    private void upload(Button button) {
        if (uploading) return;
        if (uploadedCrashLink != null) {
            ConfirmLinkScreen.confirmLinkNow(this, URI.create(uploadedCrashLink));
            return;
        }
        uploading = true;
        uploadFailed = false;
        button.active = false;
        button.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.uploading"));
        String text = report.getFriendlyReport(ReportType.CRASH);
        CompletableFuture.supplyAsync(() -> {
            try { return PasteUpload.upload(text); }
            catch (Exception exception) { throw new java.util.concurrent.CompletionException(exception); }
        }).whenComplete((link, error) -> Minecraft.getInstance().execute(() -> {
            uploading = false;
            button.active = true;
            uploadFailed = error != null;
            if (error != null) {
                NotEnoughCrashes.getLogger().error("Could not upload crash report to pastes.dev", error);
                button.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.uploadFailed"));
            } else {
                uploadedCrashLink = link;
                button.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.openPaste"));
                Minecraft.getInstance().keyboardHandler.setClipboard(link);
            }
            if (Minecraft.getInstance().gui.screen() == this) {
                rebuildWidgets();
                if (link != null) ConfirmLinkScreen.confirmLinkNow(this, URI.create(link));
            }
        }));
    }

    private void openLocalReport() {
        Path file = report.getSaveFile();
        if (file != null) Blaze3D.openPath(file);
    }

    protected void addReportLink(int centerX, int lineY) {
        Component text = Component.literal(getFileNameString()).withColor(0x00FF00);
        int textWidth = font.width(text);
        var button = new PlainTextButton(centerX - textWidth / 2, lineY, textWidth, 10, text, ignored -> openLocalReport(), font);
        button.active = report.getSaveFile() != null;
        addRenderableWidget(button);
    }

    @Override
    public void init() {
        x = width / 2 - 155;
        y = height / 4;
        Button local = Button.builder(NecLocalization.translatedText("notenoughcrashes.gui.openLocalReport"), ignored -> openLocalReport())
                .bounds(x + 160, y + 132, 150, 20).build();
        local.active = report.getSaveFile() != null;
        addRenderableWidget(local);
        Button share = Button.builder(NecLocalization.translatedText(uploading ? "notenoughcrashes.gui.uploading" : uploadFailed ? "notenoughcrashes.gui.uploadFailed" : uploadedCrashLink == null ? "notenoughcrashes.gui.uploadPaste" : "notenoughcrashes.gui.openPaste"), this::upload)
                .bounds(x, y + 156, 150, 20).build();
        share.active = !uploading;
        addRenderableWidget(share);
        URI tracker = webUri(NecMidnightConfig.modpackIssueTracker);
        if (tracker != null) {
            addRenderableWidget(Button.builder(NecLocalization.translatedText("notenoughcrashes.gui.modpackIssues"), button -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(uploadedCrashLink != null ? uploadedCrashLink : report.getFriendlyReport(ReportType.CRASH));
                ConfirmLinkScreen.confirmLinkNow(this, tracker, false);
            }).bounds(x + 160, y + 156, 150, 20).build());
        }
        addRenderableWidget(Button.builder(NecLocalization.translatedText("notenoughcrashes.export.title"),
                button -> Minecraft.getInstance().gui.setScreen(new InstanceExportScreen(this)))
                .bounds(x, y + 180, 310, 20).build());
        addSuspectedModsWidget();
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    String getFileNameString() {
        return report.getSaveFile() != null ? report.getSaveFile().getFileName().toString()
                : NecLocalization.localize("notenoughcrashes.crashscreen.reportSaveFailed");
    }
}
