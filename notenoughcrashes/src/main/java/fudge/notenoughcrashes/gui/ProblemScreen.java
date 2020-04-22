package fudge.notenoughcrashes.gui;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


import fudge.notenoughcrashes.patches.PatchedCrashReport;
import fudge.notenoughcrashes.gui.util.TextWidget;
import fudge.notenoughcrashes.gui.util.Widget;
import fudge.notenoughcrashes.utils.CrashLogUpload;
import net.fabricmc.loader.metadata.ModMetadataV1;
import net.minecraft.class_5250;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Environment(EnvType.CLIENT)
//TODO: use proper widgets and composition over inheritance
public abstract class ProblemScreen extends Screen {

    private List<Widget> widgets = new ArrayList<>();

    protected void addWidget(Widget widget) {
        widgets.add(widget);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    public abstract ProblemScreen construct(CrashReport report);

    protected final CrashReport report;
    private String hasteLink = null;
    protected int xLeft = Integer.MAX_VALUE;
    protected int xRight = Integer.MIN_VALUE;
    protected int yTop = Integer.MAX_VALUE;
    protected int yBottom = Integer.MIN_VALUE;


    protected int x;
    protected int y;


    protected ProblemScreen(CrashReport report) {
        super(new LiteralText(""));
        this.report = report;
    }

    // Earlier elements will be used first, may need to add more elements if people start using weird shit
    private static final List<String> possibleIssuesFieldsByPriority = Arrays.asList(
                    "issues", "sources", "homepage"
    );

    /**
     * Loader doesn't provide us with a proper api to get a singular link to an issues page, so we need to guess.
     */
    private String getIssuesPage(ContactInformation contactInformation) {
        for (String possibleField : possibleIssuesFieldsByPriority) {
            Optional<String> value = contactInformation.get(possibleField);
            if (value.isPresent()) return value.get();
        }
        return null;
    }

    private static final Set<String> IGNORED_MODS = new HashSet<>(Arrays.asList("minecraft","fabricloader","loadcatcher", "jumploader"));
    private Text getSuspectedModsText() {
        Set<ModMetadata> suspectedMods = ((PatchedCrashReport) report).getSuspectedMods();
        if (suspectedMods == null) return new TranslatableText("notenoughcrashes.crashscreen.identificationErrored");
        if (suspectedMods.isEmpty()) return new TranslatableText("notenoughcrashes.crashscreen.unknownCause");


        // Minecraft exists and basically any stack trace, and loader exists in any launch,
        // it's better not to include them in the list of mods.
        suspectedMods.removeIf(mod -> IGNORED_MODS.contains(mod.getId()));

//        suspectedMods.add(new ModMetadataV1())

        if (suspectedMods.isEmpty()) return new TranslatableText("notenoughcrashes.crashscreen.noModsErrored");

        Text text = suspectedMods.stream()
                        .sorted(Comparator.comparing(ModMetadata::getName))
                        .map(mod -> {
                            String issuesPage = getIssuesPage(mod.getContact());
                            class_5250 modText = new LiteralText(mod.getName());
                            if (issuesPage != null) {
                                modText.method_27694(style -> style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, issuesPage)));
                            }
                            return modText;

                        })
                        .reduce((existing, next) -> existing.append(new LiteralText(", ")).append(next))
                        .get();

        return text;
    }

    private void addSuspectedModsWidget() {
        addWidget(new TextWidget(getSuspectedModsText(), TextWidget.CLICKABLE_TEXT_COLOR, textRenderer, width / 2, y + 29));
    }


    @Override
    public void init() {
        widgets = new ArrayList<>();
        addButton(new ButtonWidget(width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, new TranslatableText("notenoughcrashes.gui.getLink"),
                        buttonWidget -> {
                            try {
                                if (hasteLink == null) {
                                    hasteLink = CrashLogUpload.upload(report.asString());
                                }
                                Field uriField;
                                //noinspection JavaReflectionMemberAccess
                                uriField = Screen.class.getDeclaredField("clickedLink");
                                uriField.setAccessible(true);
                                uriField.set(ProblemScreen.this, new URI(hasteLink));
                                MinecraftClient.getInstance().openScreen(new ConfirmChatLinkScreen(b -> {
                                    if (b) {
                                        Util.getOperatingSystem().open(hasteLink);
                                    }

                                    MinecraftClient.getInstance().openScreen(construct(report));
                                }, hasteLink, true));
                            } catch (Throwable e) {
                                LOGGER.error("Exception when crash menu button clicked:", e);
                                buttonWidget.setMessage(new TranslatableText("notenoughcrashes.gui.failed"));
                                buttonWidget.active = false;
                            }
                        }));

        x = width / 2 - 155;
        y = height / 4;
        addSuspectedModsWidget();
    }

    @Override
    public boolean mouseClicked(double x, double y, int int_1) {
        for (Widget widget : widgets) widget.onClick(x, y);
        if (x >= xLeft && x <= xRight && y >= yTop && y <= yBottom) {
            File file = report.getFile();
            if (file != null) {
                Util.getOperatingSystem().open(file);
            }
        }
        return super.mouseClicked(x, y, int_1);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }


    protected void drawFileNameString(MatrixStack matrixStack, int y) {
        String fileNameString = report.getFile() != null ? "\u00A7n" + report.getFile().getName()
                                        : I18n.translate("notenoughcrashes.crashscreen.reportSaveFailed");
        int stLen = textRenderer.getStringWidth(fileNameString);
        xLeft = width / 2 - stLen / 2;
        xRight = width / 2 + stLen / 2;
        drawString(matrixStack, textRenderer, fileNameString, xLeft, y += 11, 0x00FF00);
        yTop = y;
        yBottom = y + 10;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseY, int i, float f) {
        for (Widget widget : widgets) widget.draw(matrixStack);
        super.render(matrixStack, mouseY, i, f);
    }

}
