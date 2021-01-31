package com.simibubi.kinetic_api.content.contraptions.relays.advanced.sequencer;

import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public class SequencedGearshiftGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return state.c(SequencedGearshiftBlock.VERTICAL) ? 90 : 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return state.c(SequencedGearshiftBlock.HORIZONTAL_AXIS) == Axis.X ? 90 : 0;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		String variant = "idle";
		int seq = state.c(SequencedGearshiftBlock.STATE);
		if (seq > 0)
			variant = "seq_" + seq;
		return prov.models()
			.getExistingFile(prov.modLoc("block/" + ctx.getName() + "/" + variant));
	}

}
