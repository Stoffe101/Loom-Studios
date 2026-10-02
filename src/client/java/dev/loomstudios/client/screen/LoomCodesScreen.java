package dev.loomstudios.client.screen;

import dev.loomstudios.LoomStudios;
import dev.loomstudios.client.project.ClientProjectWorkspace;
import dev.loomstudios.client.project.LocalProjectLibrary;
import dev.loomstudios.client.project.ProjectLibraryIndex;
import dev.loomstudios.client.sharing.LoomShareExportAdapter;
import dev.loomstudios.client.ui.LoomButton;
import dev.loomstudios.client.ui.LoomPlayerPreviewWidget;
import dev.loomstudios.client.ui.LoomScreenChrome;
import dev.loomstudios.client.ui.LoomUiTheme;
import dev.loomstudios.project.LoomProject;
import dev.loomstudios.project.LoomProjectCode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class LoomCodesScreen extends Screen {
    private static final Component TITLE =
            Component.literal("Loom Studios - Share / Export");

    private enum Workspace {
        EXPORT,
        IMPORT
    }

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
    private LoomButton exportTabButton;
    private LoomButton importTabButton;
    private final List<LoomButton> exportControls = new ArrayList<>();
    private final List<LoomButton> importControls = new ArrayList<>();
    private Workspace workspace = Workspace.EXPORT;
    private boolean compactMode;

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
        exportControls.clear();
        importControls.clear();
        compactMode = LoomUiTheme.compact(width, height);

        int margin = compactMode ? 5 : 9;
        int headerHeight = LoomScreenChrome.headerHeight(compactMode);
        int footerHeight = 18;
        int gap = compactMode ? 5 : 8;

        shellLeft = margin;
        shellRight = width - margin;
        shellTop = 0;
        shellBottom = height;
        contentTop = headerHeight + 5;
        contentBottom = height - footerHeight - 4;

        int previewWidth = compactMode
                ? Math.max(152, Math.min(178, width / 3))
                : Math.max(200, Math.min(250, width / 4));

        leftLeft = shellLeft;
        leftRight = shellLeft;
        centerLeft = shellLeft;
        centerRight = shellRight - previewWidth - gap;
        rightLeft = centerRight + gap;
        rightRight = shellRight;

        buildWorkspaceTabs();
        buildExportControls();
        buildImportControls();
        buildPreview();
        updateWorkspaceVisibility();
        updateButtons();
    }

    private LoomButton actionButton(
            int x,
            int y,
            int width,
            int height,
            String label,
            LoomButton.Icon icon,
            Runnable action
    ) {
        LoomButton button = new LoomButton(
                x,
                y,
                width,
                height,
                Component.literal(label),
                icon,
                false,
                action
        );
        addRenderableWidget(button);
        return button;
    }

    private void buildWorkspaceTabs() {
        int top = contentTop;
        int tabHeight = compactMode ? 20 : 23;
        int mainWidth = centerRight - centerLeft;
        int backWidth = compactMode ? 28 : 72;
        int gap = 4;
        int tabWidth = Math.max(
                64,
                (mainWidth - backWidth - gap * 3) / 2
        );

        LoomButton back = new LoomButton(
                centerLeft,
                top,
                backWidth,
                tabHeight,
                Component.literal("Back"),
                LoomButton.Icon.BACK,
                false,
                this::goBack
        );
        addRenderableWidget(back);

        exportTabButton = new LoomButton(
                centerLeft + backWidth + gap,
                top,
                tabWidth,
                tabHeight,
                Component.literal("Export"),
                LoomButton.Icon.EXPORT,
                false,
                () -> setWorkspace(Workspace.EXPORT)
        );
        addRenderableWidget(exportTabButton);

        importTabButton = new LoomButton(
                centerLeft + backWidth + gap + tabWidth + gap,
                top,
                centerRight
                        - (centerLeft + backWidth + gap + tabWidth + gap),
                tabHeight,
                Component.literal("Import"),
                LoomButton.Icon.IMAGE,
                false,
                () -> setWorkspace(Workspace.IMPORT)
        );
        addRenderableWidget(importTabButton);
    }

    private void buildExportControls() {
        int panelTop = contentTop + (compactMode ? 27 : 31);
        int left = centerLeft + 7;
        int right = centerRight - 7;
        int gap = compactMode ? 4 : 6;
        int buttonHeight = compactMode ? 25 : 30;
        int half = Math.max(80, (right - left - gap) / 2);
        int y = panelTop + (compactMode ? 35 : 46);

        exportControls.add(actionButton(
                left,
                y,
                half,
                buttonHeight,
                "Copy Design ID",
                LoomButton.Icon.COPY,
                this::copyDesignId
        ));
        exportControls.add(actionButton(
                left + half + gap,
                y,
                right - (left + half + gap),
                buttonHeight,
                "Copy Portable Code",
                LoomButton.Icon.SHARE,
                this::copyPortableCode
        ));
        y += buttonHeight + gap;

        exportControls.add(actionButton(
                left,
                y,
                half,
                buttonHeight,
                "Export Project (.loom)",
                LoomButton.Icon.SAVE,
                this::exportProject
        ));
        exportControls.add(actionButton(
                left + half + gap,
                y,
                right - (left + half + gap),
                buttonHeight,
                "Save Portable Code",
                LoomButton.Icon.EXPORT,
                this::savePortableCode
        ));
        y += buttonHeight + gap;

        exportControls.add(actionButton(
                left,
                y,
                half,
                buttonHeight,
                "Export Cape PNG",
                LoomButton.Icon.CAPE,
                this::exportCape
        ));
        exportControls.add(actionButton(
                left + half + gap,
                y,
                right - (left + half + gap),
                buttonHeight,
                "Export Elytra PNG",
                LoomButton.Icon.ELYTRA,
                this::exportElytra
        ));
    }

    private void buildImportControls() {
        int panelTop = contentTop + (compactMode ? 27 : 31);
        int left = centerLeft + 7;
        int right = centerRight - 7;
        int gap = compactMode ? 4 : 6;
        int buttonHeight = compactMode ? 25 : 30;
        int half = Math.max(80, (right - left - gap) / 2);
        int y = panelTop + (compactMode ? 35 : 46);

        importControls.add(actionButton(
                left,
                y,
                right - left,
                buttonHeight,
                "Paste Portable Code",
                LoomButton.Icon.SHARE,
                this::pastePortableCode
        ));
        y += buttonHeight + gap;

        importControls.add(actionButton(
                left,
                y,
                right - left,
                buttonHeight,
                "Open .loom Project File",
                LoomButton.Icon.SAVE,
                this::importProjectFile
        ));
        y += buttonHeight + gap;

        sourcePreviewButton = actionButton(
                left,
                y,
                half,
                buttonHeight,
                "Preview Mine",
                LoomButton.Icon.CAPE,
                () -> selectPreview(sourceProject)
        );
        incomingPreviewButton = actionButton(
                left + half + gap,
                y,
                right - (left + half + gap),
                buttonHeight,
                "Preview Import",
                LoomButton.Icon.IMAGE,
                () -> {
                    if (incomingProject != null) {
                        selectPreview(incomingProject);
                    }
                }
        );
        importControls.add(sourcePreviewButton);
        importControls.add(incomingPreviewButton);
        y += buttonHeight + gap;

        importButton = actionButton(
                left,
                y,
                half,
                buttonHeight,
                "Import to Library",
                LoomButton.Icon.PLUS,
                () -> importIncoming(false)
        );
        importOpenButton = actionButton(
                left + half + gap,
                y,
                right - (left + half + gap),
                buttonHeight,
                "Import + Open",
                LoomButton.Icon.IMAGE,
                () -> importIncoming(true)
        );
        importControls.add(importButton);
        importControls.add(importOpenButton);
    }

    private void buildPreview() {
        int width = rightRight - rightLeft;
        int previewHeight = Math.max(
                compactMode ? 96 : 130,
                contentBottom - contentTop - (compactMode ? 48 : 56)
        );

        previewWidget = new LoomPlayerPreviewWidget(
                rightLeft,
                contentTop,
                width,
                previewHeight,
                () -> previewProject,
                previewMode
        );
        addRenderableWidget(previewWidget);

        int gap = 4;
        int half = Math.max(54, (width - gap) / 2);
        int y = contentTop + previewHeight + 3;
        int h = compactMode ? 18 : 20;

        capePreviewButton = new LoomButton(
                rightLeft,
                y,
                half,
                h,
                Component.literal("Cape"),
                LoomButton.Icon.CAPE,
                compactMode,
                () -> setPreviewMode(
                        LoomPlayerPreviewWidget.Mode.CAPE
                )
        );
        elytraPreviewButton = new LoomButton(
                rightLeft + half + gap,
                y,
                width - half - gap,
                h,
                Component.literal("Elytra"),
                LoomButton.Icon.ELYTRA,
                compactMode,
                () -> setPreviewMode(
                        LoomPlayerPreviewWidget.Mode.ELYTRA
                )
        );
        addRenderableWidget(capePreviewButton);
        addRenderableWidget(elytraPreviewButton);

        addRenderableWidget(new LoomButton(
                rightLeft,
                y + h + 3,
                width,
                h,
                Component.literal("Reset 3D View"),
                LoomButton.Icon.RESET,
                compactMode,
                previewWidget::resetView
        ));
    }

    private void setWorkspace(Workspace next) {
        workspace = next;
        updateWorkspaceVisibility();
    }

    private void updateWorkspaceVisibility() {
        boolean exporting = workspace == Workspace.EXPORT;
        if (exportTabButton != null) {
            exportTabButton.setSelected(exporting);
        }
        if (importTabButton != null) {
            importTabButton.setSelected(!exporting);
        }
        for (LoomButton button : exportControls) {
            button.visible = exporting;
        }
        for (LoomButton button : importControls) {
            button.visible = !exporting;
        }
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

    private void importProjectFile() {
        String selected = TinyFileDialogs.tinyfd_openFileDialog(
                "Loom Studios - Import Project",
                "",
                null,
                "Loom project (*.loom)",
                false
        );

        if (selected == null || selected.isBlank()) {
            return;
        }

        try {
            Path path = Path.of(selected);
            long size = java.nio.file.Files.size(path);

            if (size <= 0
                    || size > dev.loomstudios.project.LoomProjectCodec.MAX_SERIALIZED_BYTES) {
                throw new IllegalArgumentException(
                        "Project file size is outside Loom limits"
                );
            }

            incomingProject =
                    dev.loomstudios.project.LoomProjectCodec.decode(
                            java.nio.file.Files.readAllBytes(path)
                    );
            incomingStatus = "Valid file: "
                    + incomingProject.name()
                    + " • "
                    + LoomProjectCode.designId(incomingProject);
            selectPreview(incomingProject);
            status = "Project file decoded safely";
        } catch (IOException | IllegalArgumentException e) {
            LoomStudios.LOGGER.warn(
                    "Rejected imported Loom project file {}",
                    selected,
                    e
            );
            incomingProject = null;
            incomingStatus = "Selected .loom file is invalid or unsupported.";
            status = "Project-file import rejected";
        }

        updateButtons();
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
        minecraft.keyboardHandler.setClipboard(value);
    }

    private String readClipboard() {
        return minecraft.keyboardHandler.getClipboard();
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
        LoomScreenChrome.renderBackdrop(graphics, width, height);
        LoomScreenChrome.renderBrandHeader(
                graphics,
                width,
                compactMode
                        ? "Share / Export"
                        : "Share • Import • Export",
                compactMode
        );

        LoomScreenChrome.panel(
                graphics,
                centerLeft,
                contentTop + (compactMode ? 27 : 31),
                centerRight,
                contentBottom
        );
        LoomScreenChrome.panel(
                graphics,
                rightLeft,
                contentTop,
                rightRight,
                contentBottom
        );
        if (workspace == Workspace.EXPORT) {
            renderExportIntro(graphics);
        } else {
            renderImportIntro(graphics);
        }

        LoomScreenChrome.footer(
                graphics,
                width,
                height,
                status,
                ProjectLibraryIndex.entries().size() + " Designs"
        );

        super.render(graphics, mouseX, mouseY, partialTick);
        updateButtons();
    }

    private void renderExportIntro(GuiGraphics graphics) {
        int left = centerLeft + 8;
        int top = contentTop + (compactMode ? 34 : 39);
        int max = centerRight - centerLeft - 16;

        graphics.drawString(
                font,
                Component.literal("Export current design"),
                left,
                top,
                LoomUiTheme.TEXT,
                false
        );

        String id = LoomProjectCode.designId(sourceProject);
        graphics.fill(left-2,top+11,centerRight-8,top+25,0xFF123347);
        graphics.fill(left-2,top+11,left,top+25,LoomUiTheme.ACCENT);
        graphics.drawString(
                font,
                Component.literal(id).withStyle(net.minecraft.ChatFormatting.BOLD),
                left,
                top + 13,
                LoomUiTheme.ACCENT,
                false
        );

        if (!compactMode) {
            String hint =
                    "PNG = ready texture • .loom = editable project • Portable = copy/share";
            graphics.drawString(
                    font,
                    Component.literal(
                            font.plainSubstrByWidth(hint, max)
                    ),
                    left,
                    top + 27,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }
    }

    private void renderImportIntro(GuiGraphics graphics) {
        int left = centerLeft + 8;
        int top = contentTop + (compactMode ? 34 : 39);
        int max = centerRight - centerLeft - 16;

        graphics.drawString(
                font,
                Component.literal("Import a shared design"),
                left,
                top,
                LoomUiTheme.TEXT,
                false
        );

        String message = incomingProject == null
                ? "Paste LSP1 code or open a .loom file"
                : incomingStatus;
        graphics.drawString(
                font,
                Component.literal(
                        font.plainSubstrByWidth(message, max)
                ),
                left,
                top + 14,
                incomingProject == null
                        ? LoomUiTheme.TEXT_MUTED
                        : LoomUiTheme.ACCENT,
                false
        );

        if (!compactMode) {
            graphics.drawString(
                    font,
                    Component.literal(
                            font.plainSubstrByWidth("Imports create a new local project.", max)
                    ),
                    left,
                    top + 28,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }
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
