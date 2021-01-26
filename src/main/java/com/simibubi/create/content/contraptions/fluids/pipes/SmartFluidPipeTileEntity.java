package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeTileEntity.SmartPipeBehaviour;
import com.simibubi.create.content.contraptions.fluids.pipes.SmartFluidPipeTileEntity.SmartPipeFilterSlot;
import com.simibubi.create.content.contraptions.fluids.pipes.StraightPipeTileEntity.StraightPipeFluidTransportBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.fluids.FluidStack;

public class SmartFluidPipeTileEntity extends SmartTileEntity {

	private FilteringBehaviour filter;

	public SmartFluidPipeTileEntity(BellBlockEntity<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new SmartPipeBehaviour(this));
		behaviours.add(filter = new FilteringBehaviour(this, new SmartPipeFilterSlot()).forFluids()
			.withCallback(this::onFilterChanged));
	}

	private void onFilterChanged(ItemCooldownManager newFilter) {
		if (d.v)
			return;
		FluidPropagator.propagateChangedPipe(d, e, p());
	}

	class SmartPipeBehaviour extends StraightPipeFluidTransportBehaviour {

		public SmartPipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canPullFluidFrom(FluidStack fluid, PistonHandler state, Direction direction) {
			if (fluid.isEmpty() || filter != null && filter.test(fluid))
				return super.canPullFluidFrom(fluid, state, direction);
			return false;
		}

		@Override
		public boolean canHaveFlowToward(PistonHandler state, Direction direction) {
			return state.b() instanceof SmartFluidPipeBlock
				&& SmartFluidPipeBlock.getPipeAxis(state) == direction.getAxis();
		}

	}

	class SmartPipeFilterSlot extends ValueBoxTransform {

		@Override
		protected EntityHitResult getLocalOffset(PistonHandler state) {
			BlockPredicate face = state.c(SmartFluidPipeBlock.u);
			float y = face == BlockPredicate.c ? 0.3f : face == BlockPredicate.b ? 11.3f : 15.3f;
			float z = face == BlockPredicate.c ? 4.6f : face == BlockPredicate.b ? 0.6f : 4.6f;
			return VecHelper.rotateCentered(VecHelper.voxelSpace(8, y, z), angleY(state), Axis.Y);
		}

		@Override
		protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
			BlockPredicate face = state.c(SmartFluidPipeBlock.u);
			MatrixStacker.of(ms)
				.rotateY(angleY(state))
				.rotateX(face == BlockPredicate.c ? -45 : 45);
		}

		protected float angleY(PistonHandler state) {
			BlockPredicate face = state.c(SmartFluidPipeBlock.u);
			float horizontalAngle = AngleHelper.horizontalAngle(state.c(SmartFluidPipeBlock.aq));
			if (face == BlockPredicate.b)
				horizontalAngle += 180;
			return horizontalAngle;
		}

	}

}
