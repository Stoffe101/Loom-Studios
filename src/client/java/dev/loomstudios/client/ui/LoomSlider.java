package dev.loomstudios.client.ui;

import dev.loomstudios.client.project.ClientProjectWorkspace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleConsumer;

/** Label, track and numeric value in one row; one drag is one undo entry. */
public final class LoomSlider extends AbstractWidget {
    private final DoubleSupplier value;
    private final DoubleConsumer change;
    private boolean dragging;
    public LoomSlider(int x, int y, int width, String label, DoubleSupplier value, DoubleConsumer change) {
        super(x, y, width, 22, Component.literal(label));
        this.value = value;
        this.change = change;
    }
    @Override protected void renderWidget(GuiGraphics g, int mx, int my, float tick) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, getMessage(), getX(), getY() + 2, LoomUiTheme.TEXT_MUTED, false);
        String text = Math.round(value.getAsDouble() * 100) + "%";
        g.drawString(font, Component.literal(text), getRight() - font.width(text), getY() + 2, LoomUiTheme.TEXT, false);
        int y = getY() + 17;
        int thumb = getX() + 4 + (int)Math.round(value.getAsDouble() * (getWidth() - 8));
        g.fill(getX() + 4, y, getRight() - 4, y + 2, LoomUiTheme.BORDER);
        g.fill(getX() + 4, y, thumb, y + 2, LoomUiTheme.ACCENT);
        g.fill(thumb - 2, y - 2, thumb + 3, y + 4, active ? LoomUiTheme.ACCENT : LoomUiTheme.TEXT_FAINT);
    }
    private void update(double x) { change.accept(Math.max(0, Math.min(1, (x - getX() - 4) / (getWidth() - 8)))); }
    @Override public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return;
        dragging = true;
        ClientProjectWorkspace.beginCompoundEdit();
        update(event.x());
    }
    @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (dragging) update(event.x());
    }
    @Override public void onRelease(MouseButtonEvent event) {
        if (dragging) { dragging = false; ClientProjectWorkspace.endCompoundEdit(); }
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if (!active || !isFocused()) return false;
        double next = switch (event.key()) {
            case 263, 264 -> value.getAsDouble() - 0.01;
            case 262, 265 -> value.getAsDouble() + 0.01;
            case 268 -> 0;
            case 269 -> 1;
            default -> Double.NaN;
        };
        if (Double.isNaN(next)) return false;
        change.accept(Math.max(0, Math.min(1, next)));
        return true;
    }
    @Override public void setFocused(boolean focused) {
        if (!focused && dragging) { dragging = false; ClientProjectWorkspace.endCompoundEdit(); }
        super.setFocused(focused);
    }
    @Override protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.literal(getMessage().getString() + " " + Math.round(value.getAsDouble() * 100) + "%"));
    }
}
