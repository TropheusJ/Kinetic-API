package com.simibubi.create.content.contraptions.components.actors;

import javax.annotation.ParametersAreNonnullByDefault;
import afj;
import apx;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ItemConvertible;
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
public class DrillBlock extends DirectionalKineticBlock implements ITE<DrillTileEntity> {
	public static DamageRecord damageSourceDrill = new DamageRecord("create.mechanical_drill").l();

	public DrillBlock(c properties) {
		super(properties);
	}
	
	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
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
			entityIn.a(damageSourceDrill, (float) getDamage(te.getSpeed()));
		});
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.DRILL.create();
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.CASING_12PX.get(state.c(FACING));
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		withTileEntityDo(worldIn, pos, DrillTileEntity::destroyNextTick);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == state.c(FACING).getOpposite();
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.a;
	}

	@Override
	public Class<DrillTileEntity> getTileEntityClass() {
		return DrillTileEntity.class;
	}

	public static double getDamage(float speed) {
		float speedAbs = Math.abs(speed);
		double sub1 = Math.min(speedAbs / 16, 2);
		double sub2 = Math.min(speedAbs / 32, 4);
		double sub3 = Math.min(speedAbs / 64, 4);
		return afj.a(sub1 + sub2 + sub3, 1, 10);
	}
}
