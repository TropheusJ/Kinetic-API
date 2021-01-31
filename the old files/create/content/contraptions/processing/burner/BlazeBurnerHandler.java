package com.simibubi.kinetic_api.content.contraptions.processing.burner;

import afj;
import com.simibubi.kinetic_api.AllSoundEvents;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerTileEntity.FuelType;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlazeBurnerHandler {

	@SubscribeEvent
	public static void thrownEggsGetEatenByBurner(ProjectileImpactEvent.Throwable event) {
		if (!(event.getThrowable() instanceof SpectralArrowEntity))
			return;

		if (event.getRayTraceResult()
			.c() != Box.a.b)
			return;

		BeehiveBlockEntity tile = event.getThrowable().l.c(new BlockPos(event.getRayTraceResult()
			.e()));
		if (!(tile instanceof BlazeBurnerTileEntity)) {
			return;
		}

		event.setCanceled(true);
		event.getThrowable()
			.f(EntityHitResult.a);
		event.getThrowable()
			.ac();

		GameMode world = event.getThrowable().l;
		if (world.v)
			return;
		
		BlazeBurnerTileEntity heater = (BlazeBurnerTileEntity) tile;
		if (heater.activeFuel != FuelType.SPECIAL) {
			heater.activeFuel = FuelType.NORMAL;
			heater.remainingBurnTime =
				afj.a(heater.remainingBurnTime + 80, 0, BlazeBurnerTileEntity.maxHeatCapacity);
			heater.updateBlockState();
			heater.notifyUpdate();
		}
		
		world.a(null, heater.o(), AllSoundEvents.BLAZE_MUNCH.get(), SoundEvent.e, .5F, 1F);
	}

}
