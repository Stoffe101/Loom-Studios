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
import dev.loomstudios.client.ui.LoomProjectMenu;
import dev.loomstudios.client.ui.LoomScreenChrome;
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

public final class LoomHomeScreen extends LoomPointerScreen {
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
    private boolean compactMode;

    public LoomHomeScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        closeProjectCards();
        ProjectLibraryIndex.refresh();
        refreshSelectedPreview();

        this.compactMode = LoomUiTheme.compact(width, height);

        int margin = compactMode ? 8 : Math.max(8, Math.min(14, width / 72));
        int headerHeight = LoomScreenChrome.headerHeight(compactMode);
        int footerHeight = 18;

        shellLeft = margin;
        shellRight = width - margin;
        shellTop = 0;
        shellBottom = height;

        contentTop = headerHeight + (compactMode ? 8 : 8);
        contentBottom = height - 28;

        int totalWidth = shellRight - shellLeft;
        int gap = compactMode ? 8 : 8;

        int leftWidth = compactMode
                ? Math.max(102, Math.min(118, totalWidth / 5))
                : Math.max(
                        150,
                        Math.min(190, (int)Math.round(totalWidth * 0.20))
                );
        int previewWidth = compactMode
                ? Math.max(128, Math.min(148, totalWidth / 4))
                : Math.max(
                        176,
                        Math.min(250, (int)Math.round(totalWidth * 0.25))
                );

        leftPanelLeft = shellLeft;
        leftPanelRight = leftPanelLeft + leftWidth;

        previewRight = shellRight;
        previewLeft = previewRight - previewWidth;

        centerLeft = leftPanelRight + gap;
        centerRight = previewLeft - gap;

        if (centerRight - centerLeft < (compactMode ? 235 : 260)) {
            int shortage = (compactMode ? 235 : 260)
                    - (centerRight - centerLeft);

            int leftMinimum = compactMode ? 96 : 138;
            int rightMinimum = compactMode ? 118 : 158;

            int shaveLeft = Math.min(
                    shortage / 2,
                    Math.max(0, leftPanelRight - leftPanelLeft - leftMinimum)
            );
            leftPanelRight -= shaveLeft;
            centerLeft -= shaveLeft;

            int remaining = shortage - shaveLeft;
            int shaveRight = Math.min(
                    remaining,
                    Math.max(0, previewRight - previewLeft - rightMinimum)
            );
            previewLeft += shaveRight;
            centerRight += shaveRight;
        }

        buildLeftActions();
        buildCenterContent();
        buildPreviewPanel();
        addRenderableWidget(new LoomButton(centerRight-61,contentTop+3,54,16,Component.literal("View All"),()->minecraft.setScreen(new LoomLibraryScreen(this))));
        addRenderableWidget(new LoomButton(centerRight-61,templatesTop+3,54,16,Component.literal("View All"),()->minecraft.setScreen(new LoomTemplatesScreen(this))));
        addRenderableWidget(new LoomButton(leftPanelLeft,contentBottom-22,(leftPanelRight-leftPanelLeft-4)/2,20,Component.literal("Browse"),()->minecraft.setScreen(new LoomLibraryScreen(this))));
        addRenderableWidget(new LoomButton(leftPanelLeft+(leftPanelRight-leftPanelLeft-4)/2+4,contentBottom-22,(leftPanelRight-leftPanelLeft-4)/2,20,Component.literal("Settings"),()->minecraft.setScreen(new LoomSettingsScreen(this))));
    }

    private void buildLeftActions() {
        int width = leftPanelRight - leftPanelLeft;
        int available = contentBottom - contentTop;
        int gap = compactMode ? 4 : 5;
        int cardHeight = compactMode
                ? Math.max(28, Math.min(34, (available - gap * 4) / 5))
                : Math.max(
                        34,
                        Math.min(42, (available - gap * 4) / 5)
                );
        int y = contentTop;

        addRenderableWidget(new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal(compactMode ? "New Cape" : "Create New Cape"),
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
                Component.literal("Design linked or separate wings"),
                LoomActionCard.Icon.ELYTRA,
                false,
                this::editElytra
        );
        addRenderableWidget(elytra);
        y += cardHeight + gap;

        LoomActionCard load = new LoomActionCard(
                leftPanelLeft,
                y,
                width,
                cardHeight,
                Component.literal("Load Design"),
                Component.literal("Browse saved projects"),
                LoomActionCard.Icon.FOLDER,
                false,
                ()->minecraft.setScreen(new LoomLibraryScreen(this))
        );
        load.active = true;
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
                Component.literal("Share / Export"),
                Component.literal("PNG, .loom & portable codes"),
                LoomActionCard.Icon.CODE,
                false,
                this::openLoomCodes
        );
        addRenderableWidget(codes);
    }

    private void buildCenterContent() {
        int centerWidth = centerRight - centerLeft;
        int centerHeight = contentBottom - contentTop;
        int recentHeight = compactMode
                ? Math.max(
                        92,
                        Math.min(172, (int)Math.round(centerHeight * 0.57))
                )
                : Math.max(
                        108,
                        Math.min(380, (int)Math.round(centerHeight * 0.60))
                );

        recentBottom = contentTop + recentHeight;
        templatesTop = recentBottom + 7;

        List<ProjectDescriptor> recent = ProjectLibraryIndex.entries()
                .stream()
                .sorted(java.util.Comparator.comparing((ProjectDescriptor d)->!dev.loomstudios.client.project.LoomPreferences.get().favorite(d.projectId())))
                .limit(4)
                .toList();

        int innerLeft = centerLeft + 7;
        int innerRight = centerRight - 7;
        int cardTop = contentTop + 24;
        int cardBottom = recentBottom - 7;

        if (!recent.isEmpty()) {
            int cards = Math.min(
                    recent.size(),
                    compactMode
                            ? Math.max(
                                    1,
                                    Math.min(2, (innerRight - innerLeft) / 108)
                            )
                            : Math.max(
                                    1,
                                    Math.min(4, (innerRight - innerLeft) / 94)
                            )
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
                card.setOpenAction(this::openSelected);
                card.setContextAction((x,y)->{selectProject(descriptor);projectMenu=descriptor;menuX=Math.max(8,Math.min(width-166,x.intValue()));menuY=Math.max(contentTop,Math.min(height-28-207,y.intValue()));});
                card.setDecorated(true);
                projectCards.add(card);
                addRenderableWidget(card);
            }
        }

        int templateHeight = Math.max(34, contentBottom - templatesTop - 27);
        int templateGap = 4;
        int templateCount = compactMode
                ? 2
                : centerWidth >= 430
                        ? 6
                        : centerWidth >= 300
                                ? 4
                                : 3;
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
                    "Layered design",
                    icons[index],
                    true,
                    () -> createTemplate(dev.loomstudios.project.TemplateCatalog.Kind.values()[index+2])
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
        if(!primaryTemplateTitle(title).isEmpty())card.setTemplate(dev.loomstudios.project.TemplateCatalog.Kind.valueOf(primaryTemplateTitle(title)));
        addRenderableWidget(card);
    }

    private static String primaryTemplateTitle(String title){return switch(title){case "Blank"->"BLANK";case "Gradient"->"GRADIENT";case "Nature"->"NATURE";case "Space"->"SPACE";case "Fantasy"->"FANTASY";case "Emblems"->"EMBLEM";default->"";};}
    private ProjectDescriptor projectMenu;
    private int menuX,menuY;
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent e){if(projectMenu!=null&&e.key()==256){projectMenu=null;return true;}return super.keyPressed(e);}
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent e,boolean twice){if(projectMenu!=null){var d=projectMenu;projectMenu=null;if(e.button()==0&&e.x()>=menuX&&e.x()<menuX+158&&e.y()>=menuY&&e.y()<menuY+207){int action=(int)(e.y()-menuY)/23;if(action!=5){var lib=new LoomLibraryScreen(this);lib.executeAction(d,action);}}return true;}return super.mouseClicked(e,twice);}

    private void buildPreviewPanel() {
        int width = previewRight - previewLeft;
        int previewHeight = Math.max(
                compactMode ? 80 : 90,
                contentBottom - contentTop - (compactMode ? 23 : 27)
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
        this.openSelectedButton.setY(contentTop + previewHeight + 4);
        this.openSelectedButton.setWidth(buttonWidth);
        this.openSelectedButton.setHeight(compactMode ? 18 : 20);
        this.openSelectedButton
                .setIcon(LoomButton.Icon.CAPE)
                .setIconOnly(compactMode);
        addRenderableWidget(this.openSelectedButton);

        addRenderableWidget(new LoomButton(
                previewLeft + buttonWidth + buttonGap,
                contentTop + previewHeight + 4,
                width - buttonWidth - buttonGap,
                compactMode ? 18 : 20,
                Component.literal("Reset View"),
                LoomButton.Icon.RESET,
                compactMode,
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

    @Override public void onClose(){dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->minecraft.setScreen(null));}
    private void createNewCape() { dev.loomstudios.client.project.WorkspaceNavigation.request(this,this::createNewCapeNow); }
    private void createNewCapeNow() {
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

    private void createTemplate(dev.loomstudios.project.TemplateCatalog.Kind kind){dev.loomstudios.client.project.WorkspaceNavigation.request(this,()->{if(minecraft.player==null)return;ClientProjectWorkspace.replaceWith(dev.loomstudios.project.TemplateCatalog.create(kind,System.currentTimeMillis()),minecraft.player.getUUID());minecraft.setScreen(new CapeEditorScreen(this));});}
    private void createGradientTemplate() { dev.loomstudios.client.project.WorkspaceNavigation.request(this,this::createGradientTemplateNow); }
    private void createGradientTemplateNow() {
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

    private void editElytra() {
        if (this.minecraft.player == null) {
            return;
        }

        if (ClientProjectWorkspace.isInitialized()
                && ClientProjectWorkspace.isDirty()) {
            this.minecraft.setScreen(new ElytraEditorScreen(this));
            return;
        }

        ProjectLibraryIndex.selected().ifPresentOrElse(
                descriptor -> {
                    try {
                        ClientProjectWorkspace.open(
                                descriptor.projectPath(),
                                this.minecraft.player.getUUID()
                        );
                        this.minecraft.setScreen(
                                new ElytraEditorScreen(this)
                        );
                    } catch (IOException | IllegalArgumentException e) {
                        LoomStudios.LOGGER.error(
                                "Failed to open Loom project {} for Elytra editing",
                                descriptor.projectPath(),
                                e
                        );
                    }
                },
                () -> {
                    ClientProjectWorkspace.createBlank(
                            "Untitled Elytra",
                            System.currentTimeMillis(),
                            this.minecraft.player.getUUID()
                    );
                    this.minecraft.setScreen(
                            new ElytraEditorScreen(this)
                    );
                }
        );
    }

    private void createFromImport() { dev.loomstudios.client.project.WorkspaceNavigation.request(this,this::createFromImportNow); }
    private void createFromImportNow() {
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

    private void openLoomCodes() {
        ProjectLibraryIndex.selected().ifPresentOrElse(
                descriptor -> {
                    try {
                        LoomProject project = LocalProjectLibrary.load(
                                descriptor.projectPath()
                        );
                        this.minecraft.setScreen(
                                new LoomCodesScreen(this, project)
                        );
                    } catch (IOException | IllegalArgumentException e) {
                        LoomStudios.LOGGER.error(
                                "Failed to open selected Loom design for sharing",
                                e
                        );
                    }
                },
                () -> {
                    if (ClientProjectWorkspace.isInitialized()) {
                        this.minecraft.setScreen(
                                new LoomCodesScreen(
                                        this,
                                        ClientProjectWorkspace.project()
                                )
                        );
                    } else if (this.minecraft.player != null) {
                        ClientProjectWorkspace.createBlank(
                                "Untitled Design",
                                System.currentTimeMillis(),
                                this.minecraft.player.getUUID()
                        );
                        this.minecraft.setScreen(
                                new LoomCodesScreen(
                                        this,
                                        ClientProjectWorkspace.project()
                                )
                        );
                    }
                }
        );
    }

    private void openSelected() { dev.loomstudios.client.project.WorkspaceNavigation.request(this,this::openSelectedNow); }
    private void openSelectedNow() {
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
        LoomScreenChrome.renderBackdrop(graphics, width, height);
        LoomScreenChrome.renderBrandHeader(
                graphics,
                width,
                compactMode ? "Cape & Elytra Studio" : SUBTITLE.getString(),
                compactMode
        );

        renderSectionPanels(graphics);
        LoomScreenChrome.footer(
                graphics,
                width,
                height,
                compactMode
                        ? "Ready"
                        : "Minecraft 1.21.11  •  Ready",
                ProjectLibraryIndex.entries().size() + " Designs"
        );

        super.render(graphics, mouseX, mouseY, partialTick);
        if(projectMenu!=null)LoomProjectMenu.render(graphics,font,menuX,menuY,mouseX,mouseY,new String[]{"Edit","Rename…",dev.loomstudios.client.project.LoomPreferences.get().favorite(projectMenu.projectId())?"Unfavorite":"Favorite","Duplicate","Delete to Trash","Close","Equip","Folder / tags","Backup / versions"},4);
    }

    private void renderSectionPanels(GuiGraphics graphics) {
        LoomScreenChrome.panel(
                graphics,
                leftPanelLeft,
                contentTop,
                leftPanelRight,
                contentBottom
        );
        LoomScreenChrome.panel(
                graphics,
                centerLeft,
                contentTop,
                centerRight,
                recentBottom
        );
        LoomScreenChrome.panel(
                graphics,
                centerLeft,
                templatesTop,
                centerRight,
                contentBottom
        );
        LoomScreenChrome.panel(
                graphics,
                previewLeft,
                contentTop,
                previewRight,
                contentBottom
        );

        LoomScreenChrome.panelHeader(
                graphics,
                centerLeft,
                contentTop,
                centerRight,
                "Recent Projects"
        );
        LoomScreenChrome.panelHeader(
                graphics,
                centerLeft,
                templatesTop,
                centerRight,
                "Templates"
        );
        LoomScreenChrome.panelHeader(
                graphics,
                previewLeft,
                contentTop,
                previewRight,
                "3D Preview"
        );

        if (ProjectLibraryIndex.entries().isEmpty()) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal("No saved projects yet"),
                    (centerLeft + centerRight) / 2,
                    contentTop + 48,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        if (ProjectLibraryIndex.rejectedFiles() > 0 && !compactMode) {
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

    public static String formatAge(long modifiedAt) {
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
