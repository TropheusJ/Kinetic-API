package com.simibubi.kinetic_api.content.contraptions.components.deployer;

import java.util.OptionalInt;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import apx;
import com.mojang.authlib.GameProfile;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.config.CKinetics;
import com.simibubi.kinetic_api.foundation.utility.Lang;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DeployerFakePlayer extends FakePlayer {

	private static final ClientConnection NETWORK_MANAGER = new ClientConnection(NetworkSide.CLIENTBOUND);
	public static final GameProfile DEPLOYER_PROFILE =
		new GameProfile(UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d"), "Deployer");
	Pair<BlockPos, Float> blockBreakingProgress;
	ItemCooldownManager spawnedItemEffects;

	public DeployerFakePlayer(ServerWorld world) {
		super(world, DEPLOYER_PROFILE);
		networkHandler = new FakePlayNetHandler(world.l(), this);
	}

	@Override
	public OptionalInt a(ActionResult container) {
		return OptionalInt.empty();
	}

	@Override
	public Text d() {
		return Lang.translate("block.deployer.damage_source_name");
	}

	@Override
	@Environment(EnvType.CLIENT)
	public float e(PathAwareEntity poseIn) {
		return 0;
	}

	@Override
	public EntityHitResult cz() {
		return new EntityHitResult(cC(), cD(), cG());
	}

	@Override
	public float eQ() {
		return 1 / 64f;
	}

	@Override
	public boolean q(boolean ignoreHunger) {
		return false;
	}

	@Override
	public ItemCooldownManager a(GameMode world, ItemCooldownManager stack) {
		return stack;
	}

	@SubscribeEvent
	public static void deployerHasEyesOnHisFeet(EntityEvent.Size event) {
		if (event.getEntity() instanceof DeployerFakePlayer)
			event.setNewEyeHeight(0);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void deployerCollectsDropsFromKilledEntities(LivingDropsEvent event) {
		if (!(event.getSource() instanceof DamageUtil))
			return;
		DamageUtil source = (DamageUtil) event.getSource();
		apx trueSource = source.k();
		if (trueSource != null && trueSource instanceof DeployerFakePlayer) {
			DeployerFakePlayer fakePlayer = (DeployerFakePlayer) trueSource;
			event.getDrops()
				.forEach(stack -> fakePlayer.bm.a(trueSource.l, stack.g()));
			event.setCanceled(true);
		}
	}

	@Override
	protected void b(ItemCooldownManager p_184606_1_) {}

	@Override
	public void remove(boolean keepData) {
		if (blockBreakingProgress != null && !l.v)
			l.a(X(), blockBreakingProgress.getKey(), -1);
		super.remove(keepData);
	}

	@SubscribeEvent
	public static void deployerKillsDoNotSpawnXP(LivingExperienceDropEvent event) {
		if (event.getAttackingPlayer() instanceof DeployerFakePlayer)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void entitiesDontRetaliate(LivingSetAttackTargetEvent event) {
		if (!(event.getTarget() instanceof DeployerFakePlayer))
			return;
		SaddledComponent entityLiving = event.getEntityLiving();
		if (!(entityLiving instanceof ItemSteerable))
			return;
		ItemSteerable mob = (ItemSteerable) entityLiving;

		CKinetics.DeployerAggroSetting setting = AllConfigs.SERVER.kinetics.ignoreDeployerAttacks.get();

		switch (setting) {
		case ALL:
			mob.h(null);
			break;
		case CREEPERS:
			if (mob instanceof AbstractSkeletonEntity)
				mob.h(null);
			break;
		case NONE:
		default:
		}
	}

	private static class FakePlayNetHandler extends ServerPlayNetworkHandler {
		public FakePlayNetHandler(MinecraftServer server, ServerPlayerEntity playerIn) {
			super(server, NETWORK_MANAGER, playerIn);
		}

		@Override
		public void sendPacket(Packet<?> packetIn) {}

		@Override
		public void sendPacket(Packet<?> packetIn,
			GenericFutureListener<? extends Future<? super Void>> futureListeners) {}
	}

}
