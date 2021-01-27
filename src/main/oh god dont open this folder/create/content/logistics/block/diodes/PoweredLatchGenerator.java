package com.simibubi.create.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class PoweredLatchGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends BeetrootsBlock> Vector<ModelFile> createModels(DataGenContext<BeetrootsBlock, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(2);
		String name = ctx.getName();
		Identifier off = existing("latch_off");
		Identifier on = existing("latch_on");
		
		models.add(prov.withExistingParent(name, off)
			.texture("top", texture(ctx, "idle")));
		models.add(prov.withExistingParent(name + "_powered", on)
			.texture("top", texture(ctx, "powering")));
		
		return models;
	}

	@Override
	int getModelIndex(PistonHandler state) {
		return state.c(PoweredLatchBlock.POWERING)? 1 : 0;
	}

}
