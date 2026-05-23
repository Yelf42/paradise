package com.yelf42.paradise.client.gui.screens;

import com.yelf42.paradise.Paradise;
import com.yelf42.paradise.registry.ModPackets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WhitelistScreen extends Screen {

    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private static final ResourceLocation BACKGROUND_SPRITE = Paradise.identifier("textures/gui/whitelist_background.png");

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 240;

    private static final int PADDING = 12;
    private static final int SIDE_PADDING = 32;
    private static final int ROW_HEIGHT = 12;
    private static final int SCROLLBAR_WIDTH = 17;
    private static final int SCROLLBAR_HEIGHT = 21;
    private static final int COLUMN_GAP = 24;
    private static final int HEADER_HEIGHT = 32;

    private final ResourceLocation dimensionId;
    private final BlockPos pos;

    // Data
    private final List<String> activeList = new ArrayList<>();
    private final List<String> historyList = new ArrayList<>();

    // Selection
    private boolean selectedInActive = false;
    private int selectedIndex = -1;

    // Scroll
    private float scrollOffset = 0f;
    private boolean isScrolling = false;

    // Widgets
    private EditBox nameInput;
    private Button addButton;
    private Button moveButton;
    private Button removeButton;

    // Layout (computed in init, all relative to guiLeft/guiTop)
    private int guiLeft;
    private int guiTop;
    private int listTop;
    private int listBottom;
    private int colWidth;
    private int activeColX;
    private int historyColX;
    private int midX;
    private int scrollbarX;
    private int scrollbarTop;
    private int scrollbarBottom;

    public WhitelistScreen(ResourceLocation dimensionId, Map<String, Long> active, Set<String> history, BlockPos pos) {
        super(Component.translatable("gui.paradise.whitelist.title"));
        this.dimensionId = dimensionId;
        this.activeList.addAll(active.keySet());
        this.historyList.addAll(history);
        this.pos = pos;
    }

    @Override
    protected void init() {
        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;

        listTop = guiTop + HEADER_HEIGHT + PADDING + PADDING;
        listBottom = guiTop + GUI_HEIGHT - PADDING - 4;

        // Scrollbar on the far right
        scrollbarX = guiLeft + GUI_WIDTH - SIDE_PADDING - SCROLLBAR_WIDTH;
        scrollbarTop = listTop;
        scrollbarBottom = listBottom;

        // Two columns between side padding and scrollbar, with a center gap for buttons
        int availableWidth = scrollbarX - PADDING - (guiLeft + SIDE_PADDING) - COLUMN_GAP - PADDING * 2;
        colWidth = availableWidth / 2;
        activeColX = guiLeft + SIDE_PADDING;
        historyColX = activeColX + colWidth + PADDING + COLUMN_GAP + PADDING;
        midX = activeColX + colWidth + PADDING + COLUMN_GAP / 2;

        // Add button anchored to right edge of GUI, above history column
        int addButtonX = scrollbarX - 2;
        int inputY = guiTop + PADDING + PADDING / 2;
        int inputX = historyColX - 2;
        int inputWidth = addButtonX - 2 - inputX;

        nameInput = new EditBox(this.font, inputX, inputY, inputWidth, 16,
                Component.translatable("gui.paradise.whitelist.input"));
        nameInput.setMaxLength(16);
        this.addWidget(nameInput);

        addButton = Button.builder(
                Component.literal("+"),
                btn -> onAddClicked()
        ).bounds(addButtonX, inputY, SCROLLBAR_WIDTH + 4, 16).build();
        this.addRenderableWidget(addButton);

        // Middle buttons — vertically centered within the list area
        int btnY = listTop + (GUI_HEIGHT - HEADER_HEIGHT - PADDING * 2) / 2 - 56;
        moveButton = Button.builder(
                Component.literal("<>"),
                btn -> onMoveClicked()
        ).bounds(midX - COLUMN_GAP / 2, btnY, COLUMN_GAP, 16).build();
        this.addRenderableWidget(moveButton);

        removeButton = Button.builder(
                Component.literal("X"),
                btn -> onRemoveClicked()
        ).bounds(midX - COLUMN_GAP / 2, btnY + 25, COLUMN_GAP, 16).build();
        this.addRenderableWidget(removeButton);

        updateButtonStates();
    }

    private void onAddClicked() {
        String name = nameInput.getValue().replaceAll("[^a-zA-Z0-9_]+", "");
        name = name.substring(0, Math.min(name.length(), 16));
        if (name.length() < 3) return;
        if (!activeList.contains(name) && !historyList.contains(name)) {
            activeList.add(name);
            sendMutate(name, ModPackets.MutateWhitelistPayload.Action.ADD);
            nameInput.setValue("");
        }
    }

    // TODO should switch selected to new name position
    private void onMoveClicked() {
        if (selectedIndex < 0) return;
        String name;
        if (selectedInActive) {
            name = activeList.remove(selectedIndex);
            historyList.add(name);
        } else {
            name = historyList.remove(selectedIndex);
            activeList.add(name);
        }
        if (name.isEmpty()) return;
        sendMutate(name, ModPackets.MutateWhitelistPayload.Action.FLIP);
        selectedIndex = -1;
        updateButtonStates();
    }

    private void onRemoveClicked() {
        if (selectedIndex < 0) return;
        String name;
        if (selectedInActive) {
            name = activeList.remove(selectedIndex);
        } else {
            name = historyList.remove(selectedIndex);
        }
        if (name.isEmpty()) return;
        sendMutate(name, ModPackets.MutateWhitelistPayload.Action.REMOVE);
        selectedIndex = -1;
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedIndex >= 0;
        moveButton.active = hasSelection;
        removeButton.active = hasSelection;
    }

    private void sendMutate(String name, ModPackets.MutateWhitelistPayload.Action action) {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(new ModPackets.MutateWhitelistPayload(this.dimensionId, name, action));
        clientPacketListener.send(packet);
    }

    // --- Scrolling ---

    private int maxRows() {
        return Math.max(activeList.size(), historyList.size());
    }

    private int visibleRows() {
        return (listBottom - listTop) / ROW_HEIGHT;
    }

    private int scrollableRows() {
        return Math.max(0, maxRows() - visibleRows());
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

        // Blocking shapes
//        graphics.fill(activeColX, listTop, activeColX + colWidth, listBottom, 0x44FF0000);
//        graphics.fill(historyColX, listTop, historyColX + colWidth, listBottom, 0x440000FF);
//        graphics.fill(scrollbarX, scrollbarTop, scrollbarX + SCROLLBAR_WIDTH, scrollbarBottom, 0x4400FF00);
//        graphics.fill(midX - COLUMN_GAP / 2, listTop, midX + COLUMN_GAP / 2, listBottom, 0x44FFFF00);

        // Title
        graphics.drawString(this.font,
                Component.translatable("gui.paradise.whitelist.title"),
                activeColX, guiTop + PADDING + PADDING / 2 + 4, 4210752,false);

        // Column headers
        graphics.drawString(this.font,
                Component.translatable("gui.paradise.whitelist.active"),
                activeColX, listTop - PADDING, 4210752, false);
        graphics.drawString(this.font,
                Component.translatable("gui.paradise.whitelist.history"),
                historyColX, listTop - PADDING, 4210752,false);

        int startRow = scrollRow();
        int rows = visibleRows();

        // Active column
        for (int i = 0; i < rows; i++) {
            int idx = startRow + i;
            if (idx >= activeList.size()) break;
            int y = listTop + i * ROW_HEIGHT;
            boolean selected = selectedInActive && selectedIndex == idx;
            if (selected) graphics.fill(activeColX, y - 1, activeColX + colWidth, y + ROW_HEIGHT - 1, 0x44FFFFFF);
            graphics.drawString(this.font, activeList.get(idx), activeColX + 2, y + 1, selected ? 0xFFFFFF : 0xDDDDDD);
        }

        // History column
        for (int i = 0; i < rows; i++) {
            int idx = startRow + i;
            if (idx >= historyList.size()) break;
            int y = listTop + i * ROW_HEIGHT;
            boolean selected = !selectedInActive && selectedIndex == idx;
            if (selected) graphics.fill(historyColX, y - 1, historyColX + colWidth, y + ROW_HEIGHT - 1, 0x44FFFFFF);
            graphics.drawString(this.font, historyList.get(idx), historyColX + 2, y + 1, selected ? 0xFFFFFF : 0xDDDDDD);
        }

        // Scrollbar
        if (scrollableRows() <= 0) {
            graphics.blitSprite(SCROLLER_DISABLED_SPRITE, scrollbarX, scrollbarTop, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
        } else {
            int handleY = scrollbarTop + (int) (scrollOffset * (scrollbarBottom - scrollbarTop - SCROLLBAR_HEIGHT));
            graphics.blitSprite(SCROLLER_SPRITE, scrollbarX, handleY, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
        }

        // Name input
        nameInput.render(graphics, mouseX, mouseY, partialTick);
    }

    // --- Input ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= scrollbarX && mouseX <= scrollbarX + SCROLLBAR_WIDTH
                && mouseY >= scrollbarTop && mouseY <= scrollbarBottom && scrollableRows() > 0) {
            isScrolling = true;
            return true;
        }

        if (mouseX >= activeColX && mouseX <= activeColX + colWidth
                && mouseY >= listTop && mouseY <= listBottom) {
            int row = scrollRow() + (int) ((mouseY - listTop) / ROW_HEIGHT);
            if (row < activeList.size()) {
                selectedInActive = true;
                selectedIndex = row;
                updateButtonStates();
                return true;
            }
        }

        if (mouseX >= historyColX && mouseX <= historyColX + colWidth
                && mouseY >= listTop && mouseY <= listBottom) {
            int row = scrollRow() + (int) ((mouseY - listTop) / ROW_HEIGHT);
            if (row < historyList.size()) {
                selectedInActive = false;
                selectedIndex = row;
                updateButtonStates();
                return true;
            }
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

    public void onClose() {
        this.onDone();
    }

    private void onDone() {
        this.minecraft.setScreen(null);
    }

    public void removed() {
        ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
        if (clientpacketlistener != null) {
            clientpacketlistener.send(new ServerboundCustomPayloadPacket(new ModPackets.CloseWhitelistPayload(this.pos)));
        }
    }

}
