package com.simibubi.kinetic_api.content.contraptions.components.crusher;

import static com.simibubi.kinetic_api.content.contraptions.components.crusher.CrushingWheelControllerBlock.VALID;

import apx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllShapes;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.BellBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;

public class CrushingWheelBlock extends RotatedPillarKineticBlock implements ITE<CrushingWheelTileEntity> {

	public CrushingWheelBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CRUSHING_WHEEL.create();
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(AXIS);
	}

	@Override
	public RedstoneLampBlock b(PistonHandler state) {
		return RedstoneLampBlock.b;
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos,
		ArrayVoxelShape context) {
		return AllShapes.CRUSHING_WHEEL_COLLISION_SHAPE;
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {

		for (Direction d : Iterate.horizontalDirections) {
			if (d.getAxis() == state.c(AXIS))
				continue;
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(worldIn.d_(pos.offset(d))))
				worldIn.a(pos.offset(d), BellBlock.FACING.n());
		}

		if (state.hasTileEntity() && state.b() != newState.b()) {
			worldIn.o(pos);
		}
	}

	public void updateControllers(PistonHandler state, GameMode world, BlockPos pos, Direction facing) {
		if (facing.getAxis() == state.c(AXIS) || facing.getAxis()
			.isVertical())
			return;
		if (world == null)
			return;

		BlockPos controllerPos = pos.offset(facing);
		BlockPos otherWheelPos = pos.offset(facing, 2);

		boolean controllerExists = AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(world.d_(controllerPos));
		boolean controllerIsValid = controllerExists && world.d_(controllerPos)
			.c(VALID);
		boolean controllerShouldExist = false;
		boolean controllerShouldBeValid = false;

		PistonHandler otherState = world.d_(otherWheelPos);
		if (AllBlocks.CRUSHING_WHEEL.has(otherState)) {
			controllerShouldExist = true;

			try {
				CrushingWheelTileEntity te = getTileEntity(world, pos);
				CrushingWheelTileEntity otherTe = getTileEntity(world, otherWheelPos);

				if (te != null && otherTe != null && (te.getSpeed() > 0) != (otherTe.getSpeed() > 0)
					&& te.getSpeed() != 0) {
					float signum = Math.signum(te.getSpeed()) * (state.c(AXIS) == Axis.X ? -1 : 1);
					controllerShouldBeValid = facing.getDirection()
						.offset() != signum;
				}
				if (otherState.c(AXIS) != state.c(AXIS))
					controllerShouldExist = false;

			} catch (TileEntityException e) {
				controllerShouldExist = false;
			}
		}

		if (!controllerShouldExist) {
			if (controllerExists)
				world.a(controllerPos, BellBlock.FACING.n());
			return;
		}

		if (!controllerExists) {
			if (!world.d_(controllerPos)
				.c()
				.e())
				return;
			world.a(controllerPos, AllBlocks.CRUSHING_WHEEL_CONTROLLER.getDefaultState()
				.a(VALID, controllerShouldBeValid));
		} else if (controllerIsValid != controllerShouldBeValid) {
			world.a(controllerPos, world.d_(controllerPos)
				.a(VALID, controllerShouldBeValid));
		}

		((CrushingWheelControllerBlock) AllBlocks.CRUSHING_WHEEL_CONTROLLER.get())
			.updateSpeed(world.d_(controllerPos), world, controllerPos);

	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx entityIn) {
		try {
			CrushingWheelTileEntity te = getTileEntity(worldIn, pos);
			if (entityIn.cD() < pos.getY() + 1.25f || !entityIn.an())
				return;

			double x = 0;
			double z = 0;

			if (state.c(AXIS) == Axis.X) {
				z = te.getSpeed() / 20f;
				x += (pos.getX() + .5f - entityIn.cC()) * .1f;
			}
			if (state.c(AXIS) == Axis.Z) {
				x = te.getSpeed() / -20f;
				z += (pos.getZ() + .5f - entityIn.cG()) * .1f;
			}
			entityIn.f(entityIn.cB()
				.b(x, 0, z));

		} catch (TileEntityException e) {
		}
	}

	@Override
	public boolean a(PistonHandler state, ItemConvertible worldIn, BlockPos pos) {
		for (Direction direction : Iterate.directions) {
			BlockPos neighbourPos = pos.offset(direction);
			PistonHandler neighbourState = worldIn.d_(neighbourPos);
			Axis stateAxis = state.c(AXIS);
			if (AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(neighbourState) && direction.getAxis() != stateAxis)
				return false;
			if (!AllBlocks.CRUSHING_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.c(AXIS) != stateAxis || stateAxis != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() == state.c(AXIS);
	}

	@Override
	public float getParticleTargetRadius() {
		return 1.125f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 1f;
	}

	@Override
	public Class<CrushingWheelTileEntity> getTileEntityClass() {
		return CrushingWheelTileEntity.class;
	}

}
