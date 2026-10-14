package io.github.natanfudge.nectest;

import io.github.natanfudge.nectest.mixin.MaintenanceLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.InstantNeighborUpdater;
import net.minecraft.world.level.redstone.Orientation;

/** Real neighbor recursion, initiated by a scheduled repeater tick (no player packet). */
public final class MaintenanceUpdateSuppression {
    public static void prepare(MinecraftServer server) {
        var level = server.overworld();
        var origin = server.getPlayerList().getPlayers().getFirst().blockPosition().offset(4, 8, 4);
        // AntiShadowPatch restores vanilla's pre-1.19 instant updater in exactly this way.
        // A test adapter is needed because AntiShadowPatch has no published 26.3 build.
        ((MaintenanceLevelAccessor) level).necTest$setNeighborUpdater(new InstantUpdates(level));
        var rail = Blocks.POWERED_RAIL.defaultBlockState().setValue(PoweredRailBlock.POWERED, true);
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 48; z++) {
                var pos = origin.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
                level.setBlock(pos, rail, Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
            }
        }
        var trigger = origin.west();
        level.setBlock(trigger.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
        level.setBlock(trigger, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.WEST)
                .setValue(RepeaterBlock.POWERED, true), Block.UPDATE_SKIP_ALL_SIDEEFFECTS);
        level.scheduleTick(trigger, Blocks.REPEATER, 1);
        System.out.println("NEC_TEST scheduled repeater beside 2304 powered rails at " + origin);
    }

    private static final class InstantUpdates extends CollectingNeighborUpdater {
        private final InstantNeighborUpdater instant;
        private InstantUpdates(Level level) { super(level, 0); instant = new InstantNeighborUpdater(level); }
        @Override public void shapeUpdate(Direction direction, BlockState state, BlockPos pos, BlockPos neighborPos, int flags, int limit) {
            instant.shapeUpdate(direction, state, pos, neighborPos, flags, limit);
        }
        @Override public void neighborChanged(BlockPos pos, Block block, Orientation orientation) {
            instant.neighborChanged(pos, block, orientation);
        }
        @Override public void neighborChanged(BlockState state, BlockPos pos, Block block, Orientation orientation, boolean moved) {
            instant.neighborChanged(state, pos, block, orientation, moved);
        }
        @Override public void updateNeighborsAtExceptFromFacing(BlockPos pos, Block block, Direction skip, Orientation orientation) {
            instant.updateNeighborsAtExceptFromFacing(pos, block, skip, orientation);
        }
    }
}
