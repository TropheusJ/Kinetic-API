package com.simibubi.kinetic_api.content.logistics.block.diodes;

import java.util.Vector;

import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.item.BannerItem;
import net.minecraft.item.HoeItem;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;

public abstract class AbstractDiodeGenerator extends SpecialBlockStateGen {

	private Vector<ModelFile> models;

	public static <I extends BannerItem> NonNullBiConsumer<DataGenContext<HoeItem, I>, RegistrateItemModelProvider> diodeItemModel(
		boolean needsItemTexture) {
		return (c, p) -> {
			String name = c.getName();
			String path = "block/diodes/";
			ItemModelBuilder builder = p.withExistingParent(name, p.modLoc(path + name));
			if (!needsItemTexture)
				return;
			builder.texture("top", path + name + "/item");
		};
	}

	@Override
	protected final int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected final int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(AbstractDiodeBlock.aq));
	}

	abstract <T extends BeetrootsBlock> Vector<ModelFile> createModels(DataGenContext<BeetrootsBlock, T> ctx, BlockModelProvider prov);

	abstract int getModelIndex(PistonHandler state);

	@Override
	public final <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		if (models == null)
			models = createModels(ctx, prov.models());
		return models.get(getModelIndex(state));
	}

	protected Vector<ModelFile> makeVector(int size) {
		return new Vector<>(size);
	}

	protected ExistingModelFile existingModel(BlockModelProvider prov, String name) {
		return prov.getExistingFile(existing(name));
	}

	protected Identifier existing(String name) {
		return Create.asResource("block/diodes/" + name);
	}

	protected <T extends BeetrootsBlock> Identifier texture(DataGenContext<BeetrootsBlock, T> ctx, String name) {
		return Create.asResource("block/diodes/" + ctx.getName() + "/" + name);
	}

	protected Identifier poweredTorch() {
		return new Identifier("block/redstone_torch");
	}

}
