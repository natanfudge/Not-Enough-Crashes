package fudge.notenoughcrashes.gui;

import fudge.notenoughcrashes.NotEnoughCrashes;
import fudge.notenoughcrashes.platform.CommonModMetadata;
import fudge.notenoughcrashes.stacktrace.ModIdentifier;
import fudge.notenoughcrashes.upload.LegacyCrashLogUpload;
import fudge.notenoughcrashes.utils.NecLocalization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

@Environment(EnvType.CLIENT)
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
        super(Text.of(""));
        this.report = report;
    }


    private Text getSuspectedModsText() {
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
                    MutableText modText = Text.literal(mod.name());
                    if (issuesPage != null) {
                        modText.styled(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(issuesPage))));
                    }
                    return modText;

                })
                .reduce((existing, next) -> existing.append(Text.of(", ")).append(next))
                .get();
    }

    private void addSuspectedModsWidget() {
        var widget = new TextWidget(getSuspectedModsText(),textRenderer);
        widget.setX(width / 2 - textRenderer.getWidth(getSuspectedModsText().getString()) / 2);
        widget.setTextColor(0xE0E000);
        widget.setY(y + 29);
        addDrawableChild(widget);
    }

    private void handleLegacyLinkClick(ButtonWidget buttonWidget) {
        try {
            if (uploadedCrashLink == null) {
                uploadedCrashLink = LegacyCrashLogUpload.upload(report.asString(ReportType.MINECRAFT_CRASH_REPORT));
            }
            Util.getOperatingSystem().open(uploadedCrashLink);
        } catch (Throwable e) {
            NotEnoughCrashes.getLogger().error("Exception when crash menu button clicked:", e);
            buttonWidget.setMessage(NecLocalization.translatedText("notenoughcrashes.gui.failed"));
            buttonWidget.active = false;
        }
    }


    @Override
    public void init() {
        addDrawableChild(
                ButtonWidget.builder(
                                NecLocalization.translatedText("notenoughcrashes.gui.getLink")
                                , this::handleLegacyLinkClick)
                        .dimensions(width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20)
                        .build()
        );


        x = width / 2 - 155;
        y = height / 4;
        addSuspectedModsWidget();
    }


    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (x >= xLeft && x <= xRight && y >= yTop && y <= yBottom) {
            Path file = report.getFile();
            if (file != null) {
                Util.getOperatingSystem().open(file);
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    String getFileNameString() {
        return report.getFile() != null ? "\u00A7n" + report.getFile().getFileName()
                : NecLocalization.localize("notenoughcrashes.crashscreen.reportSaveFailed");
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
    }

}
