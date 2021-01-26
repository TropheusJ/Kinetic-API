package com.simibubi.create.content.contraptions.components.actors;

import java.util.Optional;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PortableStorageInterfaceMovement extends MovementBehaviour {

	static final String _workingPos_ = "WorkingPos";
	static final String _clientPrevPos_ = "ClientPrevPos";

	@Override
	public EntityHitResult getActiveAreaOffset(MovementContext context) {
		return EntityHitResult.b(context.state.c(PortableStorageInterfaceBlock.SHAPE)
			.getVector()).a(1.85f);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffer) {
		PortableStorageInterfaceRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		context.data.remove(_workingPos_);
		if (findInterface(context, pos))
			context.stall = true;
	}

	protected boolean findInterface(MovementContext context, BlockPos pos) {
		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return false;

		Direction currentFacing = currentFacingIfValid.get();
		PortableStorageInterfaceTileEntity psi =
			findStationaryInterface(context.world, pos, context.state, currentFacing);
		if (psi == null)
			return false;

		if ((psi.isTransferring() || psi.isPowered()) && !context.world.v)
			return false;
		context.data.put(_workingPos_, NbtHelper.fromBlockPos(psi.o()));
		if (!context.world.v) {
			EntityHitResult diff = VecHelper.getCenterOf(psi.o())
				.d(context.position);
			diff = VecHelper.project(diff, EntityHitResult.b(currentFacing.getVector()));
			float distance = (float) (diff.f() + 1.85f - 1);
			psi.startTransferringTo(context.contraption, distance);
		} else {
			context.data.put(_clientPrevPos_, NbtHelper.fromBlockPos(pos));
		}
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world.v) {
			boolean stalled = context.contraption.stalled;
			if (stalled && !context.data.contains(_workingPos_)) {
				BlockPos pos = new BlockPos(context.position);
				if (!context.data.contains(_clientPrevPos_)
					|| !NbtHelper.toBlockPos(context.data.getCompound(_clientPrevPos_))
						.equals(pos))
					findInterface(context, pos);
			}
			if (!stalled)
				reset(context);
			return;
		}

		if (!context.data.contains(_workingPos_))
			return;

		BlockPos pos = NbtHelper.toBlockPos(context.data.getCompound(_workingPos_));
		Optional<Direction> currentFacingIfValid = getCurrentFacingIfValid(context);
		if (!currentFacingIfValid.isPresent())
			return;

		PortableStorageInterfaceTileEntity stationaryInterface =
			getStationaryInterfaceAt(context.world, pos, context.state, currentFacingIfValid.get());
		if (stationaryInterface == null || !stationaryInterface.isTransferring()) {
			reset(context);
			return;
		}
	}

	@Override
	public void stopMoving(MovementContext context) {
		reset(context);
	}

	public void reset(MovementContext context) {
		context.data.remove(_clientPrevPos_);
		context.data.remove(_workingPos_);
		context.stall = false;
	}

	private PortableStorageInterfaceTileEntity findStationaryInterface(GameMode world, BlockPos pos, PistonHandler state,
		Direction facing) {
		for (int i = 0; i < 2; i++) {
			PortableStorageInterfaceTileEntity interfaceAt =
				getStationaryInterfaceAt(world, pos.offset(facing, i), state, facing);
			if (interfaceAt == null)
				continue;
			return interfaceAt;
		}
		return null;
	}

	private PortableStorageInterfaceTileEntity getStationaryInterfaceAt(GameMode world, BlockPos pos, PistonHandler state,
		Direction facing) {
		BeehiveBlockEntity te = world.c(pos);
		if (!(te instanceof PortableStorageInterfaceTileEntity))
			return null;
		PistonHandler blockState = world.d_(pos);
		if (blockState.b() != state.b())
			return null;
		if (blockState.c(PortableStorageInterfaceBlock.SHAPE) != facing.getOpposite())
			return null;
		return (PortableStorageInterfaceTileEntity) te;
	}

	private Optional<Direction> getCurrentFacingIfValid(MovementContext context) {
		EntityHitResult directionVec = EntityHitResult.b(context.state.c(PortableStorageInterfaceBlock.SHAPE)
			.getVector());
		directionVec = context.rotation.apply(directionVec);
		Direction facingFromVector = Direction.getFacing(directionVec.entity, directionVec.c, directionVec.d);
		if (directionVec.f(EntityHitResult.b(facingFromVector.getVector())) > 1 / 2f)
			return Optional.empty();
		return Optional.of(facingFromVector);
	}

}
