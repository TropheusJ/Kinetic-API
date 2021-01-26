package com.simibubi.create.content.contraptions.relays.gauge;

import com.simibubi.create.foundation.data.DirectionalAxisBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;

public class GaugeGenerator extends DirectionalAxisBlockStateGen {

	@Override
	public <T extends BeetrootsBlock> String getModelPrefix(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return "block/gauge/base";
	}

}
