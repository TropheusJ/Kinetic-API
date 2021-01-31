package com.simibubi.kinetic_api.foundation.data;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.CreateClient;
import com.simibubi.kinetic_api.content.AllSections;
import com.simibubi.kinetic_api.content.contraptions.fluids.VirtualFluid;
import com.simibubi.kinetic_api.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.kinetic_api.foundation.block.IBlockVertexColor;
import com.simibubi.kinetic_api.foundation.block.connected.CTModel;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.kinetic_api.foundation.block.render.CustomRenderedItemModel;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import cut;
import elg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.PistonBlockEntity.c;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.HoeItem;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {

	protected CreateRegistrate(String modid) {
		super(modid);
	}

	public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(
			() -> new CreateRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get()
				.getModEventBus()));
	}

	/* Section Tracking */

	private static Map<RegistryEntry<?>, AllSections> sectionLookup = new IdentityHashMap<>();
	private AllSections section;

	public CreateRegistrate startSection(AllSections section) {
		this.section = section;
		return this;
	}

	public AllSections currentSection() {
		return section;
	}

	@Override
	protected <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name,
		Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
		NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> entryFactory) {
		RegistryEntry<T> ret = super.accept(name, type, builder, creator, entryFactory);
		sectionLookup.put(ret, currentSection());
		return ret;
	}

	public void addToSection(RegistryEntry<?> entry, AllSections section) {
		sectionLookup.put(entry, section);
	}

	public AllSections getSection(RegistryEntry<?> entry) {
		return sectionLookup.getOrDefault(entry, AllSections.UNASSIGNED);
	}

	public AllSections getSection(IForgeRegistryEntry<?> entry) {
		return sectionLookup.entrySet()
			.stream()
			.filter(e -> e.getKey()
				.get() == entry)
			.map(Entry::getValue)
			.findFirst()
			.orElse(AllSections.UNASSIGNED);
	}

	public <R extends IForgeRegistryEntry<R>> Collection<RegistryEntry<R>> getAll(AllSections section,
		Class<? super R> registryType) {
		return this.<R>getAll(registryType)
			.stream()
			.filter(e -> getSection(e) == section)
			.collect(Collectors.toList());
	}

	/* Palettes */

	public <T extends BeetrootsBlock> BlockBuilder<T, CreateRegistrate> baseBlock(String name,
		NonNullFunction<c, T> factory, NonNullSupplier<BeetrootsBlock> propertiesFrom, boolean TFworldGen) {
		return super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
				// TODO tag with forge:stone; if TFWorldGen == true tag with forge:wg_stone
				// aswell
			})
			.simpleItem();
	}

	/* Fluids */

	public <T extends ForgeFlowingFluid> FluidBuilder<T, CreateRegistrate> virtualFluid(String name,
		BiFunction<FluidAttributes.Builder, cut, FluidAttributes> attributesFactory,
		NonNullFunction<ForgeFlowingFluid.Properties, T> factory) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), attributesFactory, factory));
	}

	public FluidBuilder<VirtualFluid, CreateRegistrate> virtualFluid(String name) {
		return entry(name,
			c -> new VirtualFluidBuilder<>(self(), self(), name, c, Create.asResource("fluid/" + name + "_still"),
				Create.asResource("fluid/" + name + "_flow"), null, VirtualFluid::new));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"));
	}

	public FluidBuilder<ForgeFlowingFluid.Flowing, CreateRegistrate> standardFluid(String name,
		NonNullBiFunction<FluidAttributes.Builder, cut, FluidAttributes> attributesFactory) {
		return fluid(name, Create.asResource("fluid/" + name + "_still"), Create.asResource("fluid/" + name + "_flow"),
			attributesFactory);
	}

	/* Util */

	public static <T extends BeetrootsBlock> NonNullConsumer<? super T> connectedTextures(ConnectedTextureBehaviour behavior) {
		return entry -> onClient(() -> () -> registerCTBehviour(entry, behavior));
	}

	public static <T extends BeetrootsBlock> NonNullConsumer<? super T> casingConnectivity(
		BiConsumer<T, CasingConnectivity> consumer) {
		return entry -> onClient(() -> () -> registerCasingConnectivity(entry, consumer));
	}

	public static <T extends BeetrootsBlock> NonNullConsumer<? super T> blockModel(
		Supplier<NonNullFunction<elg, ? extends elg>> func) {
		return entry -> onClient(() -> () -> registerBlockModel(entry, func));
	}

	public static <T extends BeetrootsBlock> NonNullConsumer<? super T> blockColors(Supplier<Supplier<RenderTickCounter>> colorFunc) {
		return entry -> onClient(() -> () -> registerBlockColor(entry, colorFunc));
	}

	public static <T extends BeetrootsBlock> NonNullConsumer<? super T> blockVertexColors(IBlockVertexColor colorFunc) {
		return entry -> onClient(() -> () -> registerBlockVertexColor(entry, colorFunc));
	}

	public static <T extends HoeItem> NonNullConsumer<? super T> itemModel(
		Supplier<NonNullFunction<elg, ? extends elg>> func) {
		return entry -> onClient(() -> () -> registerItemModel(entry, func));
	}

	public static <T extends HoeItem> NonNullConsumer<? super T> itemColors(Supplier<Supplier<BlockColors>> colorFunc) {
		return entry -> onClient(() -> () -> registerItemColor(entry, colorFunc));
	}

	public static <T extends HoeItem, P> NonNullUnaryOperator<ItemBuilder<T, P>> customRenderedItem(
		Supplier<NonNullFunction<elg, ? extends CustomRenderedItemModel>> func) {
		return b -> b.properties(p -> p.setISTER(() -> () -> func.get()
			.apply(null)
			.createRenderer()))
			.onRegister(entry -> onClient(() -> () -> registerCustomRenderedItem(entry, func)));
	}

	protected static void onClient(Supplier<Runnable> toRun) {
		DistExecutor.runWhenOn(Dist.CLIENT, toRun);
	}

	@Environment(EnvType.CLIENT)
	private static void registerCTBehviour(BeetrootsBlock entry, ConnectedTextureBehaviour behavior) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}

	@Environment(EnvType.CLIENT)
	private static <T extends BeetrootsBlock> void registerCasingConnectivity(T entry,
		BiConsumer<T, CasingConnectivity> consumer) {
		consumer.accept(entry, CreateClient.getCasingConnectivity());
	}

	@Environment(EnvType.CLIENT)
	private static void registerBlockModel(BeetrootsBlock entry,
		Supplier<NonNullFunction<elg, ? extends elg>> func) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, func.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerItemModel(HoeItem entry,
		Supplier<NonNullFunction<elg, ? extends elg>> func) {
		CreateClient.getCustomItemModels()
			.register(entry.delegate, func.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerCustomRenderedItem(HoeItem entry,
		Supplier<NonNullFunction<elg, ? extends CustomRenderedItemModel>> func) {
		CreateClient.getCustomRenderedItems()
			.register(entry.delegate, func.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerBlockColor(BeetrootsBlock entry, Supplier<Supplier<RenderTickCounter>> colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc.get()
				.get());
	}

	@Environment(EnvType.CLIENT)
	private static void registerBlockVertexColor(BeetrootsBlock entry, IBlockVertexColor colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc);
	}

	@Environment(EnvType.CLIENT)
	private static void registerItemColor(GameRules entry, Supplier<Supplier<BlockColors>> colorFunc) {
		CreateClient.getColorHandler()
			.register(entry, colorFunc.get()
				.get());
	}

}
