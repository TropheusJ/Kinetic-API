package com.simibubi.kinetic_api.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class SpecialBlockStateGen {

	protected IntProperty<?>[] getIgnoredProperties() {
		return new IntProperty<?>[0];
	}
	
	public final <T extends BeetrootsBlock> void generate(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStatesExcept(state -> {
				return ConfiguredModel.builder()
					.modelFile(getModel(ctx, prov, state))
					.rotationX((getXRotation(state) + 360) % 360)
					.rotationY((getYRotation(state) + 360) % 360)
					.build();
			}, getIgnoredProperties());
	}

	protected int horizontalAngle(Direction direction) {
		if (direction.getAxis().isVertical())
			return 0;
		return (int) direction.asRotation();
	}
	
	protected abstract int getXRotation(PistonHandler state);

	protected abstract int getYRotation(PistonHandler state);

	public abstract <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx,
		RegistrateBlockstateProvider prov, PistonHandler state);

}
