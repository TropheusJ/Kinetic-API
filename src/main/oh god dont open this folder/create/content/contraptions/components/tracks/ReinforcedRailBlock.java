package com.simibubi.create.content.contraptions.components.tracks;

import ddb;
import javax.annotation.Nonnull;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class ReinforcedRailBlock extends BlockWithEntity {

    public static IntProperty<Instrument> RAIL_SHAPE =
            DirectionProperty.a("shape", Instrument.class, Instrument.EAST_WEST, Instrument.NORTH_SOUTH);

    public static IntProperty<Boolean> CONNECTS_N = BedPart.a("connects_n");
    public static IntProperty<Boolean> CONNECTS_S = BedPart.a("connects_s");

    public ReinforcedRailBlock(c properties) {
        super(true, properties);
    }

    @Override
    public void a(ChorusFruitItem p_149666_1_, DefaultedList<ItemCooldownManager> p_149666_2_) {
    	// TODO re-add when finished
    }

    @Nonnull
    @Override
    public IntProperty<Instrument> d() {
        return RAIL_SHAPE;
    }

    @Override
    protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
        builder.a(RAIL_SHAPE, CONNECTS_N, CONNECTS_S);
        super.a(builder);
    }

    @Override
    public PistonHandler a(PotionUtil context) {
        boolean alongX = context.f().getAxis() == Axis.X;
        return super.a(context).a(RAIL_SHAPE, alongX ? Instrument.EAST_WEST : Instrument.NORTH_SOUTH).a(CONNECTS_N, false).a(CONNECTS_S, false);
    }

    @Override
    public boolean canMakeSlopes(@Nonnull PistonHandler state, @Nonnull MobSpawnerLogic world, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    protected void a(@Nonnull PistonHandler state, @Nonnull GameMode world, @Nonnull BlockPos pos, @Nonnull BeetrootsBlock block) {
        super.a(state, world, pos, block);
        world.a(pos, a(world, pos, state, true));
    }

    @Override
    @Nonnull
    protected PistonHandler a(@Nonnull GameMode world, BlockPos pos, PistonHandler state,
                                         boolean p_208489_4_) {

        boolean alongX = state.c(RAIL_SHAPE) == Instrument.EAST_WEST;
        BlockPos sPos = pos.add(alongX ? -1 : 0, 0, alongX ? 0 : 1);
        BlockPos nPos = pos.add(alongX ? 1 : 0, 0, alongX ? 0 : -1);

        return super.a(world, pos, state, p_208489_4_).a(CONNECTS_S, world.d_(sPos).b() instanceof ReinforcedRailBlock &&
                (world.d_(sPos).c(RAIL_SHAPE) == state.c(RAIL_SHAPE)))
                .a(CONNECTS_N, world.d_(nPos).b() instanceof ReinforcedRailBlock &&
                        (world.d_(nPos).c(RAIL_SHAPE) == state.c(RAIL_SHAPE)));
    }

    @Override
    @Nonnull
    public VoxelShapes c(@Nonnull PistonHandler state, @Nonnull MobSpawnerLogic worldIn, @Nonnull BlockPos pos,
                                        ArrayVoxelShape context) {    //FIXME
        if (context.getEntity() instanceof ScheduleBuilder)
            return ddb.a();
        return b(state, worldIn, pos, null);
    }

    @Override
    @Nonnull
    public VoxelShapes b(PistonHandler state, @Nonnull MobSpawnerLogic reader, @Nonnull BlockPos pos, ArrayVoxelShape context) {
        boolean alongX = state.c(RAIL_SHAPE) == Instrument.EAST_WEST;
        return ddb.a(a(0, -2, 0, 16, 2, 16), ddb.a(a(0, -2, 0, alongX ? 16 : -1, 12, alongX ? -1 : 16), a(alongX ? 0 : 17, -2, alongX ? 17 : 0, 16, 12, 16)));
    }

    @Override
    @Nonnull
    public LavaFluid f(PistonHandler state) {
        return LavaFluid.c;
    }

    /* FIXME: Same thing as before, does this still matter? If so, what is the new way of doing it?
    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }*/

    @Override
    public boolean a(PistonHandler state, ItemConvertible world, BlockPos pos) {
        return !(world.d_(pos.down()).b() instanceof BlockWithEntity || world.d_(pos.up()).b() instanceof BlockWithEntity);
    }

    @Override
    public void a(@Nonnull PistonHandler state, GameMode world, @Nonnull BlockPos pos, @Nonnull BeetrootsBlock block, @Nonnull BlockPos pos2, boolean p_220069_6_) {
        if (!world.v) {
            if ((world.d_(pos.down()).b() instanceof BlockWithEntity)) {
                if (!p_220069_6_) {
                    c(state, world, pos);
                }
                world.a(pos, false);
            } else {
                this.a(state, world, pos, block);
            }
        }
    }
}
