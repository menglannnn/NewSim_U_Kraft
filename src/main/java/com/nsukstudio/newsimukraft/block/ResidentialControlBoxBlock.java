package com.nsukstudio.newsimukraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * New-Sim-U 控制盒方块
 *
 * 所属系统：建筑
 * 特性：
 *   - 六面贴图相同（residential_control_box.png）
 *   - 硬度/阻力继承橡木木板，斧头最快破坏
 *   - 右击打开建筑分类管理界面
 *   - 管理五大建筑分类：商业、工业、住宅、公共、其他
 */
public class ResidentialControlBoxBlock extends Block {

    public ResidentialControlBoxBlock(Properties props) {
        super(props);
    }

    /** 右击打开建筑控制盒管理界面 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            mc.gui.setScreen(new com.nsukstudio.newsimukraft.client.screen.BuildBoxCategoryScreen(pos, null));
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.SUCCESS_SERVER;
        }
    }
}
