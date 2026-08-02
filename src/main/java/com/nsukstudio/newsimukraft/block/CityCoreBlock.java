package com.nsukstudio.newsimukraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * New-Sim-U 城市核心方块
 *
 * 所属系统：城市核心
 * 特性：
 *   - 六面贴图相同（city_core.png）
 *   - 硬度/阻力继承橡木木板，斧头最快破坏
 *   - 右击打开城市管理界面
 *   - 统一管理所有建筑 NPC 及城市
 */
public class CityCoreBlock extends Block {

    public CityCoreBlock(Properties props) {
        super(props);
    }

    /** 右击打开城市管理界面（客户端）；服务端返回 SUCCESS_SERVER */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.SUCCESS_SERVER;
        }
    }
}
