package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import static com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class MechanicalPistonHeadBlock extends ProperDirectionalBlock implements SeagrassBlock {

    public static final DirectionProperty<BlockHalf> TYPE = BambooLeaves.aJ;

    public MechanicalPistonHeadBlock(c p_i48415_1_) {
        super(p_i48415_1_);
        j(super.n().a(BambooLeaves.C, false));
    }

    @Override
    protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
        builder.a(TYPE, BambooLeaves.C);
        super.a(builder);
    }

    @Override
    public LavaFluid f(PistonHandler state) {
        return LavaFluid.a;
    }

    @Override
    public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
                                  PlayerAbilities player) {
        return AllBlocks.PISTON_EXTENSION_POLE.asStack();
    }

    @Override
    public void a(GameMode worldIn, BlockPos pos, PistonHandler state, PlayerAbilities player) {
        Direction direction = state.c(SHAPE);
        BlockPos pistonHead = pos;
        BlockPos pistonBase = null;

        for (int offset = 1; offset < MechanicalPistonBlock.maxAllowedPistonPoles(); offset++) {
            BlockPos currentPos = pos.offset(direction.getOpposite(), offset);
            PistonHandler block = worldIn.d_(currentPos);

            if (isExtensionPole(block) && direction.getAxis() == block.c(BambooLeaves.M)
                    .getAxis())
                continue;

            if (MechanicalPistonBlock.isPiston(block) && block.c(BambooLeaves.M) == direction)
                pistonBase = currentPos;

            break;
        }

        if (pistonHead != null && pistonBase != null) {
            final BlockPos basePos = pistonBase;
            BlockPos.stream(pistonBase, pistonHead)
                    .filter(p -> !p.equals(pos) && !p.equals(basePos))
                    .forEach(p -> worldIn.b(p, !player.b_()));
            worldIn.a(basePos, worldIn.d_(basePos)
                    .a(MechanicalPistonBlock.STATE, PistonState.RETRACTED));
        }

        super.a(worldIn, pos, state, player);
    }

    @Override
    public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
        return AllShapes.MECHANICAL_PISTON_HEAD.get(state.c(SHAPE));
    }

    @Override
    public EmptyFluid d(PistonHandler state) {
        return state.c(BambooLeaves.C) ? FlowableFluid.c.a(false) : FlowableFluid.FALLING.h();
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
