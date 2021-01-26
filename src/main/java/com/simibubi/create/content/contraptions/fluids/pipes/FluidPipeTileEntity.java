package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;
import bqx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeTileEntity.StandardPipeFluidTransportBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FluidPipeTileEntity extends SmartTileEntity {

	public FluidPipeTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StandardPipeFluidTransportBehaviour(this));
		behaviours.add(new BracketedTileEntityBehaviour(this, this::canHaveBracket)
			.withTrigger(state -> AllTriggers.BRACKET_PIPE));
	}

	private boolean canHaveBracket(PistonHandler state) {
		return !(state.b() instanceof EncasedPipeBlock);
	}

	class StandardPipeFluidTransportBehaviour extends FluidTransportBehaviour {

		public StandardPipeFluidTransportBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canHaveFlowToward(PistonHandler state, Direction direction) {
			return (FluidPipeBlock.isPipe(state) || state.b() instanceof EncasedPipeBlock)
				&& state.c(FluidPipeBlock.g.get(direction));
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(bqx world, BlockPos pos, PistonHandler state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

			if (attachment == AttachmentTypes.RIM && AllBlocks.ENCASED_FLUID_PIPE.has(state))
				return AttachmentTypes.RIM;

			BlockPos offsetPos = pos.offset(direction);
			if (!FluidPipeBlock.isPipe(world.d_(offsetPos))) {
				FluidTransportBehaviour pipeBehaviour =
					TileEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
				if (pipeBehaviour != null
					&& pipeBehaviour.canHaveFlowToward(world.d_(offsetPos), direction.getOpposite()))
					return AttachmentTypes.NONE;
			}

			if (attachment == AttachmentTypes.RIM && !FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
				return AttachmentTypes.NONE;
			return attachment;
		}

	}

}
