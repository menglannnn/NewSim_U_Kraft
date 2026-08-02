package com.nsukstudio.newsimukraft.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * 建筑盒主界面
 *
 * 布局对应 1.20.1 BuildBoxScreen，按钮尺寸自适应屏幕宽度：
 *   完成按钮：左上角 (5, 5, 45×20)
 *   第1行：雇佣建筑师 | 选择建筑 | 解雇员工
 *   第2行：雇佣规划师 | 规划区域 | 员工信息
 *   半透明黑底：0xC8000000
 */
public class BuildBoxScreen extends Screen {

    private static final int BTN_H   = 20;
    private static final int BTN_GAP = 2;
    private static final int MAX_BTN_W = 120;

    @SuppressWarnings("Null")
    private final BlockPos buildBoxPos;

    public BuildBoxScreen(BlockPos buildBoxPos) {
        super(Component.translatable("gui.build_box.title"));
        this.buildBoxPos = buildBoxPos;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        // 按钮宽度自适应：最大120，但确保3列不溢出屏幕（左右各留5px）
        int btnW = Math.min(MAX_BTN_W, (width - 10 - BTN_GAP * 2) / 3);
        int rowW = btnW * 3 + BTN_GAP * 2;
        int col0 = cx - rowW / 2;
        int col1 = col0 + btnW + BTN_GAP;
        int col2 = col1 + btnW + BTN_GAP;

        // 行Y坐标相对屏幕高度居中
        int row1Y = height / 2;
        int row2Y = row1Y + BTN_H + 4;

        // 完成按钮（左上角）
        addRenderableWidget(Button.builder(
                Component.translatable("gui.button.done"),
                btn -> { if (minecraft != null) minecraft.gui.setScreen(null); }
        ).bounds(5, 5, 45, 20).build());

        // 第1行
        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.hire_builder"),
                btn -> {}
        ).bounds(col0, row1Y, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.select_building"),
                btn -> handleSelectBuilding()
        ).bounds(col1, row1Y, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.fire_employee"),
                btn -> {}
        ).bounds(col2, row1Y, btnW, BTN_H).build());

        // 第2行
        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.hire_planner"),
                btn -> {}
        ).bounds(col0, row2Y, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.plan_area"),
                btn -> {}
        ).bounds(col1, row2Y, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.build_box.employee_info"),
                btn -> {}
        ).bounds(col2, row2Y, btnW, BTN_H).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 半透明黑色全屏背景
        graphics.fillGradient(0, 0, width, height, 0xC8000000, 0xC8000000);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        int cx = width / 2;
        graphics.centeredText(font, Component.translatable("gui.build_box.title"),       cx, height / 4,       0xFFFFFF);
        graphics.centeredText(font, Component.translatable("gui.build_box.instruction"), cx, height / 2 - 20,  0xFFFFAA);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void handleSelectBuilding() {
        if (minecraft == null) return;
        minecraft.gui.setScreen(new BuildBoxCategoryScreen(buildBoxPos, this));
    }
}


