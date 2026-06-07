package com.yelf42.paradise.client.gui.screens;

import com.yelf42.paradise.Paradise;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class TransitLogScreen extends Screen {

    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private static final ResourceLocation BACKGROUND_SPRITE = Paradise.identifier("textures/gui/transit_log_background.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 240;

    private static final int PADDING = 12;
    private static final int SIDE_PADDING = 20;
    private static final int ROW_HEIGHT = 12;
    private static final int SCROLLBAR_WIDTH = 17;
    private static final int SCROLLBAR_HEIGHT = 21;
    private static final int HEADER_HEIGHT = 48;

    private final ResourceLocation dimensionId;
    private final List<String> entries = new ArrayList<>();

    // Scroll
    private float scrollOffset = 0f;
    private boolean isScrolling = false;

    // Layout (computed in init)
    private int guiLeft;
    private int guiTop;
    private int listTop;
    private int listBottom;
    private int listLeft;
    private int scrollbarX;
    private int scrollbarTop;
    private int scrollbarBottom;
    private int maxTextWidth;

    public TransitLogScreen(ResourceLocation dimensionId, List<String> transitLog) {
        super(Component.translatable("gui.paradise.transit_log.title"));
        this.dimensionId = dimensionId;
        this.entries.addAll(transitLog.reversed());
    }

    @Override
    protected void init() {
        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;

        listTop = guiTop + HEADER_HEIGHT + PADDING;
        listBottom = guiTop + GUI_HEIGHT - PADDING - 4;

        scrollbarX = guiLeft + GUI_WIDTH - SIDE_PADDING - SCROLLBAR_WIDTH;
        scrollbarTop = listTop;
        scrollbarBottom = listBottom - 9;

        listLeft = guiLeft + SIDE_PADDING;

        maxTextWidth = this.font.width("WWWWWWWWWWWWWWWWWW");
    }

    // --- Scrolling helpers ---

    private int visibleRows() {
        return (listBottom - listTop) / ROW_HEIGHT;
    }

    private int scrollableRows() {
        return Math.max(0, entries.size() - visibleRows());
    }

    private int scrollRow() {
        return (int) (scrollOffset * scrollableRows());
    }

    // --- Rendering ---

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(BACKGROUND_SPRITE, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, 512, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Title
        graphics.drawString(this.font,
                Component.translatable("gui.paradise.transit_log.title").withStyle(ChatFormatting.BOLD),
                listLeft, guiTop + PADDING + PADDING / 2 + 7, 4210752, false);

        // Dimension ID subtitle
        graphics.drawString(this.font,
                dimensionId.getPath().toUpperCase(),
                listLeft, guiTop + PADDING + PADDING / 2 + 7 + ROW_HEIGHT, 6710886, false);

        // Entries
        int startRow = scrollRow();
        int rows = visibleRows();
        for (int i = 0; i < rows; i++) {
            int idx = startRow + i;
            if (idx >= entries.size()) break;
            int y = listTop + i * ROW_HEIGHT;

            String[] data = entries.get(idx).split("\\$");
            if (data.length < 3) continue;

            graphics.drawString(this.font, data[0], listLeft, y + 1, 0xDDDDDD, false);
            graphics.drawString(this.font, data[1], listLeft + maxTextWidth, y + 1, 0xDDDDDD, false);
            graphics.drawString(this.font, data[2], listLeft + maxTextWidth + maxTextWidth, y + 1, 0xDDDDDD, false);
        }

        // Scrollbar
        if (scrollableRows() <= 0) {
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, scrollbarX, scrollbarTop, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
        } else {
            int handleY = scrollbarTop + (int) (scrollOffset * (scrollbarBottom - scrollbarTop - SCROLLBAR_HEIGHT));
            graphics.blitSprite(SCROLLER_SPRITE, scrollbarX, handleY, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
        }
    }

    // --- Input ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0
                && mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH
                && mouseY >= scrollbarTop && mouseY <= scrollbarBottom
                && scrollableRows() > 0) {
            isScrolling = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isScrolling) {
            float range = scrollbarBottom - scrollbarTop - SCROLLBAR_HEIGHT;
            if (range > 0) {
                scrollOffset = Mth.clamp((float) (mouseY - scrollbarTop) / range, 0f, 1f);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) isScrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollableRows() > 0) {
            scrollOffset = Mth.clamp(scrollOffset - (float) scrollY / scrollableRows(), 0f, 1f);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
