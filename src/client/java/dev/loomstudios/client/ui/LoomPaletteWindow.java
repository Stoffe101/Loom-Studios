package dev.loomstudios.client.ui;

import dev.loomstudios.client.palette.ColorPaletteLibrary;
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
 * Movable/pinnable custom-palette side window.
 *
 * <p>Left-click palette colors to use them. Right-click a selected palette
 * color to remove it. Export writes a .loompalette file and also copies a
 * share code to the clipboard. Import first tries a share code from the
 * clipboard and otherwise scans the palette imports folder.</p>
 */
public final class LoomPaletteWindow extends AbstractContainerWidget {
    private static final int TITLE_HEIGHT = 22;
    private static final int PALETTE_ROW_HEIGHT = 18;
    private static final int VISIBLE_PALETTE_ROWS = 4;

    private final List<AbstractWidget> children = new ArrayList<>();
    private final IntSupplier selectedColorSupplier;
    private final IntConsumer colorSelected;
    private final Consumer<String> notifier;
    private final IntSupplier screenWidth;
    private final IntSupplier screenHeight;

    private final EditBox nameBox;
    private final LoomButton newButton;
    private final LoomButton saveButton;
    private final LoomButton addColorButton;
    private final LoomButton importButton;
    private final LoomButton exportButton;
    private final LoomButton pinButton;

    private boolean pinned;
    private boolean moving;
    private int paletteScroll;

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
        super(x, y, width, height, Component.literal("Palettes"));
        this.pinned = pinned;
        this.selectedColorSupplier = selectedColorSupplier;
        this.colorSelected = colorSelected;
        this.notifier = notifier;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        ColorPaletteLibrary.refresh();

        this.nameBox = new EditBox(
                Minecraft.getInstance().font,
                x + 8,
                y + 29,
                width - 16,
                18,
                Component.literal("Palette name")
        );
        this.nameBox.setMaxLength(ColorPalette.MAX_NAME_CHARS);
        this.children.add(nameBox);

        int half = (width - 20) / 2;

        this.newButton = button(
                x + 8,
                y + 52,
                half,
                "New",
                this::createPalette
        );
        this.saveButton = button(
                x + 12 + half,
                y + 52,
                half,
                "Save Name",
                this::saveName
        );

        this.addColorButton = button(
                x + 8,
                y + 77,
                width - 16,
                "Add Current Color",
                this::addCurrentColor
        );

        this.importButton = button(
                x + 8,
                y + 102,
                half,
                "Import",
                this::importPalette
        );
        this.exportButton = button(
                x + 12 + half,
                y + 102,
                half,
                "Export",
                this::exportPalette
        );

        this.pinButton = button(
                x + width - 54,
                y + 3,
                46,
                pinned ? "Pinned" : "Pin",
                this::togglePinned
        );

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
                width,
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

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
        this.pinButton.setMessage(
                Component.literal(pinned ? "Pinned" : "Pin")
        );
    }

    private void togglePinned() {
        setPinned(!pinned);
    }

    private void createPalette() {
        try {
            ColorPalette palette = ColorPaletteLibrary.create(
                    nameBox.getValue(),
                    selectedColorSupplier.getAsInt()
            );
            nameBox.setValue(palette.name());
            paletteScroll = Math.max(
                    0,
                    ColorPaletteLibrary.palettes().indexOf(palette)
                            - VISIBLE_PALETTE_ROWS
                            + 1
            );
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
            notifier.accept("Saved palette: " + palette.name());
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not save palette name");
        }
    }

    private void addCurrentColor() {
        try {
            ColorPalette palette = ColorPaletteLibrary.addColorToSelected(
                    selectedColorSupplier.getAsInt()
            );
            notifier.accept(
                    "Palette now has " + palette.colors().size() + " colors"
            );
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not add color to palette");
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
                notifier.accept("Imported palette: " + palette.name());
                return;
            }

            int imported = ColorPaletteLibrary.importInbox();
            if (imported > 0) {
                syncSelectedPalette();
                notifier.accept("Imported " + imported + " palette(s)");
            } else {
                notifier.accept(
                        "Clipboard has no Loom palette code; import folder is empty"
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
                    "Palette exported; share code copied to clipboard"
            );
        } catch (IOException | RuntimeException e) {
            notifier.accept("Could not export palette");
        }
    }

    private void syncSelectedPalette() {
        Optional<ColorPalette> selected = ColorPaletteLibrary.selected();
        this.nameBox.setValue(
                selected.map(ColorPalette::name).orElse("New Palette")
        );

        boolean active = selected.isPresent();
        this.saveButton.active = active;
        this.addColorButton.active = active;
        this.exportButton.active = active;
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
                Component.literal("Palettes"),
                getX() + 8,
                getY() + 7,
                LoomUiTheme.TEXT,
                false
        );

        for (AbstractWidget child : children) {
            child.render(graphics, mouseX, mouseY, partialTick);
        }

        renderPaletteList(graphics);
        renderSelectedColors(graphics);
    }

    private void renderPaletteList(GuiGraphics graphics) {
        int listY = getY() + 129;
        int listWidth = getWidth() - 16;

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Saved palettes"),
                getX() + 8,
                listY,
                LoomUiTheme.TEXT_MUTED,
                false
        );
        listY += 13;

        List<ColorPalette> palettes = ColorPaletteLibrary.palettes();
        UUIDHolder selected = new UUIDHolder(
                ColorPaletteLibrary.selected()
                        .map(ColorPalette::id)
                        .orElse(null)
        );

        for (int row = 0; row < VISIBLE_PALETTE_ROWS; row++) {
            int index = paletteScroll + row;
            int y = listY + row * PALETTE_ROW_HEIGHT;

            if (index >= palettes.size()) {
                graphics.fill(
                        getX() + 8,
                        y,
                        getX() + 8 + listWidth,
                        y + 16,
                        LoomUiTheme.PANEL_INNER
                );
                continue;
            }

            ColorPalette palette = palettes.get(index);
            boolean isSelected = palette.id().equals(selected.value);

            graphics.fill(
                    getX() + 8,
                    y,
                    getX() + 8 + listWidth,
                    y + 16,
                    isSelected ? 0xFF213744 : LoomUiTheme.PANEL_INNER
            );

            int previewX = getX() + 12;
            int previewColors = Math.min(4, palette.colors().size());
            for (int i = 0; i < previewColors; i++) {
                graphics.fill(
                        previewX + i * 8,
                        y + 4,
                        previewX + i * 8 + 6,
                        y + 10,
                        palette.colors().get(i)
                );
            }

            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(palette.name()),
                    getX() + 48,
                    y + 4,
                    isSelected ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT,
                    false
            );
        }
    }

    private void renderSelectedColors(GuiGraphics graphics) {
        int swatchY = getY() + 223;

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal("Colors  •  right-click to remove"),
                getX() + 8,
                swatchY - 12,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        ColorPaletteLibrary.selected().ifPresent(palette -> {
            int swatch = 16;
            int gap = 4;
            int columns = Math.max(1, (getWidth() - 16) / (swatch + gap));

            for (int i = 0; i < palette.colors().size(); i++) {
                int row = i / columns;
                int col = i % columns;
                int x = getX() + 8 + col * (swatch + gap);
                int y = swatchY + row * (swatch + gap);

                if (y + swatch > getBottom() - 6) {
                    break;
                }

                graphics.fill(
                        x - 1,
                        y - 1,
                        x + swatch + 1,
                        y + swatch + 1,
                        LoomUiTheme.BORDER
                );
                graphics.fill(
                        x,
                        y,
                        x + swatch,
                        y + swatch,
                        palette.colors().get(i)
                );
            }
        });
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

        if (clickPaletteList(event)) {
            return true;
        }

        if (clickSelectedColor(event)) {
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
        int listTop = getY() + 142;
        int listBottom = listTop
                + VISIBLE_PALETTE_ROWS * PALETTE_ROW_HEIGHT;

        if (mouseX >= getX() + 8
                && mouseX < getRight() - 8
                && mouseY >= listTop
                && mouseY < listBottom) {
            int max = Math.max(
                    0,
                    ColorPaletteLibrary.palettes().size()
                            - VISIBLE_PALETTE_ROWS
            );
            paletteScroll = Math.max(
                    0,
                    Math.min(
                            max,
                            paletteScroll + (scrollY < 0 ? 1 : -1)
                    )
            );
            return true;
        }

        return false;
    }

    private boolean clickPaletteList(MouseButtonEvent event) {
        int listTop = getY() + 142;
        int listBottom = listTop
                + VISIBLE_PALETTE_ROWS * PALETTE_ROW_HEIGHT;

        if (event.button() != 0
                || event.x() < getX() + 8
                || event.x() >= getRight() - 8
                || event.y() < listTop
                || event.y() >= listBottom) {
            return false;
        }

        int row = (int)((event.y() - listTop) / PALETTE_ROW_HEIGHT);
        int index = paletteScroll + row;
        List<ColorPalette> palettes = ColorPaletteLibrary.palettes();

        if (index >= palettes.size()) {
            return true;
        }

        ColorPalette palette = palettes.get(index);
        ColorPaletteLibrary.select(palette.id());
        nameBox.setValue(palette.name());
        syncSelectedPalette();
        return true;
    }

    private boolean clickSelectedColor(MouseButtonEvent event) {
        Optional<ColorPalette> selected = ColorPaletteLibrary.selected();
        if (selected.isEmpty()) {
            return false;
        }

        int swatch = 16;
        int gap = 4;
        int columns = Math.max(1, (getWidth() - 16) / (swatch + gap));
        int swatchY = getY() + 223;
        ColorPalette palette = selected.get();

        for (int i = 0; i < palette.colors().size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = getX() + 8 + col * (swatch + gap);
            int y = swatchY + row * (swatch + gap);

            if (y + swatch > getBottom() - 6) {
                break;
            }

            if (event.x() >= x
                    && event.x() < x + swatch
                    && event.y() >= y
                    && event.y() < y + swatch) {
                if (event.button() == 1) {
                    try {
                        ColorPaletteLibrary.removeColorFromSelected(i);
                    } catch (IOException ignored) {
                        notifier.accept("Could not remove palette color");
                    }
                } else if (event.button() == 0) {
                    colorSelected.accept(palette.colors().get(i));
                }
                return true;
            }
        }

        return false;
    }

    public void moveBy(int dx, int dy) {
        moveTo(getX() + dx, getY() + dy);
    }

    public void moveTo(int requestedX, int requestedY) {
        int maxX = Math.max(0, screenWidth.getAsInt() - getWidth());
        int maxY = Math.max(0, screenHeight.getAsInt() - getHeight());

        int nextX = Math.max(0, Math.min(maxX, requestedX));
        int nextY = Math.max(0, Math.min(maxY, requestedY));

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

    private record UUIDHolder(java.util.UUID value) {
    }
}
