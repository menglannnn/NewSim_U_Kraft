package com.nsukstudio.newsimukraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 建筑分类选择界面
 *
 * 布局完全复刻 1.20.1 SelectBuildingScreen：
 *   背景：半透明黑 0xC8000000
 *   标题：居中 y=17，白色 0xFFFFFF
 *   说明：居中 y=100，黄色 0xFFFFAA
 *   完成按钮：左上角 (5, 5, 45×20)
 *   第一行 y=150，4个 100×20：
 *     住宅 cx-200 | 商业 cx-100 | 工业 cx | 其他 cx+100
 *   第二行 y=180，公共居中：
 *     公共 cx-50
 *   返回按钮：cx-50, y=210，100×20
 */
public class BuildBoxCategoryScreen extends Screen {

    @SuppressWarnings("unused")
    private final BlockPos buildBoxPos;
    private final Screen parent;

    public BuildBoxCategoryScreen(BlockPos buildBoxPos, Screen parent) {
        super(Component.translatable("gui.select_building.title"));
        this.buildBoxPos = buildBoxPos;
        this.parent      = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        // 完成按钮（左上角），与建筑盒主界面保持一致
        addRenderableWidget(Button.builder(
                Component.translatable("gui.button.done"),
                btn -> { if (minecraft != null) minecraft.gui.setScreen(null); }
        ).bounds(5, 5, 45, 20).build());

        // 第一行 y=150，4个分类按钮，每个 100×20
        addRenderableWidget(Button.builder(
                Component.translatable("gui.category.residential"),
                btn -> {}
        ).bounds(cx - 200, 150, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.category.commercial"),
                btn -> {}
        ).bounds(cx - 100, 150, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.category.industrial"),
                btn -> {}
        ).bounds(cx, 150, 100, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.category.other"),
                btn -> {}
        ).bounds(cx + 100, 150, 100, 20).build());

        // 第二行 y=180，公共居中
        addRenderableWidget(Button.builder(
                Component.translatable("gui.category.public"),
                btn -> {}
        ).bounds(cx - 50, 180, 100, 20).build());

        // 返回按钮（回到上一界面）
        addRenderableWidget(Button.builder(
                Component.translatable("gui.button.back"),
                btn -> { if (minecraft != null) minecraft.gui.setScreen(parent); }
        ).bounds(cx - 50, 210, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 半透明黑色全屏背景，与建筑盒主界面一致
        graphics.fillGradient(0, 0, width, height, 0xC8000000, 0xC8000000);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int cx = width / 2;
        // 标题：白色，y=17
        graphics.centeredText(font, Component.translatable("gui.select_building.title"), cx, 17, 0xFFFFFF);
        // 说明：黄色，y=100
        graphics.centeredText(font, Component.translatable("gui.select_building.instruction"), cx, 100, 0xFFFFAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

