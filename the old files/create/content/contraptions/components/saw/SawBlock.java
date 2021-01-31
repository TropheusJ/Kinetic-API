package com.simibubi.kinetic_api.content.contraptions.components.saw;

import javax.annotation.ParametersAreNonnullByDefault;
import apx;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.DrillBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.timer.Timer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SawBlock extends DirectionalAxisKineticBlock implements ITE<SawTileEntity> {
	public static DamageRecord damageSourceSaw = new DamageRecord("kinetic_api.mechanical_saw").l();

	public SawBlock(c properties) {
		super(properties);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		PistonHandler stateForPlacement = super.a(context);
		Direction facing = stateForPlacement.c(FACING);
		if (facing.getAxis().isVertical())
			return stateForPlacement;
		return stateForPlacement.a(AXIS_ALONG_FIRST_COORDINATE, facing.getAxis() == Axis.X);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.SAW.create();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.CASING_12PX.get(state.c(FACING));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx entityIn) {
		if (entityIn instanceof PaintingEntity)
			return;
		if (!new Timer(pos).h(.1f).c(entityIn.cb()))
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			entityIn.a(damageSourceSaw, (float) DrillBlock.getDamage(te.getSpeed()));
		});
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		if (!(entityIn instanceof PaintingEntity))
			return;
		if (entityIn.l.v)
			return;

		BlockPos pos = entityIn.cA();
		withTileEntityDo(entityIn.l, pos, te -> {
			if (te.getSpeed() == 0)
				return;
			te.insertItem((PaintingEntity) entityIn);
		});
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	public static boolean isHorizontal(PistonHandler state) {
		return state.c(FACING).getAxis().isHorizontal();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return isHorizontal(state) ? state.c(FACING).getAxis() : super.getRotationAxis(state);
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return isHorizontal(state) ? face == state.c(FACING).getOpposite()
				: super.hasShaftTowards(world, pos, state, face);
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
      		worldIn.o(pos);
	}

	@Override
	public Class<SawTileEntity> getTileEntityClass() {
		return SawTileEntity.class;
	}

}
