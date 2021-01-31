package com.simibubi.kinetic_api.content.contraptions.relays.elementary;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.content.contraptions.base.IRotate;
import com.simibubi.kinetic_api.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class CogWheelBlock extends AbstractShaftBlock {

    boolean isLarge;

    private CogWheelBlock(boolean large, c properties) {
        super(properties);
        isLarge = large;
    }

    public static CogWheelBlock small(c properties) {
        return new CogWheelBlock(false, properties);
    }

    public static CogWheelBlock large(c properties) {
        return new CogWheelBlock(true, properties);
    }

    public static boolean isSmallCog(PistonHandler state) {
        return AllBlocks.COGWHEEL.has(state);
    }

    public static boolean isLargeCog(PistonHandler state) {
        return AllBlocks.LARGE_COGWHEEL.has(state);
    }

    @Override
    public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
        return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.c(AXIS));
    }

    @Override
    public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() == state.c(AXIS))
                continue;

            PistonHandler blockState = worldIn.d_(pos.offset(facing));
            if (blockState.b(AXIS) && facing.getAxis() == blockState.c(AXIS))
            	continue;
            
            if (isLargeCog(blockState) || isLarge && isSmallCog(blockState))
                return false;
        }
        return true;
    }

    @Override
    public PistonHandler a(PotionUtil context) {
        BlockPos placedOnPos = context.a().offset(context.j().getOpposite());
        GameMode world = context.p();
        PistonHandler placedAgainst = world.d_(placedOnPos);
        BeetrootsBlock block = placedAgainst.b();

        if (context.n() != null && context.n().bt())
			return this.n().a(AXIS, context.j().getAxis());
        
        PistonHandler stateBelow = world.d_(context.a()
                .down());
        EmptyFluid FluidState = context.p().b(context.a());
        if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(stateBelow) && isLarge) {
            return this.n()
                    .a(BambooLeaves.C, FluidState.a() == FlowableFluid.c)
                    .a(AXIS, stateBelow.c(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X);
        }

        if (!(block instanceof IRotate)
                || !(((IRotate) block).hasIntegratedCogwheel(world, placedOnPos, placedAgainst))) {
            Axis preferredAxis = getPreferredAxis(context);
            if (preferredAxis != null)
                return this.n()
                        .a(AXIS, preferredAxis)
                        .a(BambooLeaves.C, FluidState.a() == FlowableFluid.c);
            return this.n()
                    .a(AXIS, context.j().getAxis())
                    .a(BambooLeaves.C, FluidState.a() == FlowableFluid.c);
        }

        return n().a(AXIS, ((IRotate) block).getRotationAxis(placedAgainst));
    }

    @Override
    public float getParticleTargetRadius() {
        return isLarge ? 1.125f : .65f;
    }

    @Override
    public float getParticleInitialRadius() {
        return isLarge ? 1f : .75f;
    }

    public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {
        items.add(new ItemCooldownManager(this));
    }

    // IRotate

    @Override
    public boolean hasIntegratedCogwheel(ItemConvertible world, BlockPos pos, PistonHandler state) {
        return !isLarge;
    }
}
