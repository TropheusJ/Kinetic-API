package com.simibubi.kinetic_api.foundation.data;

import java.util.function.BiFunction;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import cut;
import net.minecraft.util.Identifier;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;

/**
 * For registering fluids with no buckets/blocks
 */
public class VirtualFluidBuilder<T extends ForgeFlowingFluid, P> extends FluidBuilder<T, P> {

	public VirtualFluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		Identifier stillTexture, Identifier flowingTexture,
		BiFunction<FluidAttributes.Builder, cut, FluidAttributes> attributesFactory,
		NonNullFunction<Properties, T> factory) {
		super(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory);
		source(factory);
	}

	@Override
	public NonNullSupplier<T> asSupplier() {
		return this::getEntry;
	}

}
