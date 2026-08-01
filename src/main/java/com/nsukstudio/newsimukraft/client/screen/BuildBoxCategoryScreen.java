package com.nsukstudio.newsimukraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 建筑分类选择界面 —— 对应 1.21.1 createSelectBuildingUi
 *
 * 分类按钮网格（对应 gui.category.*）：
 *   住宅 | 商业 | 工业 | 公共 | 其他
 */
public class BuildBoxCategoryScreen extends Screen {

    private static final int CATEGORY_BUTTON_WIDTH  = 110;
    private static final int CATEGORY_BUTTON_HEIGHT = 20;
    private static final int CATEGORY_BUTTON_GAP    = 4;

    @SuppressWarnings("unused")
    private final BlockPos buildBoxPos;
    private final Screen parent;

    public BuildBoxCategoryScreen(BlockPos buildBoxPos, Screen parent) {
        super(Component.translatable("gui.select_building.title"));
        this.buildBoxPos = buildBoxPos;
        this.parent     = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;

        int totalWidth = CATEGORY_BUTTON_WIDTH * 5 + CATEGORY_BUTTON_GAP * 4;
        int startX = cx - totalWidth / 2;
        int startY = cy + 10;

        String[] keys = {
            "gui.category.residential",
            "gui.category.commercial",
            "gui.category.industrial",
            "gui.category.public",
            "gui.category.other"
        };

        for (int i = 0; i < keys.length; i++) {
            int x = startX + i * (CATEGORY_BUTTON_WIDTH + CATEGORY_BUTTON_GAP);
            addRenderableWidget(Button.builder(
                    Component.translatable(keys[i]),
                    btn -> { if (minecraft != null) minecraft.gui.setScreen(parent); }
            ).bounds(x, startY, CATEGORY_BUTTON_WIDTH, CATEGORY_BUTTON_HEIGHT).build());
        }

        addRenderableWidget(Button.builder(
                Component.translatable("gui.button.back"),
                btn -> { if (minecraft != null) minecraft.gui.setScreen(parent); }
        ).bounds(cx - 50, cy + 10 + CATEGORY_BUTTON_HEIGHT + 12, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int cx = width / 2;
        int cy = height / 2;

        graphics.centeredText(font,
                Component.translatable("gui.select_building.title"),
                cx, cy - 40, 0xFFFFFFFF);
        graphics.centeredText(font,
                Component.translatable("gui.select_building.instruction"),
                cx, cy - 22, 0xFFAAAAAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

