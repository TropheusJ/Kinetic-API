package com.simibubi.create;

import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateContainer;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterContainer;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterContainer;
import com.simibubi.create.content.logistics.item.filter.FilterScreen;
import com.simibubi.create.content.schematics.block.SchematicTableContainer;
import com.simibubi.create.content.schematics.block.SchematicTableScreen;
import com.simibubi.create.content.schematics.block.SchematicannonContainer;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.simibubi.create.foundation.utility.Lang;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.screen.options.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.options.LanguageOptionsScreen.LanguageSelectionListWidget;
import net.minecraft.item.FoodComponent;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.LecternScreenHandler.a;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.network.IContainerFactory;

public enum AllContainerTypes {

	SCHEMATIC_TABLE(SchematicTableContainer::new),
	SCHEMATICANNON(SchematicannonContainer::new),
	FLEXCRATE(AdjustableCrateContainer::new),
	FILTER(FilterContainer::new),
	ATTRIBUTE_FILTER(AttributeFilterContainer::new),

	;

	public LecternScreenHandler<? extends FoodComponent> type;
	private a<?> factory;

	private <C extends FoodComponent> AllContainerTypes(IContainerFactory<C> factory) {
		this.factory = factory;
	}

	public static void register(RegistryEvent.Register<LecternScreenHandler<?>> event) {
		for (AllContainerTypes container : values()) {
			container.type = new LecternScreenHandler<>(container.factory)
					.setRegistryName(new Identifier(Create.ID, Lang.asId(container.name())));
			event.getRegistry().register(container.type);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void registerScreenFactories() {
		bind(SCHEMATIC_TABLE, SchematicTableScreen::new);
		bind(SCHEMATICANNON, SchematicannonScreen::new);
		bind(FLEXCRATE, AdjustableCrateScreen::new);
		bind(FILTER, FilterScreen::new);
		bind(ATTRIBUTE_FILTER, AttributeFilterScreen::new);
	}

	@Environment(EnvType.CLIENT)
	@SuppressWarnings("unchecked")
	private static <C extends FoodComponent, S extends PresetsScreen & JigsawBlockScreen<C>> void bind(AllContainerTypes c,
			LanguageSelectionListWidget<C, S> factory) {
		LanguageOptionsScreen.a((LecternScreenHandler<C>) c.type, factory);
	}

}
