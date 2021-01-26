package com.simibubi.create.content.contraptions.components.structureMovement;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.content.contraptions.components.structureMovement.sync.ContraptionInteractionPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.CurrentDownParticle;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ContraptionHandlerClient {

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void preventRemotePlayersWalkingAnimations(PlayerTickEvent event) {
		if (event.phase == Phase.START)
			return;
		if (!(event.player instanceof CurrentDownParticle))
			return;
		CurrentDownParticle remotePlayer = (CurrentDownParticle) event.player;
		CompoundTag data = remotePlayer.getPersistentData();
		if (!data.contains("LastOverrideLimbSwingUpdate"))
			return;

		int lastOverride = data.getInt("LastOverrideLimbSwingUpdate");
		data.putInt("LastOverrideLimbSwingUpdate", lastOverride + 1);
		if (lastOverride > 5) {
			data.remove("LastOverrideLimbSwingUpdate");
			data.remove("OverrideLimbSwing");
			return;
		}

		float limbSwing = data.getFloat("OverrideLimbSwing");
		remotePlayer.m = remotePlayer.cC() - (limbSwing / 4);
		remotePlayer.o = remotePlayer.cG();
	}

	@SubscribeEvent
	@Environment(EnvType.CLIENT)
	public static void rightClickingOnContraptionsGetsHandledLocally(ClickInputEvent event) {
		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;
		if (player == null)
			return;
		if (mc.r == null)
			return;
		if (!event.isUseItem())
			return;
		EntityHitResult origin = RaycastHelper.getTraceOrigin(player);

		double reach = mc.q.c();
		if (mc.v != null && mc.v.e() != null)
			reach = Math.min(mc.v.e()
				.f(origin), reach);

		EntityHitResult target = RaycastHelper.getTraceTarget(player, reach, origin);
		for (AbstractContraptionEntity contraptionEntity : mc.r
			.a(AbstractContraptionEntity.class, new Timer(origin, target))) {

			EntityHitResult localOrigin = contraptionEntity.toLocalVector(origin, 1);
			EntityHitResult localTarget = contraptionEntity.toLocalVector(target, 1);
			Contraption contraption = contraptionEntity.getContraption();

			MutableObject<dcg> mutableResult = new MutableObject<>();
			PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
				c blockInfo = contraption.getBlocks()
					.get(p);
				if (blockInfo == null)
					return false;
				PistonHandler state = blockInfo.b;
				VoxelShapes raytraceShape = state.j(KeyBinding.B().r, BlockPos.ORIGIN.down());
				if (raytraceShape.b())
					return false;
				dcg rayTrace = raytraceShape.a(localOrigin, localTarget, p);
				if (rayTrace != null) {
					mutableResult.setValue(rayTrace);
					return true;
				}
				return false;
			});

			if (predicateResult == null || predicateResult.missed())
				return;

			dcg rayTraceResult = mutableResult.getValue();
			ItemScatterer hand = event.getHand();
			Direction face = rayTraceResult.b();
			BlockPos pos = rayTraceResult.a();

			if (!contraptionEntity.handlePlayerInteraction(player, pos, face, hand))
				return;
			AllPackets.channel.sendToServer(new ContraptionInteractionPacket(contraptionEntity, hand, pos, face));
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}

}
