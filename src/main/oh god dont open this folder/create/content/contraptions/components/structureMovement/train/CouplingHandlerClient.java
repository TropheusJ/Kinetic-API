package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.Random;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.timer.Timer;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.VecHelper;

public class CouplingHandlerClient {

	static ScheduleBuilder selectedCart;
	static Random r = new Random();

	public static void tick() {
		if (selectedCart == null)
			return;
		spawnSelectionParticles(selectedCart.cb(), false);
		FishingParticle player = KeyBinding.B().s;
		ItemCooldownManager heldItemMainhand = player.dC();
		ItemCooldownManager heldItemOffhand = player.dD();
		if (AllItems.MINECART_COUPLING.isIn(heldItemMainhand) || AllItems.MINECART_COUPLING.isIn(heldItemOffhand))
			return;
		selectedCart = null;
	}

	static void onCartClicked(PlayerAbilities player, ScheduleBuilder entity) {
		if (KeyBinding.B().s != player)
			return;
		if (selectedCart == null || selectedCart == entity) {
			selectedCart = entity;
			spawnSelectionParticles(selectedCart.cb(), true);
			return;
		}
		spawnSelectionParticles(entity.cb(), true);
		AllPackets.channel.sendToServer(new CouplingCreationPacket(selectedCart, entity));
		selectedCart = null;
	}

	static void sneakClick() {
		selectedCart = null;
	}

	private static void spawnSelectionParticles(Timer axisAlignedBB, boolean highlight) {
		DragonHeadEntityModel world = KeyBinding.B().r;
		EntityHitResult center = axisAlignedBB.f();
		int amount = highlight ? 100 : 2;
		ParticleEffect particleData = highlight ? ParticleTypes.END_ROD : new DustParticleEffect(1, 1, 1, 1);
		for (int i = 0; i < amount; i++) {
			EntityHitResult v = VecHelper.offsetRandomly(EntityHitResult.a, r, 1);
			double yOffset = v.c;
			v = v.d(1, 0, 1)
				.d()
				.b(0, yOffset / 8f, 0)
				.e(center);
			world.addParticle(particleData, v.entity, v.c, v.d, 0, 0, 0);
		}
	}

}
