package com.simibubi.kinetic_api;

import javax.annotation.Nullable;
import bqx;
import com.simibubi.kinetic_api.content.contraptions.fluids.VirtualFluid;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluid.PotionFluidAttributes;
import com.simibubi.kinetic_api.content.palettes.AllPaletteBlocks;
import com.simibubi.kinetic_api.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import cut;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class AllFluids {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static RegistryEntry<PotionFluid> POTION =
		REGISTRATE.virtualFluid("potion", PotionFluidAttributes::new, PotionFluid::new)
			.lang(f -> "fluid.kinetic_api.potion", "Potion")
			.register();

	public static RegistryEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
		.lang(f -> "fluid.kinetic_api.tea", "Builder's Tea")
		.tag(AllTags.forgeFluidTag("tea"))
		.register();

	public static RegistryEntry<VirtualFluid> MILK = REGISTRATE.virtualFluid("milk")
		.lang(f -> "fluid.kinetic_api.milk", "Milk")
		.tag(AllTags.forgeFluidTag("milk"))
		.register();

	public static RegistryEntry<ForgeFlowingFluid.Flowing> HONEY =
		REGISTRATE.standardFluid("honey", NoColorFluidAttributes::new)
			.lang(f -> "fluid.kinetic_api.honey", "Honey")
			.attributes(b -> b.viscosity(500)
				.density(1400))
			.properties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.tag(AllTags.forgeFluidTag("honey"))
			.bucket()
			.properties(p -> p.a(1))
			.build()
			.register();

	public static RegistryEntry<ForgeFlowingFluid.Flowing> CHOCOLATE =
		REGISTRATE.standardFluid("chocolate", NoColorFluidAttributes::new)
			.lang(f -> "fluid.kinetic_api.chocolate", "Chocolate")
			.tag(AllTags.forgeFluidTag("chocolate"))
			.attributes(b -> b.viscosity(500)
				.density(1400))
			.properties(p -> p.levelDecreasePerBlock(2)
				.tickRate(25)
				.slopeFindDistance(3)
				.explosionResistance(100f))
			.bucket()
			.properties(p -> p.a(1))
			.build()
			.register();

	// Load this class

	public static void register() {}

	@Environment(EnvType.CLIENT)
	public static void assignRenderLayers() {}

	@Environment(EnvType.CLIENT)
	private static void makeTranslucent(RegistryEntry<? extends ForgeFlowingFluid> entry) {
		ForgeFlowingFluid fluid = entry.get();
		BlockBufferBuilderStorage.setRenderLayer(fluid, VertexConsumerProvider.f());
		BlockBufferBuilderStorage.setRenderLayer(fluid.e(), VertexConsumerProvider.f());
	}

	@Nullable
	public static PistonHandler getLavaInteraction(EmptyFluid fluidState) {
		cut fluid = fluidState.a();
		if (fluid.a(HONEY.get()))
			return fluidState.b() ? AllPaletteBlocks.LIMESTONE.getDefaultState()
				: AllPaletteBlocks.LIMESTONE_VARIANTS.registeredBlocks.get(0)
					.getDefaultState();
		if (fluid.a(CHOCOLATE.get()))
			return fluidState.b() ? AllPaletteBlocks.SCORIA.getDefaultState()
				: AllPaletteBlocks.SCORIA_VARIANTS.registeredBlocks.get(0)
					.getDefaultState();
		return null;
	}

	*
	 * Removing alpha from tint prevents optifine from forcibly applying biome
	 * colors to modded fluids (Makes translucent fluids disappear)
	 *
	private static class NoColorFluidAttributes extends FluidAttributes {

		protected NoColorFluidAttributes(Builder builder, cut fluid) {
			super(builder, fluid);
		}

		@Override
		public int getColor(bqx world, BlockPos pos) {
			return 0x00ffffff;
		}

	}

}
