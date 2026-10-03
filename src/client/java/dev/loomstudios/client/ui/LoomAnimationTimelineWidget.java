package dev.loomstudios.client.ui;

import dev.loomstudios.project.AnimationChannel;
import dev.loomstudios.project.AnimationKeyframe;
import dev.loomstudios.project.AnimationTrack;
import dev.loomstudios.project.LoomAnimation;
import dev.loomstudios.project.LoomCanvas;
import dev.loomstudios.project.LoomLayer;
import dev.loomstudios.project.LoomProject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Compact reference-oriented animation timeline.
 *
 * <p>It deliberately uses drawn controls rather than nesting dozens of
 * child widgets so the whole dock can stay responsive at GUI scale 3.</p>
 */
public final class LoomAnimationTimelineWidget extends AbstractWidget {
    private static final int HEADER_PRIMARY = 22;
    private static final int HEADER_SECONDARY = 20;
    private static final int RULER_HEIGHT = 12;
    private static final int ROW_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 22;

    public interface Controller {
        void togglePlayback();

        void toggleTimelineLoop();

        void changeDuration(int deltaTicks);

        void changePlaybackSpeed(float delta);

        void scrubTo(int tick);

        void addTrack();

        void selectTrack(UUID trackId);

        void toggleTrack(UUID trackId);

        void cycleEffect(UUID trackId);

        void addKeyframe(UUID trackId);

        void removeKeyframe(UUID trackId);

        void adjustKeyframeValue(UUID trackId, float delta);

        void cycleTrackSpeed(UUID trackId);

        void deleteTrack(UUID trackId);
        default void beginKeyframeDrag(){dev.loomstudios.client.project.ClientProjectWorkspace.beginCompoundEdit();}
        default void moveKeyframe(UUID id,int from,int to){
            dev.loomstudios.client.project.ClientProjectWorkspace.apply(p->{AnimationTrack track=p.animation().tracks().stream().filter(t->t.id().equals(id)).findFirst().orElseThrow();return p.withAnimation(dev.loomstudios.project.AnimationAuthoring.replaceTrack(p.animation(),dev.loomstudios.project.AnimationAuthoring.moveKeyframe(track,from,to)));});
        }
        default void endKeyframeDrag(){dev.loomstudios.client.project.ClientProjectWorkspace.endCompoundEdit();}
    }

    private final Supplier<LoomProject> projectSupplier;
    private final AnimationChannel channel;
    private final Supplier<UUID> selectedTrackSupplier;
    private final IntSupplier timelineTickSupplier;
    private final BooleanSupplier playingSupplier;
    private final Controller controller;
    private final boolean inlineTrackControls;

    private int scrollRows;
    private boolean draggingScrollbar;
    private UUID draggedTrack;
    private int draggedTick;

    public LoomAnimationTimelineWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            AnimationChannel channel,
            Supplier<UUID> selectedTrackSupplier,
            IntSupplier timelineTickSupplier,
            BooleanSupplier playingSupplier,
            Controller controller
    ) {
        this(
                x,
                y,
                width,
                height,
                projectSupplier,
                channel,
                selectedTrackSupplier,
                timelineTickSupplier,
                playingSupplier,
                controller,
                true
        );
    }

    public LoomAnimationTimelineWidget(
            int x,
            int y,
            int width,
            int height,
            Supplier<LoomProject> projectSupplier,
            AnimationChannel channel,
            Supplier<UUID> selectedTrackSupplier,
            IntSupplier timelineTickSupplier,
            BooleanSupplier playingSupplier,
            Controller controller,
            boolean inlineTrackControls
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal("Animation Timeline")
        );
        this.projectSupplier = Objects.requireNonNull(
                projectSupplier,
                "projectSupplier"
        );
        this.channel = Objects.requireNonNull(channel, "channel");
        this.selectedTrackSupplier = Objects.requireNonNull(
                selectedTrackSupplier,
                "selectedTrackSupplier"
        );
        this.timelineTickSupplier = Objects.requireNonNull(
                timelineTickSupplier,
                "timelineTickSupplier"
        );
        this.playingSupplier = Objects.requireNonNull(
                playingSupplier,
                "playingSupplier"
        );
        this.controller = Objects.requireNonNull(
                controller,
                "controller"
        );
        this.inlineTrackControls = inlineTrackControls;
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        LoomProject project = projectSupplier.get();
        LoomAnimation animation = project.animation();
        List<AnimationTrack> tracks = filteredTracks(animation);
        clampScroll(tracks.size());

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
                LoomUiTheme.PANEL_INNER
        );

        graphics.fill(getX() + 1, getY() + 1, getRight() - 1, getY() + 2, LoomUiTheme.ACCENT_ALT);
        renderPrimaryHeader(graphics, animation);
        if (!compactTimeline()) {
            renderSecondaryHeader(graphics, animation);
        }
        renderRuler(graphics, animation);

        int rowTop = rowTop();
        int visibleRows = visibleRows();

        if (tracks.isEmpty()) {
            graphics.drawString(Minecraft.getInstance().font, Component.literal("Select a layer, then + Track"),
                    getX() + 8, rowTop + 8, LoomUiTheme.TEXT_MUTED, false);
        } else {
            UUID selected = selectedTrackSupplier.get();

            for (int row = 0; row < visibleRows; row++) {
                int index = scrollRows + row;
                if (index >= tracks.size()) {
                    break;
                }

                AnimationTrack track = tracks.get(index);
                renderTrackRow(
                        graphics,
                        project,
                        animation,
                        track,
                        rowTop + row * rowHeight(),
                        track.id().equals(selected)
                );
            }
        }

        if (!compactTimeline() && inlineTrackControls) {
            renderFooter(graphics, project, animation, tracks);
        }

        if (tracks.size() > visibleRows) {
            renderScrollbar(graphics, tracks.size(), visibleRows);
        }
    }

    private void renderPrimaryHeader(
            GuiGraphics graphics,
            LoomAnimation animation
    ) {
        int y = getY() + (compactTimeline() ? 2 : 4);

        int controlHeight = compactTimeline() ? 14 : 15;
        drawControl(
                graphics,
                getX() + 6,
                y,
                20,
                controlHeight,
                playingSupplier.getAsBoolean() ? "II" : ">",
                true
        );
        drawControl(
                graphics,
                getX() + 30,
                y,
                34,
                controlHeight,
                animation.loop() ? "Loop" : "Once",
                animation.loop()
        );

        String time = formatTime(timelineTickSupplier.getAsInt())
                + " / "
                + formatTime(animation.durationTicks());
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(time),
                getX() + 72,
                y + 4,
                LoomUiTheme.TEXT_MUTED,
                false
        );

        if (compactTimeline()) {
            drawControl(graphics, getX() + 160, y, 18, controlHeight, "-", true);
            drawControl(graphics, getX() + 180, y, 18, controlHeight, "+", true);
        }
        int addWidth = Math.min(62, Math.max(48, getWidth() / 5));
        drawControl(
                graphics,
                getRight() - addWidth - 6,
                y,
                addWidth,
                controlHeight,
                "+ Track",
                animation.tracks().size()
                        < LoomAnimation.MAX_TRACKS
        );
    }

    private void renderSecondaryHeader(
            GuiGraphics graphics,
            LoomAnimation animation
    ) {
        int y = getY() + HEADER_PRIMARY + 1;
        int available = getWidth() - 12;
        int half = Math.max(70, (available - 5) / 2);

        renderStepper(
                graphics,
                getX() + 6,
                y,
                half,
                "Duration",
                formatTime(animation.durationTicks())
        );

        renderStepper(
                graphics,
                getX() + 6 + half + 5,
                y,
                Math.max(
                        70,
                        getRight() - 6 - (getX() + 6 + half + 5)
                ),
                "Playback",
                formatSpeed(animation.playbackSpeed())
        );
    }

    private void renderStepper(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            String label,
            String value
    ) {
        int side = 18;
        drawControl(graphics, x, y, side, 15, "-", true);
        drawControl(
                graphics,
                x + side + 3,
                y,
                Math.max(30, width - side * 2 - 6),
                15,
                label + " " + value,
                false
        );
        drawControl(
                graphics,
                x + width - side,
                y,
                side,
                15,
                "+",
                true
        );
    }

    private void renderRuler(
            GuiGraphics graphics,
            LoomAnimation animation
    ) {
        int top = getY()
                + primaryHeight()
                + secondaryHeight()
                + (compactTimeline() ? 1 : 3);
        int timelineLeft = timelineLeft();
        int timelineRight = getRight() - 6;
        int width = Math.max(1, timelineRight - timelineLeft);

        graphics.fill(
                timelineLeft,
                top + rulerHeight() - 2,
                timelineRight,
                top + rulerHeight() - 1,
                0xFF31404D
        );

        int[] fractions = {0, 1, 2, 3, 4};
        for (int fraction : fractions) {
            int x = timelineLeft
                    + width * fraction / 4;
            graphics.fill(
                    x,
                    top + rulerHeight() - 5,
                    x + 1,
                    top + rulerHeight(),
                    LoomUiTheme.TEXT_MUTED
            );

            int tick = animation.durationTicks()
                    * fraction / 4;
            String text = formatShortTime(tick);
            int textWidth = Minecraft.getInstance().font.width(text);
            int textX = Math.max(
                    getX() + 3,
                    Math.min(
                            getRight() - textWidth - 3,
                            x - textWidth / 2
                    )
            );
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(text),
                    textX,
                    top,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
        }
    }

    private void renderTrackRow(
            GuiGraphics graphics,
            LoomProject project,
            LoomAnimation animation,
            AnimationTrack track,
            int y,
            boolean selected
    ) {
        int left = getX() + 4;
        int right = getRight() - 4;
        int rowHeight = rowHeight();
        int background = selected
                ? 0xFF203846
                : 0xFF111B24;

        graphics.fill(left, y, right, y + rowHeight - 1, background);

        if (selected) {
            graphics.fill(
                    left,
                    y,
                    left + 2,
                    y + rowHeight - 1,
                    LoomUiTheme.ACCENT
            );
        }

        int eyeColor = track.enabled()
                ? LoomUiTheme.ACCENT
                : 0xFF45515A;
        graphics.fill(
                left + 5,
                y + 8,
                left + 12,
                y + 10,
                eyeColor
        );
        graphics.fill(
                left + 7,
                y + 6,
                left + 10,
                y + 12,
                eyeColor
        );

        LoomLayer layer = findLayer(project, track.layerId());
        String label = (layer == null ? "Missing Layer" : layer.name())
                + " • "
                + track.effect().displayName();

        String clipped = Minecraft.getInstance().font.plainSubstrByWidth(
                label,
                Math.max(20, timelineLeft() - (left + 18) - 6)
        );

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(clipped),
                left + 18,
                y + 6,
                selected ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );

        int timelineLeft = timelineLeft();
        int timelineRight = getRight() - 6;
        int timelineWidth = Math.max(1, timelineRight - timelineLeft);
        int centerY = y + rowHeight / 2;

        graphics.fill(
                timelineLeft,
                centerY - 2,
                timelineRight,
                centerY + 3,
                selected ? 0xFF4D2A73 : 0xFF263548
        );

        for (AnimationKeyframe keyframe : track.keyframes()) {
            int x = tickToX(
                    keyframe.tick(),
                    animation.durationTicks(),
                    timelineLeft,
                    timelineWidth
            );
            int color = selected
                    ? LoomUiTheme.ACCENT_ALT
                    : LoomUiTheme.ACCENT;
            graphics.fill(x - 2, centerY - 3, x + 3, centerY + 4, color);
            graphics.fill(x - 3, centerY - 1, x + 4, centerY + 2, color);
        }

        int currentX = tickToX(
                timelineTickSupplier.getAsInt(),
                animation.durationTicks(),
                timelineLeft,
                timelineWidth
        );
        graphics.fill(
                currentX,
                y + 2,
                currentX + 1,
                y + rowHeight - 3,
                LoomUiTheme.TEXT
        );
    }

    private void renderFooter(
            GuiGraphics graphics,
            LoomProject project,
            LoomAnimation animation,
            List<AnimationTrack> tracks
    ) {
        int y = getBottom() - footerHeight() + 3;
        UUID selectedId = selectedTrackSupplier.get();
        AnimationTrack selected = findTrack(tracks, selectedId);

        if (!inlineTrackControls) {
            String message = selected == null
                    ? "Select a track to edit it in the Animation panel"
                    : selected.effect().displayName()
                            + " • edit effect, speed and keyframes on the right";
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(
                            Minecraft.getInstance().font.plainSubstrByWidth(
                                    message,
                                    Math.max(40, getWidth() - 14)
                            )
                    ),
                    getX() + 7,
                    y + 4,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
            return;
        }

        if (selected == null) {
            graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(
                            "Select a track to edit its effect and keyframes"
                    ),
                    getX() + 7,
                    y + 4,
                    LoomUiTheme.TEXT_MUTED,
                    false
            );
            return;
        }

        int x = getX() + 6;
        int available = getWidth() - 12;
        int effectWidth = Math.max(
                66,
                Math.min(92, available / 4)
        );

        drawControl(
                graphics,
                x,
                y,
                effectWidth,
                16,
                selected.effect().displayName(),
                true
        );
        x += effectWidth + 3;

        String[] labels = {
                "+Key",
                "-Key",
                "Val-",
                "Val+",
                formatSpeed(selected.speed()),
                "Del"
        };

        int remaining = Math.max(1, getRight() - 6 - x);
        int gap = 3;
        int width = Math.max(
                28,
                (remaining - gap * (labels.length - 1))
                        / labels.length
        );

        for (int i = 0; i < labels.length; i++) {
            int controlWidth = i == labels.length - 1
                    ? Math.max(28, getRight() - 6 - x)
                    : width;

            drawControl(
                    graphics,
                    x,
                    y,
                    controlWidth,
                    16,
                    labels[i],
                    true
            );
            x += controlWidth + gap;
        }
    }

    private void renderScrollbar(
            GuiGraphics graphics,
            int trackCount,
            int visibleRows
    ) {
        int top = rowTop();
        int bottom = getBottom() - footerHeight() - 2;
        int height = Math.max(1, bottom - top);
        int thumbHeight = Math.max(
                10,
                height * visibleRows / trackCount
        );
        int maxScroll = trackCount - visibleRows;
        int thumbTop = top
                + (int)((height - thumbHeight)
                * (scrollRows / (double)Math.max(1, maxScroll)));

        graphics.fill(
                getRight() - 3,
                top,
                getRight() - 1,
                bottom,
                0xFF1B252E
        );
        graphics.fill(
                getRight() - 4,
                thumbTop,
                getRight(),
                thumbTop + thumbHeight,
                LoomUiTheme.TEXT_MUTED
        );
    }

    private static void drawControl(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            String text,
            boolean active
    ) {
        int border = active
                ? LoomUiTheme.BORDER
                : 0xFF26313A;
        int fill = active
                ? LoomUiTheme.BUTTON
                : LoomUiTheme.BUTTON_DISABLED;

        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(
                x + 1,
                y + 1,
                x + width - 1,
                y + height - 1,
                fill
        );

        int textWidth = Minecraft.getInstance().font.width(text);
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(text),
                x + Math.max(2, (width - textWidth) / 2),
                y + 4,
                active ? LoomUiTheme.TEXT : LoomUiTheme.TEXT_MUTED,
                false
        );
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        LoomProject project = projectSupplier.get();
        LoomAnimation animation = project.animation();

        if (mouseY < getY() + primaryHeight()) {
            if (compactTimeline() && mouseX >= getX() + 160 && mouseX < getX() + 198) {
                controller.changeDuration(mouseX < getX() + 179 ? -20 : 20);
                return;
            }
            if (mouseX >= getX() + 6 && mouseX < getX() + 26) {
                controller.togglePlayback();
                return;
            }
            if (mouseX >= getX() + 30 && mouseX < getX() + 64) {
                controller.toggleTimelineLoop();
                return;
            }
            if (mouseX >= getRight() - Math.min(
                    62,
                    Math.max(48, getWidth() / 5)
            ) - 6) {
                controller.addTrack();
                return;
            }
        }

        if (!compactTimeline()
                && mouseY >= getY() + primaryHeight()
                && mouseY < getY()
                + primaryHeight()
                + secondaryHeight()) {
            handleSecondaryHeaderClick(mouseX);
            return;
        }

        List<AnimationTrack> tracks = filteredTracks(animation);
        int rowTop = rowTop();
        int footerTop = getBottom() - footerHeight();

        if (mouseY >= rowTop && mouseY < footerTop && mouseX >= getRight() - 7
                && tracks.size() > visibleRows()) {
            draggingScrollbar = true;
            scrollTo(mouseY);
            return;
        }
        if (mouseY >= rowTop && mouseY < footerTop) {
            int row = (int)((mouseY - rowTop) / rowHeight());
            int index = scrollRows + row;

            if (index >= 0 && index < tracks.size()) {
                AnimationTrack track = tracks.get(index);

                if (mouseX < getX() + 18) {
                    controller.toggleTrack(track.id());
                    return;
                }

                controller.selectTrack(track.id());

                if (mouseX >= timelineLeft()) {
                    for(AnimationKeyframe key:track.keyframes()){
                        int x=tickToX(key.tick(),animation.durationTicks(),timelineLeft(),getRight()-6-timelineLeft());
                        if(Math.abs(mouseX-x)<=5){draggedTrack=track.id();draggedTick=key.tick();controller.beginKeyframeDrag();break;}
                    }
                    controller.scrubTo(
                            xToTick(
                                    mouseX,
                                    animation.durationTicks()
                            )
                    );
                }
            }
            return;
        }

        if (!compactTimeline()
                && mouseY >= footerTop
                && inlineTrackControls) {
            handleFooterClick(
                    mouseX,
                    tracks,
                    selectedTrackSupplier.get()
            );
        }
    }

    private void handleSecondaryHeaderClick(double mouseX) {
        int available = getWidth() - 12;
        int half = Math.max(70, (available - 5) / 2);
        int firstX = getX() + 6;
        int secondX = firstX + half + 5;
        int side = 18;

        if (mouseX >= firstX && mouseX < firstX + side) {
            controller.changeDuration(-20);
            return;
        }
        if (mouseX >= firstX + half - side
                && mouseX < firstX + half) {
            controller.changeDuration(20);
            return;
        }
        if (mouseX >= secondX && mouseX < secondX + side) {
            controller.changePlaybackSpeed(-0.25F);
            return;
        }
        if (mouseX >= getRight() - 6 - side
                && mouseX < getRight() - 6) {
            controller.changePlaybackSpeed(0.25F);
        }
    }

    private void handleFooterClick(
            double mouseX,
            List<AnimationTrack> tracks,
            UUID selectedId
    ) {
        AnimationTrack selected = findTrack(tracks, selectedId);
        if (selected == null) {
            return;
        }

        int x = getX() + 6;
        int available = getWidth() - 12;
        int effectWidth = Math.max(
                66,
                Math.min(92, available / 4)
        );

        if (mouseX >= x && mouseX < x + effectWidth) {
            controller.cycleEffect(selected.id());
            return;
        }
        x += effectWidth + 3;

        int controlCount = 6;
        int remaining = Math.max(1, getRight() - 6 - x);
        int gap = 3;
        int width = Math.max(
                28,
                (remaining - gap * (controlCount - 1))
                        / controlCount
        );

        for (int i = 0; i < controlCount; i++) {
            int controlWidth = i == controlCount - 1
                    ? Math.max(28, getRight() - 6 - x)
                    : width;

            if (mouseX >= x && mouseX < x + controlWidth) {
                switch (i) {
                    case 0 -> controller.addKeyframe(selected.id());
                    case 1 -> controller.removeKeyframe(selected.id());
                    case 2 -> controller.adjustKeyframeValue(
                            selected.id(),
                            -0.1F
                    );
                    case 3 -> controller.adjustKeyframeValue(
                            selected.id(),
                            0.1F
                    );
                    case 4 -> controller.cycleTrackSpeed(selected.id());
                    case 5 -> controller.deleteTrack(selected.id());
                    default -> {
                    }
                }
                return;
            }

            x += controlWidth + gap;
        }
    }

    private void scrollTo(double y) {
        int count = filteredTracks(projectSupplier.get().animation()).size();
        int visible = visibleRows();
        int height = Math.max(1, getBottom() - footerHeight() - 2 - rowTop());
        int thumb = Math.max(10, height * visible / Math.max(1, count));
        double fraction = (y - rowTop() - thumb / 2.0) / Math.max(1, height - thumb);
        scrollRows = (int)Math.round(Math.max(0, Math.min(1, fraction)) * Math.max(0, count - visible));
    }
    @Override protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar) scrollTo(event.y());
        else if(draggedTrack!=null){int next=xToTick(event.x(),projectSupplier.get().animation().durationTicks());if(next!=draggedTick){controller.moveKeyframe(draggedTrack,draggedTick,next);draggedTick=next;controller.scrubTo(next);}}
    }
    @Override public void onRelease(MouseButtonEvent event) { draggingScrollbar = false;if(draggedTrack!=null){draggedTrack=null;controller.endKeyframeDrag();} }
    public void closeGesture(){if(draggedTrack!=null){draggedTrack=null;controller.endKeyframeDrag();}}

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!isMouseOver(mouseX, mouseY) || scrollY == 0.0) {
            return false;
        }

        int count = filteredTracks(
                projectSupplier.get().animation()
        ).size();
        int max = Math.max(0, count - visibleRows());

        scrollRows = Math.max(
                0,
                Math.min(
                        max,
                        scrollRows + (scrollY < 0 ? 1 : -1)
                )
        );
        return true;
    }

    private List<AnimationTrack> filteredTracks(
            LoomAnimation animation
    ) {
        return animation.tracks().stream()
                .filter(track -> track.channel() == channel)
                .toList();
    }

    private LoomLayer findLayer(
            LoomProject project,
            UUID layerId
    ) {
        LoomCanvas canvas = channel == AnimationChannel.CAPE
                ? project.cape()
                : project.elytra();

        return canvas.layers().stream()
                .filter(layer -> layer.id().equals(layerId))
                .findFirst()
                .orElse(null);
    }

    private static AnimationTrack findTrack(
            List<AnimationTrack> tracks,
            UUID id
    ) {
        if (id == null) {
            return null;
        }

        return tracks.stream()
                .filter(track -> track.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private boolean compactTimeline() {
        return getHeight() < 112;
    }

    private int primaryHeight() {
        return compactTimeline() ? 18 : HEADER_PRIMARY;
    }

    private int secondaryHeight() {
        return compactTimeline() ? 0 : HEADER_SECONDARY;
    }

    private int rulerHeight() {
        return compactTimeline() ? 10 : RULER_HEIGHT;
    }

    private int footerHeight() {
        return compactTimeline() || !inlineTrackControls ? 0 : FOOTER_HEIGHT;
    }

    private int rowHeight() {
        return compactTimeline() ? 18 : ROW_HEIGHT;
    }

    private int rowTop() {
        return getY()
                + primaryHeight()
                + secondaryHeight()
                + rulerHeight()
                + (compactTimeline() ? 2 : 5);
    }

    private int visibleRows() {
        int available = getHeight()
                - primaryHeight()
                - secondaryHeight()
                - rulerHeight()
                - footerHeight()
                - (compactTimeline() ? 3 : 8);
        return Math.max(1, available / rowHeight());
    }

    private int timelineLeft() {
        return getX()
                + Math.max(
                        84,
                        Math.min(132, getWidth() / 3)
                );
    }

    private void clampScroll(int trackCount) {
        int max = Math.max(0, trackCount - visibleRows());
        scrollRows = Math.max(0, Math.min(max, scrollRows));
    }

    private int xToTick(
            double mouseX,
            int duration
    ) {
        int left = timelineLeft();
        int width = Math.max(1, getRight() - 6 - left);
        double ratio = Math.max(
                0.0,
                Math.min(1.0, (mouseX - left) / width)
        );
        return (int)Math.round(ratio * duration);
    }

    private static int tickToX(
            int tick,
            int duration,
            int left,
            int width
    ) {
        if (duration <= 0) {
            return left;
        }

        double ratio = Math.max(
                0.0,
                Math.min(1.0, tick / (double)duration)
        );
        return left + (int)Math.round(ratio * width);
    }

    private static String formatTime(int tick) {
        double seconds = Math.max(0, tick) / 20.0;
        int whole = (int)Math.floor(seconds);
        int minutes = whole / 60;
        int remainder = whole % 60;
        int tenths = (int)Math.floor(
                (seconds - whole) * 10.0
        );

        return String.format(
                java.util.Locale.ROOT,
                "%d:%02d.%d",
                minutes,
                remainder,
                tenths
        );
    }

    private static String formatShortTime(int tick) {
        double seconds = Math.max(0, tick) / 20.0;
        return String.format(
                java.util.Locale.ROOT,
                "%.1fs",
                seconds
        );
    }

    private static String formatSpeed(float speed) {
        return String.format(
                java.util.Locale.ROOT,
                "%.2gx",
                speed
        );
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
