package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.LocalProjectLibrary;
import dev.loomstudios.client.project.ProjectLibraryIndex;
import dev.loomstudios.client.sharing.LoomShareExportAdapter;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class LoomCodesScreen extends Screen {
    private static final Component TITLE =
            Component.literal("Loom Studios - Loom Codes");

    private final Screen parent;
    private final LoomProject sourceProject;

    private LoomProject incomingProject;
    private LoomProject previewProject;

    private LoomPlayerPreviewWidget previewWidget;
    private LoomPlayerPreviewWidget.Mode previewMode =
            LoomPlayerPreviewWidget.Mode.CAPE;

    private String status = "Ready";
    private String incomingStatus =
            "Paste a portable LSP1 code to preview a design.";

    private LoomButton importButton;
    private LoomButton importOpenButton;
    private LoomButton sourcePreviewButton;
    private LoomButton incomingPreviewButton;
    private LoomButton capePreviewButton;
    private LoomButton elytraPreviewButton;

    private int shellLeft;
    private int shellTop;
    private int shellRight;
    private int shellBottom;

    private int leftLeft;
    private int leftRight;
    private int centerLeft;
    private int centerRight;
    private int rightLeft;
    private int rightRight;
    private int contentTop;
    private int contentBottom;

    public LoomCodesScreen(
            Screen parent,
            LoomProject sourceProject
    ) {
        super(TITLE);
        this.parent = Objects.requireNonNull(parent, "parent");
        this.sourceProject = Objects.requireNonNull(
                sourceProject,
                "sourceProject"
        );
        this.previewProject = sourceProject;
    }

    @Override
    protected void init() {
        int margin = Math.max(8, Math.min(14, width / 72));
        shellLeft = margin;
        shellRight = width - margin;
        shellTop = Math.max(6, margin / 2);
        shellBottom = height - margin;

        int headerHeight = 42;
        int footerHeight = 22;
        contentTop = shellTop + headerHeight + 7;
        contentBottom = shellBottom - footerHeight - 7;

        int totalWidth = shellRight - shellLeft - 16;
        int gap = 8;

        int leftWidth = Math.max(
                145,
                Math.min(205, (int)Math.round(totalWidth * 0.25))
        );
        int rightWidth = Math.max(
                165,
                Math.min(235, (int)Math.round(totalWidth * 0.26))
        );

        leftLeft = shellLeft + 8;
        leftRight = leftLeft + leftWidth;
        rightRight = shellRight - 8;
        rightLeft = rightRight - rightWidth;
        centerLeft = leftRight + gap;
        centerRight = rightLeft - gap;

        if (centerRight - centerLeft < 220) {
            int shortage = 220 - (centerRight - centerLeft);
            int shaveLeft = Math.min(
                    shortage / 2,
                    Math.max(0, leftWidth - 130)
            );
            leftRight -= shaveLeft;
            centerLeft -= shaveLeft;

            int remaining = shortage - shaveLeft;
            int shaveRight = Math.min(
                    remaining,
                    Math.max(0, rightWidth - 150)
            );
            rightLeft += shaveRight;
            centerRight += shaveRight;
        }

        buildLeftActions();
        buildCenterActions();
        buildPreview();
        updateButtons();
    }

    private void buildLeftActions() {
        int width = leftRight - leftLeft;
        int y = contentTop;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Back to Home"),
                this::goBack
        ));
        y += 26;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Copy Design ID"),
                this::copyDesignId
        ));
        y += 24;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Copy Portable Code"),
                this::copyPortableCode
        ));
        y += 24;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Save Portable Code"),
                this::savePortableCode
        ));
        y += 30;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Export Project (.loom)"),
                this::exportProject
        ));
        y += 24;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Export Cape PNG"),
                this::exportCape
        ));
        y += 24;

        addRenderableWidget(new LoomButton(
                leftLeft,
                y,
                width,
                20,
                Component.literal("Export Elytra PNG"),
                this::exportElytra
        ));
    }

    private void buildCenterActions() {
        int width = centerRight - centerLeft;
        int y = contentTop + 34;

        addRenderableWidget(new LoomButton(
                centerLeft + 8,
                y,
                width - 16,
                22,
                Component.literal("Paste Portable Code from Clipboard"),
                this::pastePortableCode
        ));
        y += 29;

        int half = Math.max(70, (width - 20) / 2);

        this.sourcePreviewButton = new LoomButton(
                centerLeft + 8,
                y,
                half,
                20,
                Component.literal("Preview Mine"),
                () -> selectPreview(sourceProject)
        );
        this.incomingPreviewButton = new LoomButton(
                centerLeft + 12 + half,
                y,
                width - half - 20,
                20,
                Component.literal("Preview Import"),
                () -> {
                    if (incomingProject != null) {
                        selectPreview(incomingProject);
                    }
                }
        );
        addRenderableWidget(sourcePreviewButton);
        addRenderableWidget(incomingPreviewButton);
        y += 28;

        this.importButton = new LoomButton(
                centerLeft + 8,
                y,
                half,
                22,
                Component.literal("Import to Library"),
                () -> importIncoming(false)
        );
        this.importOpenButton = new LoomButton(
                centerLeft + 12 + half,
                y,
                width - half - 20,
                22,
                Component.literal("Import + Open"),
                () -> importIncoming(true)
        );
        addRenderableWidget(importButton);
        addRenderableWidget(importOpenButton);
    }

    private void buildPreview() {
        int width = rightRight - rightLeft;
        int previewHeight = Math.max(
                100,
                contentBottom - contentTop - 58
        );

        this.previewWidget = new LoomPlayerPreviewWidget(
                rightLeft,
                contentTop,
                width,
                previewHeight,
                () -> previewProject,
                previewMode
        );
        addRenderableWidget(previewWidget);

        int half = Math.max(60, (width - 4) / 2);
        int y = contentTop + previewHeight + 5;

        this.capePreviewButton = new LoomButton(
                rightLeft,
                y,
                half,
                20,
                Component.literal("Cape"),
                () -> setPreviewMode(
                        LoomPlayerPreviewWidget.Mode.CAPE
                )
        );
        this.elytraPreviewButton = new LoomButton(
                rightLeft + half + 4,
                y,
                width - half - 4,
                20,
                Component.literal("Elytra"),
                () -> setPreviewMode(
                        LoomPlayerPreviewWidget.Mode.ELYTRA
                )
        );
        addRenderableWidget(capePreviewButton);
        addRenderableWidget(elytraPreviewButton);

        addRenderableWidget(new LoomButton(
                rightLeft,
                y + 25,
                width,
                20,
                Component.literal("Reset 3D View"),
                previewWidget::resetView
        ));
    }

    private void copyDesignId() {
        copyToClipboard(LoomProjectCode.designId(sourceProject));
        status = "Copied design fingerprint";
    }

    private void copyPortableCode() {
        try {
            String code = LoomProjectCode.encodePortable(sourceProject);
            copyToClipboard(code);
            status = "Copied portable project code ("
                    + code.length()
                    + " chars)";
        } catch (IllegalArgumentException e) {
            status = "Project cannot be encoded as a portable code";
        }
    }

    private void savePortableCode() {
        try {
            Path path = LoomShareExportAdapter.exportPortableCode(
                    sourceProject
            );
            status = "Saved code to " + path.getFileName();
        } catch (IOException | IllegalArgumentException e) {
            LoomStudios.LOGGER.error(
                    "Failed to export Loom portable code",
                    e
            );
            status = "Portable-code export failed";
        }
    }

    private void exportProject() {
        try {
            Path path = LoomShareExportAdapter.exportProject(
                    sourceProject
            );
            status = "Exported " + path.getFileName();
        } catch (IOException e) {
            LoomStudios.LOGGER.error(
                    "Failed to export Loom project",
                    e
            );
            status = "Project export failed";
        }
    }

    private void exportCape() {
        try {
            Path path = LoomShareExportAdapter.exportCapePng(
                    sourceProject
            );
            status = "Exported " + path.getFileName();
        } catch (IOException e) {
            LoomStudios.LOGGER.error(
                    "Failed to export Loom cape PNG",
                    e
            );
            status = "Cape PNG export failed";
        }
    }

    private void exportElytra() {
        try {
            Path path = LoomShareExportAdapter.exportElytraPng(
                    sourceProject
            );
            status = "Exported " + path.getFileName();
        } catch (IOException e) {
            LoomStudios.LOGGER.error(
                    "Failed to export Loom Elytra PNG",
                    e
            );
            status = "Elytra PNG export failed";
        }
    }

    private void pastePortableCode() {
        String clipboard = readClipboard();

        if (clipboard == null || clipboard.isBlank()) {
            incomingProject = null;
            incomingStatus = "Clipboard is empty.";
            updateButtons();
            return;
        }

        try {
            incomingProject = LoomProjectCode.decodePortable(
                    clipboard
            );
            incomingStatus = "Valid "
                    + incomingProject.name()
                    + " • "
                    + LoomProjectCode.designId(incomingProject);
            selectPreview(incomingProject);
            status = "Portable code decoded safely";
        } catch (IllegalArgumentException e) {
            incomingProject = null;
            incomingStatus =
                    "Clipboard does not contain a valid LSP1 project.";
            status = "Portable-code import rejected";
        }

        updateButtons();
    }

    private void importIncoming(boolean openAfterImport) {
        if (incomingProject == null) {
            return;
        }

        if (minecraft.player == null) {
            status = "Local player is not available";
            return;
        }

        LoomProject forked = LoomProjectCode.forkImported(
                incomingProject,
                System.currentTimeMillis()
        );

        try {
            Path path = LocalProjectLibrary.save(forked);
            ProjectLibraryIndex.refresh();
            ProjectLibraryIndex.select(forked.projectId());
            status = "Imported " + forked.name();

            if (openAfterImport) {
                ClientProjectWorkspace.open(
                        path,
                        minecraft.player.getUUID()
                );
                minecraft.setScreen(new CapeEditorScreen(parent));
            }
        } catch (IOException | IllegalArgumentException e) {
            LoomStudios.LOGGER.error(
                    "Failed to import Loom portable project",
                    e
            );
            status = "Import failed";
        }
    }

    private void selectPreview(LoomProject project) {
        previewProject = Objects.requireNonNull(project, "project");
        if (previewWidget != null) {
            previewWidget.resetView();
        }
        updateButtons();
    }

    private void setPreviewMode(
            LoomPlayerPreviewWidget.Mode mode
    ) {
        previewMode = Objects.requireNonNull(mode, "mode");
        if (previewWidget != null) {
            previewWidget.setMode(mode);
            previewWidget.resetView();
        }
        updateButtons();
    }

    private void copyToClipboard(String value) {
        GLFW.glfwSetClipboardString(
                minecraft.getWindow().getWindow(),
                value
        );
    }

    private String readClipboard() {
        return GLFW.glfwGetClipboardString(
                minecraft.getWindow().getWindow()
        );
    }

    private void updateButtons() {
        boolean hasIncoming = incomingProject != null;

        if (importButton != null) {
            importButton.active = hasIncoming;
        }
        if (importOpenButton != null) {
            importOpenButton.active = hasIncoming;
        }
        if (incomingPreviewButton != null) {
            incomingPreviewButton.active = hasIncoming;
        }
        if (sourcePreviewButton != null) {
            sourcePreviewButton.active = previewProject != sourceProject;
        }
        if (capePreviewButton != null) {
            capePreviewButton.active =
                    previewMode != LoomPlayerPreviewWidget.Mode.CAPE;
        }
        if (elytraPreviewButton != null) {
            elytraPreviewButton.active =
                    previewMode != LoomPlayerPreviewWidget.Mode.ELYTRA;
        }
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
        renderLeftPanel(graphics);
        renderCenterPanel(graphics);
        renderFooter(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.fill(
                shellLeft + 8,
                shellTop + 5,
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
                font,
                Component.literal("Loom Studios"),
                width / 2,
                shellTop + 10,
                LoomUiTheme.TEXT
        );
        graphics.drawCenteredString(
                font,
                Component.literal("Share • Import • Export"),
                width / 2,
                shellTop + 23,
                LoomUiTheme.TEXT_MUTED
        );
    }

    private void renderLeftPanel(GuiGraphics graphics) {
        fillPanel(
                graphics,
                leftLeft - 4,
                contentTop - 4,
                leftRight + 4,
                contentBottom
        );

        graphics.drawString(
                font,
                Component.literal("Share Your Design"),
                leftLeft,
                contentTop + 2,
                LoomUiTheme.TEXT,
                false
        );

        String id = LoomProjectCode.designId(sourceProject);
        graphics.drawString(
                font,
                Component.literal(id),
                leftLeft,
                contentTop + 14,
                LoomUiTheme.ACCENT,
                false
        );

        String meta = sourceProject.name()
                + " • schema v"
                + sourceProject.schemaVersion();
        graphics.drawString(
                font,
                Component.literal(
                        font.plainSubstrByWidth(
                                meta,
                                leftRight - leftLeft
                        )
                ),
                leftLeft,
                contentBottom - 27,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                Component.literal(
                        "Exports: .minecraft/loom-studios/exports"
                ),
                leftLeft,
                contentBottom - 14,
                LoomUiTheme.TEXT_MUTED,
                false
        );
    }

    private void renderCenterPanel(GuiGraphics graphics) {
        fillPanel(
                graphics,
                centerLeft,
                contentTop,
                centerRight,
                contentBottom
        );

        graphics.drawString(
                font,
                Component.literal("Import / Redeem"),
                centerLeft + 8,
                contentTop + 8,
                LoomUiTheme.TEXT,
                false
        );

        graphics.drawString(
                font,
                Component.literal(
                        "Portable codes are self-contained and work offline."
                ),
                centerLeft + 8,
                contentTop + 20,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        String clipped = font.plainSubstrByWidth(
                incomingStatus,
                Math.max(40, centerRight - centerLeft - 16)
        );
        graphics.drawString(
                font,
                Component.literal(clipped),
                centerLeft + 8,
                contentTop + 128,
                incomingProject == null
                        ? LoomUiTheme.TEXT_MUTED
                        : LoomUiTheme.ACCENT,
                false
        );

        int infoTop = contentTop + 154;
        graphics.drawString(
                font,
                Component.literal("Visibility & Permissions"),
                centerLeft + 8,
                infoTop,
                LoomUiTheme.TEXT,
                false
        );
        graphics.drawString(
                font,
                Component.literal(
                        "Local/private portable sharing is active."
                ),
                centerLeft + 8,
                infoTop + 14,
                LoomUiTheme.TEXT_MUTED,
                false
        );
        graphics.drawString(
                font,
                Component.literal(
                        "Hosted gallery/friends/public codes are not connected yet."
                ),
                centerLeft + 8,
                infoTop + 26,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        graphics.drawString(
                font,
                Component.literal(
                        "Imported designs are forked to a new project UUID."
                ),
                centerLeft + 8,
                infoTop + 44,
                LoomUiTheme.TEXT_MUTED,
                false
        );
    }

    private void renderFooter(GuiGraphics graphics) {
        int y = shellBottom - 21;
        graphics.fill(
                shellLeft + 1,
                y,
                shellRight - 1,
                shellBottom - 1,
                LoomUiTheme.PANEL_INNER
        );

        graphics.drawString(
                font,
                Component.literal(status),
                shellLeft + 10,
                y + 6,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        String designCount =
                ProjectLibraryIndex.entries().size() + " Designs";
        int countWidth = font.width(designCount);
        graphics.drawString(
                font,
                Component.literal(designCount),
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

    private void goBack() {
        minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        goBack();
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
