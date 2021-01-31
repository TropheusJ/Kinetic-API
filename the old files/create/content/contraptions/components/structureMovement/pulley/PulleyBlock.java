package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import dcg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class PulleyBlock extends HorizontalAxisKineticBlock implements ITE<PulleyTileEntity> {

    public static DirectionProperty<Axis> HORIZONTAL_AXIS = BambooLeaves.E;

    public PulleyBlock(c properties) {
        super(properties);
    }

    private static void onRopeBroken(GameMode world, BlockPos pulleyPos) {
        BeehiveBlockEntity te = world.c(pulleyPos);
        if (!(te instanceof PulleyTileEntity))
            return;
        PulleyTileEntity pulley = (PulleyTileEntity) te;
        pulley.offset = 0;
        pulley.sendData();
    }

    @Override
    public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
        return AllTileEntities.ROPE_PULLEY.create();
    }

    @Override
    public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
        if (state.b() != newState.b()) {
            if (!worldIn.v) {
                PistonHandler below = worldIn.d_(pos.down());
                if (below.b() instanceof RopeBlockBase)
                    worldIn.b(pos.down(), true);
            }
            if (state.hasTileEntity())
                worldIn.o(pos);
        }
    }

    public Difficulty a(PistonHandler state, GameMode worldIn, BlockPos pos, PlayerAbilities player, ItemScatterer handIn,
                                  dcg hit) {
        if (!player.eJ())
            return Difficulty.PASS;
        if (player.bt())
            return Difficulty.PASS;
        if (player.b(handIn)
                .a()) {
            withTileEntityDo(worldIn, pos, te -> te.assembleNextTick = true);
            return Difficulty.SUCCESS;
        }
        return Difficulty.PASS;
    }

    @Override
    public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
        return AllShapes.PULLEY.get(state.c(HORIZONTAL_AXIS));
    }

    @Override
    public Class<PulleyTileEntity> getTileEntityClass() {
        return PulleyTileEntity.class;
    }

    private static class RopeBlockBase extends BeetrootsBlock implements SeagrassBlock {

        public RopeBlockBase(c properties) {
            super(properties);
            j(super.n().a(BambooLeaves.C, false));
        }

        @Override
        public LavaFluid f(PistonHandler state) {
            return LavaFluid.c;
        }

        @Override
        public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
                                      PlayerAbilities player) {
            return AllBlocks.ROPE_PULLEY.asStack();
        }

        @Override
        public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
            if (!isMoving && (!BlockHelper.hasBlockStateProperty(state, BambooLeaves.C) || !BlockHelper.hasBlockStateProperty(newState, BambooLeaves.C) || state.c(BambooLeaves.C) == newState.c(BambooLeaves.C))) {
                onRopeBroken(worldIn, pos.up());
                if (!worldIn.v) {
                    PistonHandler above = worldIn.d_(pos.up());
                    PistonHandler below = worldIn.d_(pos.down());
                    if (above.b() instanceof RopeBlockBase)
                        worldIn.b(pos.up(), true);
                    if (below.b() instanceof RopeBlockBase)
                        worldIn.b(pos.down(), true);
                }
            }
            if (state.hasTileEntity() && state.b() != newState.b()) {
                worldIn.o(pos);
            }
        }


        @Override
        public EmptyFluid d(PistonHandler state) {
            return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false) : FlowableFluid.FALLING.h();
        }

        @Override
        protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
            builder.a(BambooLeaves.C);
            super.a(builder);
        }

        @Override
        public PistonHandler a(PistonHandler state, Direction direction, PistonHandler neighbourState,
                                              GrassColors world, BlockPos pos, BlockPos neighbourPos) {
            if (state.c(BambooLeaves.C)) {
                world.H().a(pos, FlowableFluid.c, FlowableFluid.c.a(world));
            }
            return state;
        }

        @Override
        public PistonHandler a(PotionUtil context) {
            EmptyFluid FluidState = context.p().b(context.a());
            return super.a(context).a(BambooLeaves.C, Boolean.valueOf(FluidState.a() == FlowableFluid.c));
        }

    }

    public static class MagnetBlock extends RopeBlockBase {

        public MagnetBlock(c properties) {
            super(properties);
        }

        @Override
        public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
            return AllShapes.PULLEY_MAGNET;
        }

    }

    public static class RopeBlock extends RopeBlockBase {

        public RopeBlock(c properties) {
            super(properties);
        }

        @Override
        public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
            return AllShapes.FOUR_VOXEL_POLE.get(Direction.UP);
        }
    }

}
