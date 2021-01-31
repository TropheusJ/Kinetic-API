package com.simibubi.kinetic_api.content.contraptions.fluids.pipes;

import java.util.List;
import bqx;
import com.simibubi.kinetic_api.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.kinetic_api.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class StraightPipeTileEntity extends SmartTileEntity {

	public StraightPipeTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StraightPipeFluidTransportBehaviour(this));
		behaviours.add(new BracketedTileEntityBehaviour(this));
	}

	static class StraightPipeFluidTransportBehaviour extends FluidTransportBehaviour {

		public StraightPipeFluidTransportBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canHaveFlowToward(PistonHandler state, Direction direction) {
			return state.b(AxisPipeBlock.e) && state.c(AxisPipeBlock.e) == direction.getAxis();
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(bqx world, BlockPos pos, PistonHandler state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
			PistonHandler otherState = world.d_(pos.offset(direction));

			Axis axis = IAxisPipe.getAxisOf(state);
			Axis otherAxis = IAxisPipe.getAxisOf(otherState);

			if (axis == otherAxis && axis != null)
				if (state.b() == otherState.b() || direction.getDirection() == AxisDirection.POSITIVE)
					return AttachmentTypes.NONE;

			if (otherState.b() instanceof FluidValveBlock
				&& FluidValveBlock.getPipeAxis(otherState) == direction.getAxis())
				return AttachmentTypes.NONE;

			return attachment;
		}

	}

}
