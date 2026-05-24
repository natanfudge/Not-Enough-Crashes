package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import fudge.notenoughcrashes.upload.LegacyCrashLogUpload;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

public abstract class ProblemScreen extends Screen {
    private static final Set<String> IGNORED_MODS = new HashSet<>(Arrays.asList(
            "minecraft", "fabricloader", "loadcatcher", "jumploader", "quilt_loader", "forge", "notenoughcrashes"
    ));
    protected CrashReport report;
    private String uploadedCrashLink = null;
    protected int xLeft = Integer.MAX_VALUE;
    protected int xRight = Integer.MIN_VALUE;
    protected int yTop = Integer.MAX_VALUE;
    protected int yBottom = Integer.MIN_VALUE;

    protected int x;
    protected int y;


    protected ProblemScreen(CrashReport report) {
        super(Component.empty());
        this.report = report;
    }


    private Component getSuspectedModsText() {
        Set<CommonModMetadata> suspectedMods = ModIdentifier.getSuspectedModsOf(report);

        // Minecraft exists and basically any stack trace, and loader exists in any launch,
        // it's better not to include them in the list of mods.
        suspectedMods.removeIf(mod -> IGNORED_MODS.contains(mod.id()));

        if (suspectedMods.isEmpty()) {
            return NecLocalization.translatedText("notenoughcrashes.crashscreen.noModsErrored");
        }

        return suspectedMods.stream()
                .sorted(Comparator.comparing(CommonModMetadata::name))
                .map(mod -> {
                    String issuesPage = mod.issuesPage();
                    MutableComponent modText = Component.literal(mod.name());
                    if (issuesPage != null) {
                        modText.withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(issuesPage))));
                    }
                    return modText;

                })
                .reduce((existing, next) -> existing.append(Component.literal(", ")).append(next))
                .get();
    }

    private void addSuspectedModsWidget() {
        Component suspectedModsText = getSuspectedModsText().copy().withColor(0xE0E000);
        var widget = new StringWidget(suspectedModsText, font);
        widget.setX(width / 2 - font.width(suspectedModsText.getString()) / 2);
        widget.setY(y + 29);
        addRenderableWidget(widget);
    }

    private void handleLegacyLinkClick(Button buttonWidget) {
        try {
            if (uploadedCrashLink == null) {
                uploadedCrashLink = LegacyCrashLogUpload.upload(report.getFriendlyReport(ReportType.CRASH));
            }
            Util.getPlatform().openUri(uploadedCrashLink);
        } catch (Throwable e) {
            NotEnoughCrashes.getLogger().error("Exception when crash menu button clicked:", e);
            buttonWidget.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.failed"));
            buttonWidget.active = false;
        }
    }


    @Override
    public void init() {
        addRenderableWidget(
                Button.builder(
                                NecLocalization.translatedText("notenoughcrashes.gui.getLink")
                                , this::handleLegacyLinkClick)
                        .bounds(width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20)
                        .build()
        );


        x = width / 2 - 155;
        y = height / 4;
        addSuspectedModsWidget();
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (x >= xLeft && x <= xRight && y >= yTop && y <= yBottom) {
            Path file = report.getSaveFile();
            if (file != null) {
                Util.getPlatform().openPath(file);
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    String getFileNameString() {
        return report.getSaveFile() != null ? "\u00A7n" + report.getSaveFile().getFileName()
                : NecLocalization.localize("notenoughcrashes.crashscreen.reportSaveFailed");
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

}
