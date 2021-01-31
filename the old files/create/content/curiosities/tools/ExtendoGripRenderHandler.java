package com.simibubi.kinetic_api.content.curiosities.tools;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.util.ItemScatterer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ExtendoGripRenderHandler {

	public static float mainHandAnimation;
	public static float lastMainHandAnimation;
	public static AllBlockPartials pose = AllBlockPartials.DEPLOYER_HAND_PUNCHING;

	public static void tick() {
		lastMainHandAnimation = mainHandAnimation;
		mainHandAnimation *= afj.a(mainHandAnimation, 0.8f, 0.99f);

		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;
		pose = AllBlockPartials.DEPLOYER_HAND_PUNCHING;
		if (!AllItems.EXTENDO_GRIP.isIn(player.dD()))
			return;
		ItemCooldownManager main = player.dC();
		if (main.a())
			return;
		if (!(main.b() instanceof BannerItem))
			return;
		if (!KeyBinding.B()
			.ac()
			.a(main, null, null)
			.b())
			return;
		pose = AllBlockPartials.DEPLOYER_HAND_HOLDING;
	}

	@SubscribeEvent
	public static void onRenderPlayerHand(RenderHandEvent event) {
		ItemCooldownManager heldItem = event.getItemStack();
		KeyBinding mc = KeyBinding.B();
		FishingParticle player = mc.s;
		boolean rightHand = event.getHand() == ItemScatterer.RANDOM ^ player.dU() == EquipmentSlot.LEFT;

		ItemCooldownManager offhandItem = player.dD();
		boolean notInOffhand = !AllItems.EXTENDO_GRIP.isIn(offhandItem);
		if (notInOffhand && !AllItems.EXTENDO_GRIP.isIn(heldItem))
			return;

		BufferVertexConsumer ms = event.getMatrixStack();
		MatrixStacker msr = MatrixStacker.of(ms);
		SpriteBillboardParticle abstractclientplayerentity = mc.s;
		mc.L()
			.a(abstractclientplayerentity.o());

		float flip = rightHand ? 1.0F : -1.0F;
		float swingProgress = event.getSwingProgress();
		boolean blockItem = heldItem.b() instanceof BannerItem;
		float equipProgress = blockItem ? 0 : event.getEquipProgress() / 4;

		ms.a();
		if (event.getHand() == ItemScatterer.RANDOM) {

			if (1 - swingProgress > mainHandAnimation && swingProgress > 0)
				mainHandAnimation = 0.95f;
			float animation = afj.g(KeyBinding.B()
				.ai(), ExtendoGripRenderHandler.lastMainHandAnimation,
				ExtendoGripRenderHandler.mainHandAnimation);
			animation = animation * animation * animation;

			ms.a(flip * (0.64000005F - .1f), -0.4F + equipProgress * -0.6F, -0.71999997F + .3f);

			ms.a();
			msr.rotateY(flip * 75.0F);
			ms.a(flip * -1.0F, 3.6F, 3.5F);
			msr.rotateZ(flip * 120)
				.rotateX(200)
				.rotateY(flip * -135.0F);
			ms.a(flip * 5.6F, 0.0F, 0.0F);
			msr.rotateY(flip * 40.0F);
			ms.a(flip * 0.05f, -0.3f, -0.3f);

			StuckObjectsFeatureRenderer playerrenderer = (StuckObjectsFeatureRenderer) mc.ab()
				.a(player);
			if (rightHand)
				playerrenderer.a(event.getMatrixStack(), event.getBuffers(), event.getLight(), player);
			else
				playerrenderer.b(event.getMatrixStack(), event.getBuffers(), event.getLight(), player);
			ms.b();

			// Render gun
			ms.a();
			ms.a(flip * -0.1f, 0, -0.3f);
			CubeMapRenderer firstPersonRenderer = mc.ad();
			b transform =
				rightHand ? b.e : b.d;
			firstPersonRenderer.a(mc.s, notInOffhand ? heldItem : offhandItem, transform, !rightHand,
				event.getMatrixStack(), event.getBuffers(), event.getLight());

			if (!notInOffhand) {
				ForgeHooksClient.handleCameraTransforms(ms, mc.ac()
					.a(offhandItem, null, null), transform, !rightHand);
				ms.a(flip * -.05f, .15f, -1.2f);
				ms.a(0, 0, -animation * 2.25f);
				if (blockItem && mc.ac()
					.a(heldItem, null, null)
					.b()) {
					msr.rotateY(flip * 45);
					ms.a(flip * 0.15f, -0.15f, -.05f);
					ms.a(1.25f, 1.25f, 1.25f);
				}

				firstPersonRenderer.a(mc.s, heldItem, transform, !rightHand, event.getMatrixStack(),
					event.getBuffers(), event.getLight());
			}

			ms.b();
		}
		ms.b();
		event.setCanceled(true);
	}

}
