package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.LocalProjectLibrary;
import dev.loomstudios.client.project.ProjectDescriptor;
import dev.loomstudios.client.project.ProjectLibraryIndex;
import dev.loomstudios.client.ui.LoomActionCard;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomProjectCard;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.CapeUvRegion;
import dev.loomstudios.project.GradientLayerData;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.NormalizedRect;
import dev.loomstudios.project.ProjectEdits;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LoomHomeScreen extends Screen {
    private static final Component TITLE = Component.literal("Loom Studios");
    private static final Component SUBTITLE =
            Component.literal("Cape & Elytra Design Studio");

    private final List<LoomProjectCard> projectCards = new ArrayList<>();

    private LoomProject selectedPreviewProject;
    private LoomPlayerPreviewWidget previewWidget;
    private LoomButton openSelectedButton;

    private int shellLeft;
    private int shellTop;
    private int shellRight;
    private int shellBottom;

    private int leftPanelLeft;
    private int leftPanelRight;
    private int centerLeft;
    private int centerRight;
    private int previewLeft;
    private int previewRight;
    private int contentTop;
    private int contentBottom;
    private int recentBottom;
    private int templatesTop;

    public LoomHomeScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        closeProjectCards();
        ProjectLibraryIndex.refresh();
        refreshSelectedPreview();

        int margin = Math.max(8, Math.min(16, this.width / 64));
        shellLeft = margin;
        shellRight = this.width - margin;
        shellTop = Math.max(8, margin / 2);
        shellBottom = this.height - Math.max(10, margin);

        int headerHeight = 46;
        int statusHeight = 18;
        contentTop = shellTop + headerHeight + 7;
        contentBottom = shellBottom - statusHeight - 7;

        int totalWidth = shellRight - shellLeft;
        int gap = 8;
        int leftWidth = Math.max(
                142,
                Math.min(190, (int)Math.round(totalWidth * 0.20))
        );
        int previewWidth = Math.max(
                170,
                Math.min(250, (int)Math.round(totalWidth * 0.25))
        );

        leftPanelLeft = shellLeft + 8;
        leftPanelRight = leftPanelLeft + leftWidth;

        previewRight = shellRight - 8;
        previewLeft = previewRight - previewWidth;

        centerLeft = leftPanelRight + gap;
        centerRight = previewLeft - gap;

        if (centerRight - centerLeft < 220) {
            int shortage = 220 - (centerRight - centerLeft);
            int shaveLeft = Math.min(shortage / 2, Math.max(0, leftWidth - 132));
            leftPanelRight -= shaveLeft;
            centerLeft -= shaveLeft;

            int remaining = shortage - shaveLeft;
            int shaveRight = Math.min(
                    remaining,
                    Math.max(0, previewWidth - 154)
            );
            previewLeft += shaveRight;
            centerRight += shaveRight;
        }

        buildLeftActions();
        buildCenterContent();
        buildPreviewPanel();
    }

    private void buildLeftActions() {
        int width = leftPanelRight - leftPanelLeft;
        int available = contentBottom - contentTop;
        int gap = 5;
        int cardHeight = Math.max(
                32,
                Math.min(40, (available - gap * 5) / 6)
        );
        int y = contentTop;

        addRenderableWidget(new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Create New Cape"),
                Component.literal("Start from scratch"),
                LoomActionCard.Icon.CAPE,
                true,
                this::createNewCape
        ));
        y += cardHeight + gap;

        LoomActionCard elytra = new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Edit Elytra"),
                Component.literal("Wing editor coming next"),
                LoomActionCard.Icon.ELYTRA,
                false,
                () -> { }
        );
        elytra.active = false;
        addRenderableWidget(elytra);
        y += cardHeight + gap;

        LoomActionCard load = new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Load Design"),
                Component.literal("Open selected project"),
                LoomActionCard.Icon.FOLDER,
                false,
                this::openSelected
        );
        load.active = ProjectLibraryIndex.selected().isPresent();
        addRenderableWidget(load);
        this.openSelectedButton = new LoomButton(
                previewLeft,
                contentBottom - 22,
                Math.max(72, (previewRight - previewLeft - 4) / 2),
                20,
                Component.literal("Open"),
                this::openSelected
        );
        this.openSelectedButton.active = ProjectLibraryIndex.selected().isPresent();
        y += cardHeight + gap;

        addRenderableWidget(new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Import Image"),
                Component.literal("Smart Import PNG"),
                LoomActionCard.Icon.IMAGE,
                false,
                this::createFromImport
        ));
        y += cardHeight + gap;

        LoomActionCard codes = new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Loom Codes"),
                Component.literal("Share & redeem designs"),
                LoomActionCard.Icon.CODE,
                false,
                () -> { }
        );
        codes.active = false;
        addRenderableWidget(codes);
        y += cardHeight + gap;

        LoomActionCard settings = new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Settings"),
                Component.literal("Editor preferences"),
                LoomActionCard.Icon.SETTINGS,
                false,
                () -> { }
        );
        settings.active = false;
        addRenderableWidget(settings);
    }

    private void buildCenterContent() {
        int centerWidth = centerRight - centerLeft;
        int centerHeight = contentBottom - contentTop;
        int recentHeight = Math.max(
                108,
                Math.min(154, (int)Math.round(centerHeight * 0.58))
        );

        recentBottom = contentTop + recentHeight;
        templatesTop = recentBottom + 7;

        List<ProjectDescriptor> recent = ProjectLibraryIndex.entries()
                .stream()
                .limit(4)
                .toList();

        int innerLeft = centerLeft + 7;
        int innerRight = centerRight - 7;
        int cardTop = contentTop + 24;
        int cardBottom = recentBottom - 7;

        if (!recent.isEmpty()) {
            int cards = Math.min(
                    recent.size(),
                    Math.max(1, Math.min(4, (innerRight - innerLeft) / 94))
            );
            int cardGap = 5;
            int cardWidth = Math.max(
                    74,
                    (innerRight - innerLeft - cardGap * (cards - 1)) / cards
            );

            for (int i = 0; i < cards; i++) {
                ProjectDescriptor descriptor = recent.get(i);
                LoomProjectCard card = new LoomProjectCard(
                        innerLeft + i * (cardWidth + cardGap),
                        cardTop,
                        cardWidth,
                        Math.max(60, cardBottom - cardTop),
                        descriptor,
                        Component.literal(formatAge(
                                descriptor.modifiedAtEpochMillis()
                        )),
                        () -> ProjectLibraryIndex.selected()
                                .map(selected -> selected.projectId()
                                        .equals(descriptor.projectId()))
                                .orElse(false),
                        () -> selectProject(descriptor)
                );
                projectCards.add(card);
                addRenderableWidget(card);
            }
        }

        int templateHeight = Math.max(34, contentBottom - templatesTop - 27);
        int templateGap = 4;
        int templateCount = centerWidth >= 430 ? 6 : centerWidth >= 300 ? 4 : 3;
        int templateWidth = Math.max(
                58,
                (innerRight - innerLeft - templateGap * (templateCount - 1))
                        / templateCount
        );

        addTemplate(
                innerLeft,
                templatesTop + 21,
                templateWidth,
                templateHeight,
                "Blank",
                "Empty cape",
                LoomActionCard.Icon.BLANK,
                true,
                this::createNewCape
        );

        if (templateCount >= 2) {
            addTemplate(
                    innerLeft + (templateWidth + templateGap),
                    templatesTop + 21,
                    templateWidth,
                    templateHeight,
                    "Gradient",
                    "Two color",
                    LoomActionCard.Icon.GRADIENT,
                    true,
                    this::createGradientTemplate
            );
        }

        String[] labels = {"Nature", "Space", "Fantasy", "Emblems"};
        LoomActionCard.Icon[] icons = {
                LoomActionCard.Icon.NATURE,
                LoomActionCard.Icon.SPACE,
                LoomActionCard.Icon.FANTASY,
                LoomActionCard.Icon.EMBLEM
        };

        for (int i = 2; i < templateCount; i++) {
            int index = i - 2;
            addTemplate(
                    innerLeft + i * (templateWidth + templateGap),
                    templatesTop + 21,
                    templateWidth,
                    templateHeight,
                    labels[index],
                    "Coming later",
                    icons[index],
                    false,
                    () -> { }
            );
        }
    }

    private void addTemplate(
            int x,
            int y,
            int width,
            int height,
            String title,
            String subtitle,
            LoomActionCard.Icon icon,
            boolean enabled,
            Runnable action
    ) {
        LoomActionCard card = new LoomActionCard(
                x,
                y,
                width,
                height,
                Component.literal(title),
                Component.literal(subtitle),
                icon,
                false,
                action
        );
        card.active = enabled;
        addRenderableWidget(card);
    }

    private void buildPreviewPanel() {
        int width = previewRight - previewLeft;
        int previewHeight = Math.max(
                90,
                contentBottom - contentTop - 27
        );

        this.previewWidget = new LoomPlayerPreviewWidget(
                previewLeft,
                contentTop,
                width,
                previewHeight,
                () -> selectedPreviewProject
        );
        addRenderableWidget(this.previewWidget);

        int buttonGap = 4;
        int buttonWidth = Math.max(72, (width - buttonGap) / 2);

        this.openSelectedButton.setX(previewLeft);
        this.openSelectedButton.setY(contentTop + previewHeight + 5);
        this.openSelectedButton.setWidth(buttonWidth);
        addRenderableWidget(this.openSelectedButton);

        addRenderableWidget(new LoomButton(
                previewLeft + buttonWidth + buttonGap,
                contentTop + previewHeight + 5,
                width - buttonWidth - buttonGap,
                20,
                Component.literal("Reset View"),
                () -> {
                    if (previewWidget != null) {
                        previewWidget.resetView();
                    }
                }
        ));
    }

    private void selectProject(ProjectDescriptor descriptor) {
        ProjectLibraryIndex.select(descriptor.projectId());
        refreshSelectedPreview();
        if (openSelectedButton != null) {
            openSelectedButton.active = true;
        }
    }

    private void refreshSelectedPreview() {
        selectedPreviewProject = null;

        ProjectLibraryIndex.selected().ifPresent(descriptor -> {
            try {
                selectedPreviewProject =
                        LocalProjectLibrary.load(descriptor.projectPath());
            } catch (IOException | IllegalArgumentException e) {
                LoomStudios.LOGGER.warn(
                        "Failed to load selected Loom preview {}",
                        descriptor.projectPath(),
                        e
                );
            }
        });
    }

    private void createNewCape() {
        if (this.minecraft.player == null) {
            return;
        }

        ClientProjectWorkspace.createBlank(
                "Untitled Cape",
                System.currentTimeMillis(),
                this.minecraft.player.getUUID()
        );
        this.minecraft.setScreen(new CapeEditorScreen(this));
    }

    private void createGradientTemplate() {
        if (this.minecraft.player == null) {
            return;
        }

        ClientProjectWorkspace.createBlank(
                "Gradient Cape",
                System.currentTimeMillis(),
                this.minecraft.player.getUUID()
        );

        LoomProject blank = ClientProjectWorkspace.project();
        int scale = blank.cape().width() / LoomProject.TEXTURE_WIDTH;
        NormalizedRect target = new NormalizedRect(
                CapeUvRegion.OUTSIDE.atlasX(0, scale)
                        / (double)blank.cape().width(),
                CapeUvRegion.OUTSIDE.atlasY(0, scale)
                        / (double)blank.cape().height(),
                CapeUvRegion.OUTSIDE.width(scale)
                        / (double)blank.cape().width(),
                CapeUvRegion.OUTSIDE.height(scale)
                        / (double)blank.cape().height()
        );
        GradientLayerData gradient = GradientLayerData.defaultLinear(
                0xFF22D7E8,
                0xFF9B4DFF,
                target
        );

        ClientProjectWorkspace.apply(project ->
                ProjectEdits.addCapeGradientLayer(
                        project,
                        "Base Gradient",
                        gradient
                )
        );

        this.minecraft.setScreen(new CapeEditorScreen(this));
    }

    private void createFromImport() {
        if (this.minecraft.player == null) {
            return;
        }

        ClientProjectWorkspace.createBlank(
                "Imported Cape",
                System.currentTimeMillis(),
                this.minecraft.player.getUUID()
        );
        this.minecraft.setScreen(
                new SmartImportScreen(
                        this,
                        CapeUvRegion.OUTSIDE
                )
        );
    }

    private void openSelected() {
        if (this.minecraft.player == null) {
            return;
        }

        ProjectLibraryIndex.selected().ifPresent(descriptor -> {
            try {
                ClientProjectWorkspace.open(
                        descriptor.projectPath(),
                        this.minecraft.player.getUUID()
                );
                this.minecraft.setScreen(new CapeEditorScreen(this));
            } catch (IOException | IllegalArgumentException e) {
                LoomStudios.LOGGER.error(
                        "Failed to open Loom project {}",
                        descriptor.projectPath(),
                        e
                );
            }
        });
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                shellLeft,
                shellTop,
                shellRight,
                shellBottom,
                LoomUiTheme.BACKDROP
        );
        graphics.fill(
                shellLeft + 1,
                shellTop + 1,
                shellRight - 1,
                shellBottom - 1,
                LoomUiTheme.PANEL
        );

        renderHeader(graphics);
        renderSectionPanels(graphics);
        renderStatusBar(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(
                shellLeft + 8,
                shellTop + 6,
                shellRight - 8,
                contentTop - 7,
                LoomUiTheme.PANEL_INNER
        );
        graphics.fill(
                shellLeft + 8,
                contentTop - 9,
                shellRight - 8,
                contentTop - 7,
                LoomUiTheme.ACCENT
        );

        graphics.drawCenteredString(
                this.font,
                TITLE,
                this.width / 2,
                shellTop + 12,
                LoomUiTheme.TEXT
        );
        graphics.drawCenteredString(
                this.font,
                SUBTITLE,
                this.width / 2,
                shellTop + 26,
                LoomUiTheme.TEXT_MUTED
        );

        int markX = this.width / 2 - this.font.width("Loom Studios") / 2 - 18;
        int markY = shellTop + 14;
        graphics.fill(markX, markY, markX + 6, markY + 6, LoomUiTheme.ACCENT);
        graphics.fill(markX + 3, markY - 3, markX + 4, markY + 9, LoomUiTheme.ACCENT_ALT);
    }

    private void renderSectionPanels(GuiGraphics graphics) {
        fillPanel(
                graphics,
                leftPanelLeft - 5,
                contentTop - 5,
                leftPanelRight + 5,
                contentBottom + 5
        );
        fillPanel(
                graphics,
                centerLeft,
                contentTop,
                centerRight,
                recentBottom
        );
        fillPanel(
                graphics,
                centerLeft,
                templatesTop,
                centerRight,
                contentBottom
        );

        graphics.drawString(
                this.font,
                Component.literal("Recent Projects"),
                centerLeft + 7,
                contentTop + 7,
                LoomUiTheme.TEXT,
                false
        );
        graphics.drawString(
                this.font,
                Component.literal("Templates"),
                centerLeft + 7,
                templatesTop + 7,
                LoomUiTheme.TEXT,
                false
        );

        if (ProjectLibraryIndex.entries().isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("No saved projects yet"),
                    (centerLeft + centerRight) / 2,
                    contentTop + 50,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        if (ProjectLibraryIndex.rejectedFiles() > 0) {
            graphics.drawString(
                    this.font,
                    Component.literal(
                            ProjectLibraryIndex.rejectedFiles()
                                    + " unreadable project file(s) skipped"
                    ),
                    centerLeft + 7,
                    recentBottom - 13,
                    LoomUiTheme.DANGER,
                    false
            );
        }
    }

    private void renderStatusBar(GuiGraphics graphics) {
        int y = shellBottom - 20;
        graphics.fill(
                shellLeft + 1,
                y,
                shellRight - 1,
                shellBottom - 1,
                LoomUiTheme.PANEL_INNER
        );

        graphics.drawString(
                this.font,
                Component.literal("✦ Loom Studios"),
                shellLeft + 10,
                y + 6,
                LoomUiTheme.TEXT,
                false
        );
        graphics.drawString(
                this.font,
                Component.literal("Minecraft 1.21.11"),
                shellLeft + 104,
                y + 6,
                LoomUiTheme.TEXT_MUTED,
                false
        );
        graphics.drawString(
                this.font,
                Component.literal("• Ready"),
                shellLeft + 206,
                y + 6,
                0xFF58D47D,
                false
        );

        String count = ProjectLibraryIndex.entries().size() + " Designs";
        int countWidth = this.font.width(count);
        graphics.drawString(
                this.font,
                Component.literal(count),
                shellRight - countWidth - 10,
                y + 6,
                LoomUiTheme.ACCENT_ALT,
                false
        );
    }

    private static void fillPanel(
            GuiGraphics graphics,
            int left,
            int top,
            int right,
            int bottom
    ) {
        graphics.fill(left, top, right, bottom, LoomUiTheme.BORDER);
        graphics.fill(
                left + 1,
                top + 1,
                right - 1,
                bottom - 1,
                LoomUiTheme.PANEL_INNER
        );
    }

    private static String formatAge(long modifiedAt) {
        long delta = Math.max(0L, System.currentTimeMillis() - modifiedAt);
        long minutes = delta / 60_000L;

        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }

        long hours = minutes / 60L;
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        long days = hours / 24L;
        if (days < 30) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        long months = days / 30L;
        return String.format(Locale.ROOT, "%d mo ago", months);
    }

    private void closeProjectCards() {
        for (LoomProjectCard card : projectCards) {
            card.close();
        }
        projectCards.clear();
    }

    @Override
    public void removed() {
        closeProjectCards();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }
}
