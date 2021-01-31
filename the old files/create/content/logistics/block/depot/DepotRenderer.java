package com.simibubi.kinetic_api.content.logistics.block.depot;

import java.util.Random;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction.Axis;
import afj;
import apx;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ebv;

public class DepotRenderer extends SafeTileEntityRenderer<DepotTileEntity> {

	public DepotRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(DepotTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {

		TransportedItemStack transported = te.heldItem;
		MatrixStacker msr = MatrixStacker.of(ms);
		EntityHitResult itemPosition = VecHelper.getCenterOf(te.o());

		ms.a();
		ms.a(.5f, 15 / 16f, .5f);

		// Render main item
		if (transported != null) {
			ms.a();
			msr.nudge(0);
			float offset = afj.g(partialTicks, transported.prevBeltPosition, transported.beltPosition);
			float sideOffset = afj.g(partialTicks, transported.prevSideOffset, transported.sideOffset);

			if (transported.insertedFrom.getAxis()
				.isHorizontal()) {
				EntityHitResult offsetVec = EntityHitResult.b(transported.insertedFrom.getOpposite()
					.getVector())
					.a(.5f - offset);
				ms.a(offsetVec.entity, offsetVec.c, offsetVec.d);
				boolean alongX = transported.insertedFrom.rotateYClockwise()
					.getAxis() == Axis.X;
				if (!alongX)
					sideOffset *= -1;
				ms.a(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);
			}

			ItemCooldownManager itemStack = transported.stack;
			int angle = transported.angle;
			Random r = new Random(0);
			renderItem(ms, buffer, light, overlay, itemStack, angle, r, itemPosition);
			ms.b();
		}

		// Render output items
		for (int i = 0; i < te.processingOutputBuffer.getSlots(); i++) {
			ItemCooldownManager stack = te.processingOutputBuffer.getStackInSlot(i);
			if (stack.a())
				continue;
			ms.a();
			msr.nudge(i);

			boolean renderUpright = BeltHelper.isItemUpright(stack);
			msr.rotateY(360 / 8f * i);
			ms.a(.35f, 0, 0);
			if (renderUpright)
				msr.rotateY(-(360 / 8f * i));
			Random r = new Random(i + 1);
			int angle = (int) (360 * r.nextFloat());
			renderItem(ms, buffer, light, overlay, stack, renderUpright ? angle + 90 : angle, r, itemPosition);
			ms.b();
		}

		ms.b();
	}

	public static void renderItem(BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay, ItemCooldownManager itemStack,
		int angle, Random r, EntityHitResult itemPosition) {
		HorseEntityRenderer itemRenderer = KeyBinding.B()
			.ac();
		MatrixStacker msr = MatrixStacker.of(ms);
		int count = (int) (afj.f((int) (itemStack.E()))) / 2;
		boolean renderUpright = BeltHelper.isItemUpright(itemStack);
		boolean blockItem = itemRenderer.a(itemStack, null, null)
			.b();

		ms.a();
		msr.rotateY(angle);

		if (renderUpright) {
			apx renderViewEntity = KeyBinding.B().t;
			if (renderViewEntity != null) {
				EntityHitResult positionVec = renderViewEntity.cz();
				EntityHitResult vectorForOffset = itemPosition;
				EntityHitResult diff = vectorForOffset.d(positionVec);
				float yRot = (float) afj.d(diff.d, -diff.entity);
				ms.a(Vector3f.POSITIVE_Y.getRadialQuaternion((float) (yRot - Math.PI / 2)));
			}
			ms.a(0, 3 / 32d, 1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.a();
			if (blockItem)
				ms.a(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			ms.a(.5f, .5f, .5f);
			if (!blockItem && !renderUpright) {
				ms.a(0, -3 / 16f, 0);
				msr.rotateX(90);
			}
			itemRenderer.a(itemStack, b.i, light, overlay, ms, buffer);
			ms.b();

			if (!renderUpright) {
				if (!blockItem)
					msr.rotateY(10);
				ms.a(0, blockItem ? 1 / 64d : 1 / 16d, 0);
			} else
				ms.a(0, 0, -1 / 16f);
		}

		ms.b();
	}

}
