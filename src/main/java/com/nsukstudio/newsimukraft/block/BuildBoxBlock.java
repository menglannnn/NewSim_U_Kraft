package com.nsukstudio.newsimukraft.block;

import com.nsukstudio.newsimukraft.sound.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Sim-U 建筑盒方块
 *
 * 所属系统：建筑
 * 特性：
 *   - 六面贴图相同（build_box.png）
 *   - 放置时播放 build_box_place 音效
 *   - 破坏时播放 build_box_break 音效
 *   - 硬度/阻力继承橡木木板，斧头最快破坏
 */
public class BuildBoxBlock extends Block {

    public BuildBoxBlock(Properties props) {
        super(props);
    }

    /** 右击打开建筑盒控制界面，服务端广播打开音效 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            mc.gui.setScreen(new com.nsukstudio.newsimukraft.client.screen.BuildBoxScreen(pos));
            return InteractionResult.SUCCESS;
        } else {
            level.playSound(null, pos,
                    ModSoundEvents.BUILD_BOX_OPEN.get(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS_SERVER;
        }
    }

    /** 方块放置后播放自定义放置音效 —— 服务端调用 playSound 广播给所有客户端 */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide()) return; // 由服务端广播，客户端跳过避免重复
        level.playSound(null, pos,
                ModSoundEvents.BUILD_BOX_PLACE.get(),
                SoundSource.BLOCKS,
                1.0f, 1.0f);
    }

    /** 方块被破坏后播放自定义破坏音效 —— 服务端广播 */
    @Override
    public void destroy(net.minecraft.world.level.LevelAccessor level,
                        BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof Level worldLevel && !worldLevel.isClientSide()) {
            worldLevel.playSound(null, pos,
                    ModSoundEvents.BUILD_BOX_BREAK.get(),
                    SoundSource.BLOCKS,
                    1.0f, 0.9f);
        }
    }
}
