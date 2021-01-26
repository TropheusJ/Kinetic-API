package com.simibubi.create.content.curiosities.symmetry;

import java.util.Random;
import afj;
import bfs;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.curiosities.symmetry.mirror.EmptyMirror;
import com.simibubi.create.content.curiosities.symmetry.mirror.SymmetryMirror;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import ejo;
import elg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BellBlock;
import net.minecraft.client.options.AoMode;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class SymmetryHandler {

	private static int tickCounter = 0;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(EntityPlaceEvent event) {
		if (event.getWorld()
			.s_())
			return;
		if (!(event.getEntity() instanceof PlayerAbilities))
			return;

		PlayerAbilities player = (PlayerAbilities) event.getEntity();
		bfs inv = player.bm;
		for (int i = 0; i < bfs.g(); i++) {
			if (!inv.a(i)
				.a()
				&& inv.a(i)
					.b() == AllItems.WAND_OF_SYMMETRY.get()) {
				SymmetryWandItem.apply(player.l, inv.a(i), player, event.getPos(),
					event.getPlacedBlock());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakEvent event) {
		if (event.getWorld()
			.s_())
			return;

		PlayerAbilities player = event.getPlayer();
		bfs inv = player.bm;
		for (int i = 0; i < bfs.g(); i++) {
			if (!inv.a(i)
				.a() && AllItems.WAND_OF_SYMMETRY.isIn(inv.a(i))) {
				SymmetryWandItem.remove(player.l, inv.a(i), player, event.getPos());
			}
		}
	}

	@Environment(EnvType.CLIENT)
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;

		for (int i = 0; i < bfs.g(); i++) {
			ItemCooldownManager stackInSlot = player.bm.a(i);
			if (!AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot))
				continue;
			if (!SymmetryWandItem.isEnabled(stackInSlot))
				continue;
			SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
			if (mirror instanceof EmptyMirror)
				continue;

			BlockPos pos = new BlockPos(mirror.getPosition());

			float yShift = 0;
			double speed = 1 / 16d;
			yShift = afj.a((float) (AnimationTickHolder.getRenderTick() * speed)) / 5f;

			BackgroundRenderer.FogType buffer = KeyBinding.B()
				.aC()
				.b();
			AoMode info = mc.boundKey.k();
			EntityHitResult view = info.b();

			BufferVertexConsumer ms = event.getMatrixStack();
			ms.a();
			ms.a(-view.getX(), -view.getY(), -view.getZ());
			ms.a(pos.getX(), pos.getY(), pos.getZ());
			ms.a(0, yShift + .2f, 0);
			mirror.applyModelTransform(ms);
			elg model = mirror.getModel()
				.get();
			OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.c());

			mc.aa()
				.b()
				.renderModel(player.l, model, BellBlock.FACING.n(), pos, ms, builder, true,
					player.l.getRandom(), afj.a(pos), ejo.a,
					EmptyModelData.INSTANCE);

			buffer.method_23792();
			ms.b();
		}
	}

	@Environment(EnvType.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START)
			return;
		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;

		if (mc.r == null)
			return;
		if (mc.S())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < bfs.g(); i++) {
				ItemCooldownManager stackInSlot = player.bm.a(i);

				if (stackInSlot != null && AllItems.WAND_OF_SYMMETRY.isIn(stackInSlot)
					&& SymmetryWandItem.isEnabled(stackInSlot)) {

					SymmetryMirror mirror = SymmetryWandItem.getMirror(stackInSlot);
					if (mirror instanceof EmptyMirror)
						continue;

					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					EntityHitResult pos = mirror.getPosition()
						.b(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					EntityHitResult speed = new EntityHitResult(0, r.nextDouble() * 1 / 8f, 0);
					mc.r.addParticle(ParticleTypes.END_ROD, pos.entity, pos.c, pos.d, speed.entity, speed.c, speed.d);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.8f;
		EntityHitResult start = EntityHitResult.b(from).b(0.5, 0.5, 0.5);
		EntityHitResult end = EntityHitResult.b(to).b(0.5, 0.5, 0.5);
		EntityHitResult diff = end.d(start);

		EntityHitResult step = diff.d()
			.a(density);
		int steps = (int) (diff.f() / step.f());

		Random r = new Random();
		for (int i = 3; i < steps - 1; i++) {
			EntityHitResult pos = start.e(step.a(i));
			EntityHitResult speed = new EntityHitResult(0, r.nextDouble() * -40f, 0);

			KeyBinding.B().r.addParticle(new DustParticleEffect(1, 1, 1, 1), pos.entity, pos.c, pos.d,
				speed.entity, speed.c, speed.d);
		}

		EntityHitResult speed = new EntityHitResult(0, r.nextDouble() * 1 / 32f, 0);
		EntityHitResult pos = start.e(step.a(2));
		KeyBinding.B().r.addParticle(ParticleTypes.END_ROD, pos.entity, pos.c, pos.d, speed.entity, speed.c,
			speed.d);

		speed = new EntityHitResult(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.e(step.a(steps));
		KeyBinding.B().r.addParticle(ParticleTypes.END_ROD, pos.entity, pos.c, pos.d, speed.entity, speed.c,
			speed.d);
	}

}
