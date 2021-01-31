package com.simibubi.kinetic_api.content.curiosities.tools;

import java.util.UUID;
import apx;
import bcm;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.networking.AllPackets;
import dcg;
import dch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ExtendoGripItem extends HoeItem {
	private static DamageRecord lastActiveDamageSource;

	static NetworkUtils<Multimap<SpawnRestriction, EntityAttribute>> rangeModifier = 
		new NetworkUtils<Multimap<SpawnRestriction, EntityAttribute>>(() -> 
			// Holding an ExtendoGrip
			ImmutableMultimap.of(
				ForgeMod.REACH_DISTANCE.get(),
				new EntityAttribute(UUID.fromString("7f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 3,
					EntityAttribute.a.a))
		);

	static NetworkUtils<Multimap<SpawnRestriction, EntityAttribute>> doubleRangeModifier = 
		new NetworkUtils<Multimap<SpawnRestriction, EntityAttribute>>(() -> 
			// Holding two ExtendoGrips o.O
			ImmutableMultimap.of(
				ForgeMod.REACH_DISTANCE.get(),
				new EntityAttribute(UUID.fromString("8f7dbdb2-0d0d-458a-aa40-ac7633691f66"), "Range modifier", 5,
					EntityAttribute.a.a))
		);

	public ExtendoGripItem(a properties) {
		super(properties.a(1)
			.a(SkullItem.b));
	}

	@SubscribeEvent
	public static void holdingExtendoGripIncreasesRange(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof PlayerAbilities))
			return;

		PlayerAbilities player = (PlayerAbilities) event.getEntityLiving();
		String marker = "createExtendo";
		String dualMarker = "createDualExtendo";

		CompoundTag persistentData = player.getPersistentData();
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.dD());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.dC());
		boolean holdingDualExtendo = inOff && inMain;
		boolean holdingExtendo = inOff ^ inMain;
		holdingExtendo &= !holdingDualExtendo;
		boolean wasHoldingExtendo = persistentData.contains(marker);
		boolean wasHoldingDualExtendo = persistentData.contains(dualMarker);

		if (holdingExtendo != wasHoldingExtendo) {
			if (!holdingExtendo) {
				player.dA().a(rangeModifier.a());
				persistentData.remove(marker);
			} else {
				if (player instanceof ServerPlayerEntity)
					AllTriggers.EXTENDO.trigger((ServerPlayerEntity) player);
				player.dA()
					.b(rangeModifier.a());
				persistentData.putBoolean(marker, true);
			}
		}

		if (holdingDualExtendo != wasHoldingDualExtendo) {
			if (!holdingDualExtendo) {
				player.dA()
					.a(doubleRangeModifier.a());
				persistentData.remove(dualMarker);
			} else {
				if (player instanceof ServerPlayerEntity)
					AllTriggers.GIGA_EXTENDO.trigger((ServerPlayerEntity) player);
				player.dA()
					.b(doubleRangeModifier.a());
				persistentData.putBoolean(dualMarker, true);
			}
		}

	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void dontMissEntitiesWhenYouHaveHighReachDistance(ClickInputEvent event) {
		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;
		if (mc.r == null || player == null)
			return;
		if (!isHoldingExtendoGrip(player))
			return;
		if (mc.v instanceof dcg && mc.v.c() != net.minecraft.util.math.Box.a.a)
			return;

		// Modified version of GameRenderer#getMouseOver
		double d0 = player.a(ForgeMod.REACH_DISTANCE.get())
			.f();
		if (!player.b_())
			d0 -= 0.5f;
		EntityHitResult Vector3d = player.j(mc.ai());
		EntityHitResult Vector3d1 = player.f(1.0F);
		EntityHitResult Vector3d2 = Vector3d.b(Vector3d1.entity * d0, Vector3d1.c * d0, Vector3d1.d * d0);
		Timer axisalignedbb = player.cb()
			.b(Vector3d1.a(d0))
			.c(1.0D, 1.0D, 1.0D);
		dch entityraytraceresult =
			FireballEntity.a(player, Vector3d, Vector3d2, axisalignedbb, (e) -> {
				return !e.a_() && e.aS();
			}, d0 * d0);
		if (entityraytraceresult != null) {
			apx entity1 = entityraytraceresult.a();
			EntityHitResult Vector3d3 = entityraytraceresult.e();
			double d2 = Vector3d.g(Vector3d3);
			if (d2 < d0 * d0 || mc.v == null || mc.v.c() == net.minecraft.util.math.Box.a.a) {
				mc.v = entityraytraceresult;
				if (entity1 instanceof SaddledComponent || entity1 instanceof bcm)
					mc.u = entity1;
			}
		}
	}

	@SubscribeEvent
	public static void bufferLivingAttackEvent(LivingAttackEvent event) {
		// Workaround for removed patch to get the attacking entity. Tbf this is a hack and a half, but it should work.
		lastActiveDamageSource = event.getSource();
	}

	@SubscribeEvent
	public static void attacksByExtendoGripHaveMoreKnockback(LivingKnockBackEvent event) {
		if (lastActiveDamageSource == null)
			return;
		apx entity = lastActiveDamageSource.j();
		if (!(entity instanceof PlayerAbilities))
			return;
		PlayerAbilities player = (PlayerAbilities) entity;
		if (!isHoldingExtendoGrip(player))
			return;
		event.setStrength(event.getStrength() + 2);
	}

	private static boolean isUncaughtClientInteraction(apx entity, apx target) {
		// Server ignores entity interaction further than 6m
		if (entity.h(target) < 36)
			return false;
		if (!entity.l.v)
			return false;
		if (!(entity instanceof PlayerAbilities))
			return false;
		return true;
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
		apx entity = event.getEntity();
		apx target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerAbilities player = (PlayerAbilities) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target));
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
		apx entity = event.getEntity();
		apx target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerAbilities player = (PlayerAbilities) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand()));
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
		apx entity = event.getEntity();
		apx target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		PlayerAbilities player = (PlayerAbilities) entity;
		if (isHoldingExtendoGrip(player))
			AllPackets.channel
				.sendToServer(new ExtendoGripInteractionPacket(target, event.getHand(), event.getLocalPos()));
	}

	public static boolean isHoldingExtendoGrip(PlayerAbilities player) {
		boolean inOff = AllItems.EXTENDO_GRIP.isIn(player.dD());
		boolean inMain = AllItems.EXTENDO_GRIP.isIn(player.dC());
		boolean holdingGrip = inOff || inMain;
		return holdingGrip;
	}

}
