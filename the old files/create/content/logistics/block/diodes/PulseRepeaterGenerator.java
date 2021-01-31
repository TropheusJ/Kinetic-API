package com.simibubi.kinetic_api.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class PulseRepeaterGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends BeetrootsBlock> Vector<ModelFile> createModels(DataGenContext<BeetrootsBlock, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(3);
		String name = ctx.getName();
		Identifier template = existing(name);

		models.add(prov.getExistingFile(template));
		models.add(prov.withExistingParent(name + "_powered", template)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.withExistingParent(name + "_pulsing", template)
			.texture("top", texture(ctx, "powered"))
			.texture("torch", poweredTorch()));

		return models;
	}

	@Override
	int getModelIndex(PistonHandler state) {
		return state.c(PulseRepeaterBlock.PULSING) ? 2 : state.c(PulseRepeaterBlock.SHAPE) ? 1 : 0;
	}

}
