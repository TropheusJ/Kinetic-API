package com.simibubi.kinetic_api.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class ToggleLatchGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends BeetrootsBlock> Vector<ModelFile> createModels(DataGenContext<BeetrootsBlock, T> ctx, BlockModelProvider prov) {
		String name = ctx.getName();
		Vector<ModelFile> models = makeVector(4);
		Identifier off = existing("latch_off");
		Identifier on = existing("latch_on");

		models.add(prov.getExistingFile(off));
		models.add(prov.withExistingParent(name + "_off_powered", off)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.getExistingFile(on));
		models.add(prov.withExistingParent(name + "_on_powered", on)
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	int getModelIndex(PistonHandler state) {
		return (state.c(ToggleLatchBlock.POWERING) ? 2 : 0) + (state.c(ToggleLatchBlock.SHAPE) ? 1 : 0);
	}

}
