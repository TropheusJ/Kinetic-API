package com.simibubi.create.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class AdjustableRepeaterGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends BeetrootsBlock> Vector<ModelFile> createModels(DataGenContext<BeetrootsBlock, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(4);
		String name = ctx.getName();
		Identifier template = existing(name);

		models.add(prov.getExistingFile(template));
		models.add(prov.withExistingParent(name + "_powered", template)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.withExistingParent(name + "_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powering")));
		models.add(prov.withExistingParent(name + "_powered_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	int getModelIndex(PistonHandler state) {
		return (state.c(AdjustableRepeaterBlock.POWERING) ? 2 : 0) + (state.c(AdjustableRepeaterBlock.SHAPE) ? 1 : 0);
	}

}
