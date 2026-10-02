package dev.loomstudios.client.ui;

public final class LoomUiTheme {
    public static final int BACKDROP = 0xF0060A10;
    public static final int PANEL = 0xF0121822;
    public static final int PANEL_INNER = 0xF0080C12;
    public static final int PANEL_RAISED = 0xFF17212B;
    public static final int PANEL_HEADER = 0xFF0D141C;

    public static final int BORDER = 0xFF31424D;
    public static final int BORDER_SOFT = 0xFF25343E;
    public static final int FRAME_WOOD = 0xFF4A3425;
    public static final int FRAME_WOOD_LIGHT = 0xFF70513A;
    public static final int FRAME_METAL = 0xFF53616C;
    public static final int GOLD = 0xFFE3B56A;

    public static final int ACCENT = 0xFF22D7E8;
    public static final int ACCENT_ALT = 0xFF9B4DFF;
    public static final int ACCENT_SOFT = 0x5536D9EA;
    public static final int ACCENT_ALT_SOFT = 0x559B4DFF;

    public static final int TEXT = 0xFFF2FBFF;
    public static final int TEXT_MUTED = 0xFF98ABB5;
    public static final int TEXT_FAINT = 0xFF677781;

    public static final int BUTTON = 0xFF17242E;
    public static final int BUTTON_HOVER = 0xFF203744;
    public static final int BUTTON_SELECTED = 0xFF183746;
    public static final int BUTTON_DISABLED = 0xFF10161B;
    public static final int DANGER = 0xFFD95567;

    private LoomUiTheme() {
    }

    public static boolean compact(int width, int height) {
        return width <= 700 || height <= 420;
    }

    public static boolean ultraCompact(int width, int height) {
        return width <= 620 || height <= 350;
    }
}
