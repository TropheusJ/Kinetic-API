package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import javax.annotation.Nullable;
import cef;
import com.simibubi.kinetic_api.content.contraptions.base.RotatedPillarKineticBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@MethodsReturnNonnullByDefault
public abstract class AbstractEncasedShaftBlock extends RotatedPillarKineticBlock {
    public AbstractEncasedShaftBlock(c properties) {
        super(properties);
    }

    @Override
    protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
        super.a(builder);
    }

    @Override
    public boolean shouldCheckWeakPower(PistonHandler state, ItemConvertible world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public LavaFluid f(@Nullable PistonHandler state) {
        return LavaFluid.a;
    }

    @Override
    public PistonHandler a(PotionUtil context) {
        if (context.n() != null && context.n()
                .bt())
            return super.a(context);
        Direction.Axis preferredAxis = getPreferredAxis(context);
        return this.n()
                .a(AXIS, preferredAxis == null ? context.d()
                        .getAxis() : preferredAxis);
    }

    @Override
    public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
        return face.getAxis() == state.c(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(PistonHandler state) {
        return state.c(AXIS);
    }
}
