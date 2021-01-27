package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import cut;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;

public class FluidReactions {

	public static void handlePipeFlowCollision(GameMode world, BlockPos pos, FluidStack fluid, FluidStack fluid2) {
		cut f1 = fluid.getFluid();
		cut f2 = fluid2.getFluid();
		BlockHelper.destroyBlock(world, pos, 1);
		AllTriggers.triggerForNearbyPlayers(AllTriggers.PIPE_COLLISION, world, pos, 5);

		if (f1 == FlowableFluid.c && f2 == FlowableFluid.field_15901 || f2 == FlowableFluid.c && f1 == FlowableFluid.field_15901)
			world.a(pos, BellBlock.m.n());
		else if (f1 == FlowableFluid.field_15901 && FluidHelper.hasBlockState(f2)) {
			PistonHandler lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f2)
				.h());
			if (lavaInteraction != null)
				world.a(pos, lavaInteraction);
		} else if (f2 == FlowableFluid.field_15901 && FluidHelper.hasBlockState(f1)) {
			PistonHandler lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(f1)
				.h());
			if (lavaInteraction != null)
				world.a(pos, lavaInteraction);
		}
	}

	public static void handlePipeSpillCollision(GameMode world, BlockPos pos, cut pipeFluid, EmptyFluid worldFluid) {
		cut pf = FluidHelper.convertToStill(pipeFluid);
		cut wf = worldFluid.a();
		if (pf.a(BlockTags.field_15481) && wf == FlowableFluid.field_15901)
			world.a(pos, BellBlock.bK.n());
		else if (pf == FlowableFluid.c && wf == FlowableFluid.d)
			world.a(pos, BellBlock.m.n());
		else if (pf == FlowableFluid.field_15901 && wf == FlowableFluid.c)
			world.a(pos, BellBlock.ATTACHMENT.n());
		else if (pf == FlowableFluid.field_15901 && wf == FlowableFluid.LEVEL)
			world.a(pos, BellBlock.m.n());

		if (pf == FlowableFluid.field_15901) {
			PistonHandler lavaInteraction = AllFluids.getLavaInteraction(worldFluid);
			if (lavaInteraction != null)
				world.a(pos, lavaInteraction);
		} else if (wf == FlowableFluid.d && FluidHelper.hasBlockState(pf)) {
			PistonHandler lavaInteraction = AllFluids.getLavaInteraction(FluidHelper.convertToFlowing(pf)
				.h());
			if (lavaInteraction != null)
				world.a(pos, lavaInteraction);
		}
	}

}
