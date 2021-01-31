package com.simibubi.kinetic_api.content.contraptions.components.turntable;

import afj;
import apx;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.KineticBlock;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class TurntableBlock extends KineticBlock implements ITE<TurntableTileEntity> {

	public TurntableBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.TURNTABLE.create();
	}

	@Override
	public RedstoneLampBlock b(PistonHandler state) {
		return RedstoneLampBlock.b;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.TURNTABLE_SHAPE;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx e) {
		if (!e.an())
			return;
		if (e.cB().c > 0)
			return;
		if (e.cD() < pos.getY() + .5f)
			return;

		withTileEntityDo(worldIn, pos, te -> {
			float speed = ((KineticTileEntity) te).getSpeed() * 3 / 10;
			if (speed == 0)
				return;

			GameMode world = e.cf();
			if (world.v && (e instanceof PlayerAbilities)) {
				if (worldIn.d_(e.cA()) != state) {
					EntityHitResult origin = VecHelper.getCenterOf(pos);
					EntityHitResult offset = e.cz()
						.d(origin);
					offset = VecHelper.rotate(offset, afj.a(speed, -16, 16) / 1f, Axis.Y);
					EntityHitResult movement = origin.e(offset)
						.d(e.cz());
					e.f(e.cB()
						.e(movement));
					e.w = true;
				}
			}

			if ((e instanceof PlayerAbilities))
				return;
			if (world.v)
				return;

			if ((e instanceof SaddledComponent)) {
				float diff = e.bJ() - speed;
				((SaddledComponent) e).n(20);
				e.n(diff);
				e.m(diff);
				e.c(false);
				e.w = true;
			}

			e.p -= speed;
		});
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return Axis.Y;
	}

	@Override
	public Class<TurntableTileEntity> getTileEntityClass() {
		return TurntableTileEntity.class;
	}

}
