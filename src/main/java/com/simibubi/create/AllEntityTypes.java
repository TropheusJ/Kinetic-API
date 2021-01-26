package com.simibubi.create;

import apx;
import com.simibubi.create.content.contraptions.components.actors.SeatEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.ControlledContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueRenderer;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityDimensions.b;
import net.minecraft.entity.LightningEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class AllEntityTypes {

	public static final RegistryEntry<EntityDimensions<OrientedContraptionEntity>> ORIENTED_CONTRAPTION =
		register("contraption", OrientedContraptionEntity::new, LightningEntity.channeler, 5, 3, true,
			AbstractContraptionEntity::build);
	public static final RegistryEntry<EntityDimensions<ControlledContraptionEntity>> CONTROLLED_CONTRAPTION =
		register("stationary_contraption", ControlledContraptionEntity::new, LightningEntity.channeler, 20, 40, false,
			AbstractContraptionEntity::build);
	public static final RegistryEntry<EntityDimensions<SuperGlueEntity>> SUPER_GLUE = register("super_glue",
		SuperGlueEntity::new, LightningEntity.channeler, 10, Integer.MAX_VALUE, false, SuperGlueEntity::build);
	public static final RegistryEntry<EntityDimensions<SeatEntity>> SEAT =
		register("seat", SeatEntity::new, LightningEntity.channeler, 0, Integer.MAX_VALUE, false, SeatEntity::build);

	private static <T extends apx> RegistryEntry<EntityDimensions<T>> register(String name, b<T> factory,
		LightningEntity group, int range, int updateFrequency, boolean sendVelocity,
		NonNullConsumer<EntityDimensions.a<T>> propertyBuilder) {
		String id = Lang.asId(name);
		return Create.registrate()
			.entity(id, factory, group)
			.properties(b -> b.setTrackingRange(range)
				.setUpdateInterval(updateFrequency)
				.setShouldReceiveVelocityUpdates(sendVelocity))
			.properties(propertyBuilder)
			.register();
	}

	public static void register() {}

	@Environment(EnvType.CLIENT)
	public static void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(CONTROLLED_CONTRAPTION.get(),
			ControlledContraptionEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ORIENTED_CONTRAPTION.get(),
			OrientedContraptionEntityRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SUPER_GLUE.get(), SuperGlueRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SEAT.get(), SeatEntity.Render::new);
	}
}
