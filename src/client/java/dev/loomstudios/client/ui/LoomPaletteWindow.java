package dev.loomstudios.client.ui;

import dev.loomstudios.client.palette.ColorPaletteLibrary;
import dev.loomstudios.client.palette.EditorColors;
import dev.loomstudios.palette.ColorPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Compact Illustrator/Photoshop-style swatches dock.
 *
 * <p>The swatch groups are always the primary surface. Palette-management
 * controls can collapse away, which keeps the floating window useful on
 * 1920x1080 GUI scale 3 instead of covering most of the editor.</p>
 */
public final class LoomPaletteWindow extends AbstractContainerWidget {
    private static final int TITLE_HEIGHT = 22;
    private static final int GROUP_HEADER_HEIGHT = 17;
    private static final int SWATCH = 15;
    private static final int SWATCH_GAP = 3;
    private static final int GROUP_GAP = 7;

    private final List<AbstractWidget> children = new ArrayList<>();
    private final IntSupplier selectedColorSupplier;
    private final IntConsumer colorSelected;
    private final Consumer<String> notifier;
    private final IntSupplier screenWidth;
    private final IntSupplier screenHeight;

    private final boolean compactMode;

    private final EditBox nameBox;
    private final LoomButton newButton;
    private final LoomButton saveButton;
    private final LoomButton addColorButton;
    private final LoomButton importButton;
    private final LoomButton exportButton;
    private final LoomButton deleteButton;
    private final LoomButton manageButton;
    private final LoomButton pinButton;

    private Runnable onClose = () -> visible = false;
    public void setOnClose(Runnable onClose) { this.onClose = onClose; }

    private boolean pinned;
    private boolean managementExpanded;
    private boolean moving;
    private int swatchesScroll;

    public LoomPaletteWindow(
            int x,
            int y,
            int width,
            int height,
            boolean pinned,
            IntSupplier selectedColorSupplier,
            IntConsumer colorSelected,
            Consumer<String> notifier,
            IntSupplier screenWidth,
            IntSupplier screenHeight
    ) {
        super(x, y, width, height, Component.literal("Swatches"));
        this.pinned = pinned;
        this.selectedColorSupplier = selectedColorSupplier;
        this.colorSelected = colorSelected;
        this.notifier = notifier;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.compactMode = screenWidth.getAsInt() <= 700
                || screenHeight.getAsInt() <= 420;
        this.managementExpanded = !compactMode;

        ColorPaletteLibrary.refresh();

        int pinWidth = 40;
        int manageWidth = 38;

        this.pinButton = button(
                x + width - pinWidth - 28,
                y + 2,
                pinWidth,
                pinned ? "Pinned" : "Pin",
                this::togglePinned
        );

        this.manageButton = button(
                x + width - pinWidth - manageWidth - 32,
                y + 2,
                manageWidth,
                managementExpanded ? "Done" : "Edit",
                this::toggleManagement
        );

        LoomButton close = new LoomButton(x+width-24,y+2,18,18,Component.literal("Close swatches"),LoomButton.Icon.CLOSE,true,() -> this.onClose.run());
        close.setDanger(true);
        children.add(close);

        this.nameBox = new EditBox(
                Minecraft.getInstance().font,
                x + 8,
                y + 28,
                width - 16,
                18,
                Component.literal("Palette name")
        );
        this.nameBox.setMaxLength(ColorPalette.MAX_NAME_CHARS);
        this.children.add(nameBox);

        int third = Math.max(38, (width - 24) / 3);

        this.newButton = button(
                x + 8,
                y + 51,
                third,
                "New",
                this::createPalette
        );
        this.saveButton = button(
                x + 12 + third,
                y + 51,
                third,
                "Rename",
                this::saveName
        );
        this.deleteButton = button(
                x + 16 + third * 2,
                y + 51,
                Math.max(38, width - 24 - third * 2),
                "Delete",
                this::deletePalette
        );

        this.addColorButton = button(
                x + 8,
                y + 76,
                width - 16,
                "Add Current Color",
                this::addCurrentColor
        );

        int half = (width - 20) / 2;
        this.importButton = button(
                x + 8,
                y + 101,
                half,
                "Import",
                this::importPalette
        );
        this.exportButton = button(
                x + 12 + half,
                y + 101,
                half,
                compactMode ? "Export" : "Export Selected",
                this::exportPalette
        );

        applyManagementVisibility();
        syncSelectedPalette();
    }

    private LoomButton button(
            int x,
            int y,
            int width,
            String label,
            Runnable action
    ) {
        LoomButton button = new LoomButton(
                x,
                y,
                Math.max(28, width),
                20,
                Component.literal(label),
                action
        );
        children.add(button);
        return button;
    }

    public boolean pinned() {
        return pinned;
    }

    public boolean isEditingName() {
        return managementExpanded && nameBox.isFocused();
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        pinButton.setMessage(Component.literal(pinned ? "Pinned" : "Pin"));
    }

    private void togglePinned() {
        setPinned(!pinned);
    }

    private void toggleManagement() {
        this.managementExpanded = !this.managementExpanded;
        applyManagementVisibility();
        clampScroll();
    }

    private void applyManagementVisibility() {
        manageButton.setMessage(
                Component.literal(managementExpanded ? "Done" : "Edit")
        );

        nameBox.setVisible(managementExpanded);
        newButton.visible = managementExpanded;
        saveButton.visible = managementExpanded;
        deleteButton.visible = managementExpanded;
        addColorButton.visible = managementExpanded;
        importButton.visible = managementExpanded;
        exportButton.visible = managementExpanded;

        if (!managementExpanded) {
            nameBox.setFocused(false);
        }
    }

    private void createPalette() {
        try {
            ColorPalette palette = ColorPaletteLibrary.create(
                    nameBox.getValue(),
                    selectedColorSupplier.getAsInt()
            );
            nameBox.setValue(palette.name());
            ensureSelectedVisible();
            notifier.accept("Created palette: " + palette.name());
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not create palette");
        }
    }

    private void saveName() {
        try {
            ColorPalette palette = ColorPaletteLibrary.renameSelected(
                    nameBox.getValue()
            );
            nameBox.setValue(palette.name());
            notifier.accept("Saved palette name");
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not save palette name");
        }
    }

    private void deletePalette() {
        try {
            String name = ColorPaletteLibrary.selected()
                    .map(ColorPalette::name)
                    .orElse("palette");
            ColorPaletteLibrary.deleteSelected();
            syncSelectedPalette();
            notifier.accept("Deleted palette: " + name);
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not delete palette");
        }
    }

    private void addCurrentColor() {
        try {
            ColorPalette palette = ColorPaletteLibrary.addColorToSelected(
                    selectedColorSupplier.getAsInt()
            );
            notifier.accept(
                    palette.name() + ": " + palette.colors().size() + " swatches"
            );
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not add color to selected palette");
        }
    }

    private void importPalette() {
        String clipboard = Minecraft.getInstance()
                .keyboardHandler
                .getClipboard()
                .trim();

        try {
            if (clipboard.startsWith("LOOMPAL1:")) {
                ColorPalette palette =
                        ColorPaletteLibrary.importShareCode(clipboard);
                nameBox.setValue(palette.name());
                ensureSelectedVisible();
                notifier.accept("Imported palette: " + palette.name());
                return;
            }

            int imported = ColorPaletteLibrary.importInbox();
            syncSelectedPalette();

            if (imported > 0) {
                ensureSelectedVisible();
                notifier.accept("Imported " + imported + " palette(s)");
            } else {
                notifier.accept(
                        "No Loom palette code on clipboard and import folder is empty"
                );
            }
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not import palette");
        }
    }

    private void exportPalette() {
        try {
            ColorPaletteLibrary.ExportResult result =
                    ColorPaletteLibrary.exportSelected();

            Minecraft.getInstance().keyboardHandler.setClipboard(
                    result.shareCode()
            );

            notifier.accept(
                    "Exported selected palette; share code copied"
            );
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not export selected palette");
        }
    }

    private void syncSelectedPalette() {
        Optional<ColorPalette> selected = ColorPaletteLibrary.selected();
        nameBox.setValue(
                selected.map(ColorPalette::name).orElse("New Palette")
        );

        boolean active = selected.isPresent();
        saveButton.active = active;
        addColorButton.active = active;
        exportButton.active = active;
        deleteButton.active = active;
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getBottom(),
                LoomUiTheme.BORDER
        );
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getRight() - 1,
                getBottom() - 1,
                LoomUiTheme.PANEL
        );
        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getY() + TITLE_HEIGHT,
                0xFF101923
        );
        graphics.fill(
                getX(),
                getY(),
                getRight(),
                getY() + 2,
                pinned ? LoomUiTheme.ACCENT_ALT : LoomUiTheme.ACCENT
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Swatches"),
                getX() + 8,
                getY() + 7,
                LoomUiTheme.TEXT,
                false
        );

        for (AbstractWidget child : children) {
            child.render(graphics, mouseX, mouseY, partialTick);
        }

        renderSwatchGroups(graphics);
    }

    private void renderSwatchGroups(GuiGraphics graphics) {
        int labelY = managementBottom() + 7;
        int top = labelY + 13;
        int bottom = getBottom() - 7;

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Recent · Design · Saved palettes"),
                getX() + 8,
                labelY,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        if (bottom <= top) {
            return;
        }

        graphics.enableScissor(getX() + 5, top, getRight() - 5, bottom);

        int y = top - swatchesScroll;
        int columns = swatchColumns();
        Optional<ColorPalette> selected = ColorPaletteLibrary.selected();

        for (ColorPalette palette : EditorColors.groups()) {
            int rows = Math.max(
                    1,
                    (palette.colors().size() + columns - 1) / columns
            );
            int groupHeight = GROUP_HEADER_HEIGHT
                    + rows * (SWATCH + SWATCH_GAP)
                    + GROUP_GAP;

            if (y + groupHeight >= top && y <= bottom) {
                boolean isSelected = selected
                        .map(value -> value.id().equals(palette.id()))
                        .orElse(false);

                graphics.fill(
                        getX() + 7,
                        y,
                        getRight() - 7,
                        y + GROUP_HEADER_HEIGHT - 1,
                        isSelected ? 0xFF213744 : 0xFF111820
                );
                graphics.drawString(
                        Minecraft.getInstance().font,
                        Component.literal(
                                palette.name()
                                        + "  •  "
                                        + palette.colors().size()
                        ),
                        getX() + 12,
                        y + 4,
                        isSelected ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT,
                        false
                );

                int swatchesY = y + GROUP_HEADER_HEIGHT + 2;

                for (int i = 0; i < palette.colors().size(); i++) {
                    int row = i / columns;
                    int col = i % columns;
                    int x = getX() + 10 + col * (SWATCH + SWATCH_GAP);
                    int sy = swatchesY + row * (SWATCH + SWATCH_GAP);

                    renderSwatch(
                            graphics,
                            x,
                            sy,
                            palette.colors().get(i)
                    );
                }
            }

            y += groupHeight;
        }

        graphics.disableScissor();

        int total = totalSwatchContentHeight();
        int viewport = Math.max(1, bottom - top);

        if (total > viewport) {
            int trackX = getRight() - 5;
            int thumbHeight = Math.max(
                    16,
                    viewport * viewport / total
            );
            int maxScroll = total - viewport;
            int thumbY = top
                    + (int)((viewport - thumbHeight)
                    * (swatchesScroll / (double)Math.max(1, maxScroll)));

            graphics.fill(
                    trackX - 2,
                    top,
                    trackX,
                    bottom,
                    0xFF1B252E
            );
            graphics.fill(
                    trackX - 3,
                    thumbY,
                    trackX + 1,
                    thumbY + thumbHeight,
                    LoomUiTheme.TEXT_MUTED
            );
        }

        if (EditorColors.groups().isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    Component.literal(
                            compactMode
                                    ? "Edit → create a palette"
                                    : "Create a named palette, then add colors"
                    ),
                    getX() + getWidth() / 2,
                    top + 18,
                    LoomUiTheme.TEXT_MUTED
            );
        }
    }

    private static void renderSwatch(
            GuiGraphics graphics,
            int x,
            int y,
            int color
    ) {
        graphics.fill(
                x - 1,
                y - 1,
                x + SWATCH + 1,
                y + SWATCH + 1,
                LoomUiTheme.BORDER
        );

        int cell = 4;
        for (int py = 0; py < SWATCH; py += cell) {
            for (int px = 0; px < SWATCH; px += cell) {
                int checker = (((px / cell) + (py / cell)) & 1) == 0
                        ? 0xFF3B444C
                        : 0xFF252C32;
                graphics.fill(
                        x + px,
                        y + py,
                        Math.min(x + SWATCH, x + px + cell),
                        Math.min(y + SWATCH, y + py + cell),
                        checker
                );
            }
        }

        graphics.fill(x, y, x + SWATCH, y + SWATCH, color);
    }

    private int managementBottom() {
        return managementExpanded ? getY() + 121 : getY() + TITLE_HEIGHT;
    }

    private int swatchColumns() {
        return Math.max(
                1,
                (getWidth() - 24) / (SWATCH + SWATCH_GAP)
        );
    }

    private int totalSwatchContentHeight() {
        int columns = swatchColumns();
        int height = 0;

        for (ColorPalette palette : EditorColors.groups()) {
            int rows = Math.max(
                    1,
                    (palette.colors().size() + columns - 1) / columns
            );
            height += GROUP_HEADER_HEIGHT
                    + rows * (SWATCH + SWATCH_GAP)
                    + GROUP_GAP;
        }

        return height;
    }

    private int swatchViewportTop() {
        return managementBottom() + 20;
    }

    private int swatchViewportBottom() {
        return getBottom() - 7;
    }

    private void clampScroll() {
        int viewport = Math.max(
                1,
                swatchViewportBottom() - swatchViewportTop()
        );
        int max = Math.max(0, totalSwatchContentHeight() - viewport);
        swatchesScroll = Math.max(0, Math.min(max, swatchesScroll));
    }

    private void ensureSelectedVisible() {
        clampScroll();
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (!visible || !isMouseOver(event.x(), event.y())) {
            return false;
        }

        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (clickSwatchGroups(event)) {
            return true;
        }

        if (!pinned
                && event.button() == 0
                && event.y() >= getY()
                && event.y() < getY() + TITLE_HEIGHT) {
            moving = true;
            return true;
        }

        return true;
    }

    private boolean clickSwatchGroups(MouseButtonEvent event) {
        int top = swatchViewportTop();
        int bottom = swatchViewportBottom();

        if (event.y() < top
                || event.y() >= bottom
                || event.x() < getX() + 7
                || event.x() >= getRight() - 7) {
            return false;
        }

        int y = top - swatchesScroll;
        int columns = swatchColumns();

        for (ColorPalette palette : EditorColors.groups()) {
            int rows = Math.max(
                    1,
                    (palette.colors().size() + columns - 1) / columns
            );
            int groupHeight = GROUP_HEADER_HEIGHT
                    + rows * (SWATCH + SWATCH_GAP)
                    + GROUP_GAP;

            if (event.y() >= y
                    && event.y() < y + GROUP_HEADER_HEIGHT) {
                if (event.button() == 0) {
                    if(EditorColors.virtual(palette)){if(palette.id().equals(EditorColors.DESIGN_ID))try{EditorColors.saveDesign();syncSelectedPalette();notifier.accept("Saved design colors as a palette");}catch(IOException|RuntimeException e){notifier.accept("Could not save design palette");}return true;}
                    ColorPaletteLibrary.select(palette.id());
                    syncSelectedPalette();
                }
                return true;
            }

            int swatchesY = y + GROUP_HEADER_HEIGHT + 2;

            for (int i = 0; i < palette.colors().size(); i++) {
                int row = i / columns;
                int col = i % columns;
                int x = getX() + 10 + col * (SWATCH + SWATCH_GAP);
                int sy = swatchesY + row * (SWATCH + SWATCH_GAP);

                if (event.x() >= x
                        && event.x() < x + SWATCH
                        && event.y() >= sy
                        && event.y() < sy + SWATCH) {
                    if(!EditorColors.virtual(palette)){ColorPaletteLibrary.select(palette.id());syncSelectedPalette();}

                    if (event.button() == 1) {
                        if(EditorColors.virtual(palette))return true;
                        try {
                            ColorPaletteLibrary.removeColorFromSelected(i);
                        } catch (IOException ignored) {
                            notifier.accept("Could not remove swatch");
                        }
                    } else if (event.button() == 0) {
                        colorSelected.accept(palette.colors().get(i));
                    }
                    return true;
                }
            }

            y += groupHeight;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (moving && !pinned && event.button() == 0) {
            moveBy((int)Math.round(dx), (int)Math.round(dy));
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (moving) {
            moving = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (mouseY >= swatchViewportTop()
                && mouseY < swatchViewportBottom()
                && mouseX >= getX() + 5
                && mouseX < getRight() - 5) {
            swatchesScroll += scrollY < 0 ? 28 : -28;
            clampScroll();
            return true;
        }

        return false;
    }

    public void moveBy(int dx, int dy) {
        moveTo(getX() + dx, getY() + dy);
    }

    public void moveTo(int requestedX, int requestedY) {
        int maxX = Math.max(8, screenWidth.getAsInt() - 8 - getWidth());
        int maxY = Math.max(8, screenHeight.getAsInt() - 28 - getHeight());

        int nextX = Math.max(8, Math.min(maxX, requestedX));
        int nextY = Math.max(8, Math.min(maxY, requestedY));

        int dx = nextX - getX();
        int dy = nextY - getY();

        super.setX(nextX);
        super.setY(nextY);

        for (AbstractWidget child : children) {
            child.setX(child.getX() + dx);
            child.setY(child.getY() + dy);
        }
    }

    @Override
    protected int contentHeight() {
        return getHeight();
    }

    @Override
    protected double scrollRate() {
        return 0.0;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    public Collection<? extends NarratableEntry> getNarratables() {
        return children;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
