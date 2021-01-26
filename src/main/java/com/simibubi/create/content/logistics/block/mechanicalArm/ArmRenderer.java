package com.simibubi.create.content.logistics.block.mechanicalArm;

import afj;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.logistics.block.mechanicalArm.ArmTileEntity.Phase;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;

public class ArmRenderer extends KineticTileEntityRenderer {

	public ArmRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float pt, BufferVertexConsumer ms, BackgroundRenderer buffer, int light,
		int overlay) {
		super.renderSafe(te, pt, ms, buffer, light, overlay);
		ArmTileEntity arm = (ArmTileEntity) te;
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.c());
		PistonHandler blockState = te.p();
		MatrixStacker msr = MatrixStacker.of(ms);
		int color = 0xFFFFFF;

		float baseAngle = arm.baseAngle.get(pt);
		float lowerArmAngle = arm.lowerArmAngle.get(pt) - 135;
		float upperArmAngle = arm.upperArmAngle.get(pt) - 90;
		float headAngle = arm.headAngle.get(pt);
		
		boolean rave = te instanceof ArmTileEntity && ((ArmTileEntity) te).phase == Phase.DANCING;
		float renderTick = AnimationTickHolder.getRenderTick() + (te.hashCode() % 64);
		if (rave) {
			baseAngle = (renderTick * 10) % 360;
			lowerArmAngle = afj.g((afj.a(renderTick / 4) + 1) / 2, -45, 15);
			upperArmAngle = afj.g((afj.a(renderTick / 8) + 1) / 4, -45, 95);
			headAngle = -lowerArmAngle;
			color = ColorHelper.rainbowColor(AnimationTickHolder.ticks * 100);
		}
		
		ms.a();

		SuperByteBuffer base = AllBlockPartials.ARM_BASE.renderOn(blockState).light(light);
		SuperByteBuffer lowerBody = AllBlockPartials.ARM_LOWER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer upperBody = AllBlockPartials.ARM_UPPER_BODY.renderOn(blockState).light(light);
		SuperByteBuffer head = AllBlockPartials.ARM_HEAD.renderOn(blockState).light(light);
		SuperByteBuffer claw = AllBlockPartials.ARM_CLAW_BASE.renderOn(blockState).light(light);
		SuperByteBuffer clawGrip = AllBlockPartials.ARM_CLAW_GRIP.renderOn(blockState);

		msr.centre();
		
		if (blockState.c(ArmBlock.CEILING))
			msr.rotateX(180);

		ms.a(0, 4 / 16d, 0);
		msr.rotateY(baseAngle);
		base.renderInto(ms, builder);

		ms.a(0, 1 / 16d, -2 / 16d);
		msr.rotateX(lowerArmAngle);
		ms.a(0, -1 / 16d, 0);
		lowerBody.color(color)
			.renderInto(ms, builder);

		ms.a(0, 12 / 16d, 12 / 16d);
		msr.rotateX(upperArmAngle);
		upperBody.color(color)
			.renderInto(ms, builder);

		ms.a(0, 11 / 16d, -11 / 16d);
		msr.rotateX(headAngle);
		head.renderInto(ms, builder);

		ms.a(0, 0, -4 / 16d);
		claw.renderInto(ms, builder);
		ItemCooldownManager item = arm.heldItem;
		HorseEntityRenderer itemRenderer = KeyBinding.B()
			.ac();
		boolean hasItem = !item.a();
		boolean isBlockItem = hasItem && (item.b() instanceof BannerItem)
			&& itemRenderer.a(item, KeyBinding.B().r, null)
				.b();
		
		for (int flip : Iterate.positiveAndNegative) {
			ms.a();
			ms.a(0, flip * 3 / 16d, -1 / 16d);
			msr.rotateX(flip * (hasItem ? isBlockItem ? 0 : -35 : 0));
			clawGrip.light(light).renderInto(ms, builder);
			ms.b();
		}

		if (hasItem) {
			float itemScale = isBlockItem ? .5f : .625f;
			msr.rotateX(90);
			ms.a(0, -4 / 16f, 0);
			ms.a(itemScale, itemScale, itemScale);
			itemRenderer
				.a(item, b.i, light, overlay, ms, buffer);
		}

		ms.b();
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.ARM_COG.renderOn(te.p());
	}

}
