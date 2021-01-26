package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import dcg;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.timer.Timer;

public class LinkRenderer {

	public static void tick() {
		KeyBinding mc = KeyBinding.B();
		Box target = mc.v;
		if (target == null || !(target instanceof dcg))
			return;

		dcg result = (dcg) target;
		DragonHeadEntityModel world = mc.r;
		BlockPos pos = result.a();

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		Text freq1 = Lang.translate("logistics.firstFrequency");
		Text freq2 = Lang.translate("logistics.secondFrequency");

		for (boolean first : Iterate.trueAndFalse) {
			Timer bb = new Timer(EntityHitResult.a, EntityHitResult.a).g(.25f);
			Text label = first ? freq2 : freq1;
			boolean hit = behaviour.testHit(first, target.e());
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

			ValueBox box = new ValueBox(label, bb, pos).withColors(0x601F18, 0xB73C2D)
				.offsetLabel(behaviour.textShift)
				.passive(!hit);
			CreateClient.outliner.showValueBox(Pair.of(Boolean.valueOf(first), pos), box.transform(transform))
				.lineWidth(1 / 64f)
				.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
				.highlightFace(result.b());
		}
	}

	public static void renderOnTileEntity(SmartTileEntity te, float partialTicks, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {

		if (te == null || te.q())
			return;
		LinkBehaviour behaviour = te.getBehaviour(LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		for (boolean first : Iterate.trueAndFalse) {
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
			ItemCooldownManager stack = first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack();

			ms.a();
			transform.transform(te.p(), ms);
			ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
			ms.b();
		}

	}

}
