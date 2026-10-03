package dev.loomstudios.ui;

/** Bounded editor composition. Every region has one owner; none reaches the footer. */
public record LoomWorkspaceLayout(
        boolean compact, int headerHeight, int navHeight,
        Rect tools, Rect toolbar, Rect canvas, Rect context,
        Rect timeline, Rect preview, Rect inspectorTabs, Rect inspector
) {
    public record Rect(int left, int top, int right, int bottom) {
        public int width() { return right - left; }
        public int height() { return bottom - top; }
        public boolean contains(Rect r) {
            return r.left >= left && r.top >= top && r.right <= right && r.bottom <= bottom;
        }
    }

    public static LoomWorkspaceLayout create(int width, int height, boolean elytra, int tracks) {
        boolean compact = width <= 700 || height <= 420;
        int margin = 8;
        int gap = compact ? 4 : 6;
        int header = compact ? 36 : 56;
        int nav = compact ? 24 : 28;
        int top = header + nav + gap;
        int bottom = height - 28;
        int rail = compact ? 30 : 106;
        int rightWidth = compact ? 204 : Math.min(286, Math.max(230, width / 4));
        int right = width - margin;
        int rightLeft = right - rightWidth;
        int centerLeft = margin + rail + gap;
        int centerRight = rightLeft - gap;
        int toolbarBottom = top + 22;
        int timelineHeight = !elytra ? 0 : tracks == 0 ? 66
                : Math.min(compact ? 100 : 154, 56 + Math.min(tracks, 5) * 20);
        timelineHeight = Math.min(timelineHeight, Math.max(0, bottom - toolbarBottom - 90 - 26 - gap * 3));
        int timelineTop = bottom - timelineHeight;
        int contextBottom = elytra ? timelineTop - gap : bottom;
        int contextTop = contextBottom - 26;
        // Properties are paged; reserve enough room for the tallest compact page.
        int previewHeight = Math.max(60, Math.min(compact ? 110 : 210, bottom - top - 188));
        int previewBottom = top + previewHeight;
        int tabsTop = previewBottom + gap;
        int tabsBottom = tabsTop + 22;
        return new LoomWorkspaceLayout(compact, header, nav,
                new Rect(margin, top, margin + rail, bottom),
                new Rect(centerLeft, top, centerRight, toolbarBottom),
                new Rect(centerLeft, toolbarBottom + gap, centerRight, contextTop - gap),
                new Rect(centerLeft, contextTop, centerRight, contextBottom),
                new Rect(centerLeft, timelineTop, centerRight, bottom),
                new Rect(rightLeft, top, right, previewBottom),
                new Rect(rightLeft, tabsTop, right, tabsBottom),
                new Rect(rightLeft, tabsBottom + gap, right, bottom));
    }
}
