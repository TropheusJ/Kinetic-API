package com.simibubi.create.content.curiosities.zapper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import afj;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.CreateClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ZapperRenderHandler {

	public static List<LaserBeam> cachedBeams;
	public static float leftHandAnimation;
	public static float rightHandAnimation;
	public static float lastLeftHandAnimation;
	public static float lastRightHandAnimation;

	private static boolean dontReequipLeft;
	private static boolean dontReequipRight;

	public static class LaserBeam {
		float itensity;
		EntityHitResult start;
		EntityHitResult end;
		boolean follow;
		boolean mainHand;

		public LaserBeam(EntityHitResult start, EntityHitResult end) {
			this.start = start;
			this.end = end;
			itensity = 1;
		}

		public LaserBeam followPlayer(boolean follow, boolean mainHand) {
			this.follow = follow;
			this.mainHand = mainHand;
			return this;
		}
	}

	public static EntityHitResult getExactBarrelPos(boolean mainHand) {
		float partialTicks = KeyBinding.B()
			.ai();
		FishingParticle player = KeyBinding.B().s;
		float yaw = (float) ((player.h(partialTicks)) / -180 * Math.PI);
		float pitch = (float) ((player.g(partialTicks)) / -180 * Math.PI);
		boolean rightHand = mainHand == (player.dU() == EquipmentSlot.RIGHT);
		float zOffset = ((float) KeyBinding.B().k.aL - 70) / -100;
		EntityHitResult barrelPosNoTransform = new EntityHitResult(rightHand ? -.35f : .35f, -0.115f, .75f + zOffset);
		EntityHitResult barrelPos = player.j(partialTicks)
			.e(barrelPosNoTransform.a(pitch)
				.b(yaw));
		return barrelPos;
	}

	public static void tick() {
		lastLeftHandAnimation = leftHandAnimation;
		lastRightHandAnimation = rightHandAnimation;
		leftHandAnimation *= 0.8f;
		rightHandAnimation *= 0.8f;
		
		if (cachedBeams == null)
			cachedBeams = new LinkedList<>();
		
		cachedBeams.removeIf(b -> b.itensity < .1f);
		if (cachedBeams.isEmpty())
			return;
		
		cachedBeams.forEach(beam -> {
			CreateClient.outliner.endChasingLine(beam, beam.start, beam.end, 1 - beam.itensity)
			.disableNormals()
			.colored(0xffffff)
			.lineWidth(beam.itensity * 1 / 8f);
		});
		
		cachedBeams.forEach(b -> b.itensity *= .6f);
	}

	public static void shoot(ItemScatterer hand) {
		FishingParticle player = KeyBinding.B().s;
		boolean rightHand = hand == ItemScatterer.RANDOM ^ player.dU() == EquipmentSlot.LEFT;
		if (rightHand) {
			rightHandAnimation = .2f;
			dontReequipRight = false;
		} else {
			leftHandAnimation = .2f;
			dontReequipLeft = false;
		}
		playSound(hand, player.cA());
	}

	public static void playSound(ItemScatterer hand, BlockPos position) {
		float pitch = hand == ItemScatterer.RANDOM ? 2f : 0.9f;
		KeyBinding.B().r.a(position, AllSoundEvents.BLOCKZAPPER_PLACE.get(), SoundEvent.e,
			0.8f, pitch, false);
	}

	public static void addBeam(LaserBeam beam) {
		Random r = new Random();
		double x = beam.end.entity;
		double y = beam.end.c;
		double z = beam.end.d;
		DragonHeadEntityModel world = KeyBinding.B().r;
		Supplier<Double> randomSpeed = () -> (r.nextDouble() - .5d) * .2f;
		Supplier<Double> randomOffset = () -> (r.nextDouble() - .5d) * .2f;
		for (int i = 0; i < 10; i++) {
			world.addParticle(ParticleTypes.END_ROD, x, y, z, randomSpeed.get(), randomSpeed.get(), randomSpeed.get());
			world.addParticle(ParticleTypes.FIREWORK, x + randomOffset.get(), y + randomOffset.get(),
				z + randomOffset.get(), 0, 0, 0);
		}

		cachedBeams.add(beam);
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		ItemCooldownManager heldItem = event.getItemStack();
		if (!(heldItem.b() instanceof ZapperItem))
			return;

		KeyBinding mc = KeyBinding.B();
		boolean rightHand = event.getHand() == ItemScatterer.RANDOM ^ mc.s.dU() == EquipmentSlot.LEFT;

		BufferVertexConsumer ms = event.getMatrixStack();

		ms.a();
		float recoil = rightHand ? afj.g(event.getPartialTicks(), lastRightHandAnimation, rightHandAnimation)
			: afj.g(event.getPartialTicks(), lastLeftHandAnimation, leftHandAnimation);

		float equipProgress = event.getEquipProgress();

		if (rightHand && (rightHandAnimation > .01f || dontReequipRight))
			equipProgress = 0;
		if (!rightHand && (leftHandAnimation > .01f || dontReequipLeft))
			equipProgress = 0;

		// Render arm
		float f = rightHand ? 1.0F : -1.0F;
		float f1 = afj.c(event.getSwingProgress());
		float f2 = -0.3F * afj.a(f1 * (float) Math.PI);
		float f3 = 0.4F * afj.a(f1 * ((float) Math.PI * 2F));
		float f4 = -0.4F * afj.a(event.getSwingProgress() * (float) Math.PI);
		float f5 = afj.a(event.getSwingProgress() * event.getSwingProgress() * (float) Math.PI);
		float f6 = afj.a(f1 * (float) Math.PI);
		
		ms.a(f * (f2 + 0.64000005F - .1f), f3 + -0.4F + equipProgress * -0.6F,
			f4 + -0.71999997F + .3f + recoil);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * 75.0F));
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * f6 * 70.0F));
		ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * f5 * -20.0F));
		SpriteBillboardParticle abstractclientplayerentity = mc.s;
		mc.L()
			.a(abstractclientplayerentity.o());
		ms.a(f * -1.0F, 3.6F, 3.5F);
		ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * 120.0F));
		ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(200.0F));
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * -135.0F));
		ms.a(f * 5.6F, 0.0F, 0.0F);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * 40.0F));
		
		StuckObjectsFeatureRenderer playerrenderer = (StuckObjectsFeatureRenderer) mc.ab()
			.a(abstractclientplayerentity);
		if (rightHand) {
			playerrenderer.a(event.getMatrixStack(), event.getBuffers(), event.getLight(),
				abstractclientplayerentity);
		} else {
			playerrenderer.b(event.getMatrixStack(), event.getBuffers(), event.getLight(),
				abstractclientplayerentity);
		}
		ms.b();

		// Render gun
		ms.a();
		ms.a(f * (f2 + 0.64000005F - .1f), f3 + -0.4F + equipProgress * -0.6F,
			f4 + -0.71999997F - 0.1f + recoil);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * f6 * 70.0F));
		ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(f * f5 * -20.0F));

		ms.a(f * -0.1f, 0.1f, -0.4f);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(f * 5.0F));

		CubeMapRenderer firstPersonRenderer = mc.ad();
		firstPersonRenderer.a(mc.s, heldItem,
			rightHand ? ModelElementTexture.b.e
				: ModelElementTexture.b.d,
			!rightHand, event.getMatrixStack(), event.getBuffers(), event.getLight());
		ms.b();

		event.setCanceled(true);
	}

	public static void dontAnimateItem(ItemScatterer hand) {
		boolean rightHand = hand == ItemScatterer.RANDOM ^ KeyBinding.B().s.dU() == EquipmentSlot.LEFT;
		dontReequipRight |= rightHand;
		dontReequipLeft |= !rightHand;
	}

}
