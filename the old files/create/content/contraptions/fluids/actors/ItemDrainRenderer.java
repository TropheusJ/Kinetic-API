package com.simibubi.kinetic_api.content.contraptions.fluids.actors;

import java.util.Random;
import afj;
import apx;
import com.simibubi.kinetic_api.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.kinetic_api.foundation.fluid.FluidRenderer;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ebv;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.fluids.FluidStack;

public class ItemDrainRenderer extends SmartTileEntityRenderer<ItemDrainTileEntity> {

	public ItemDrainRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(ItemDrainTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		renderFluid(te, partialTicks, ms, buffer, light);
		renderItem(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItem(ItemDrainTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		TransportedItemStack transported = te.heldItem;
		if (transported == null)
			return;

		MatrixStacker msr = MatrixStacker.of(ms);
		EntityHitResult itemPosition = VecHelper.getCenterOf(te.o());

		Direction insertedFrom = transported.insertedFrom;
		if (!insertedFrom.getAxis()
			.isHorizontal())
			return;

		ms.a();
		ms.a(.5f, 15 / 16f, .5f);
		msr.nudge(0);
		float offset = afj.g(partialTicks, transported.prevBeltPosition, transported.beltPosition);
		float sideOffset = afj.g(partialTicks, transported.prevSideOffset, transported.sideOffset);

		EntityHitResult offsetVec = EntityHitResult.b(insertedFrom.getOpposite()
			.getVector())
			.a(.5f - offset);
		ms.a(offsetVec.entity, offsetVec.c, offsetVec.d);
		boolean alongX = insertedFrom.rotateYClockwise()
			.getAxis() == Axis.X;
		if (!alongX)
			sideOffset *= -1;
		ms.a(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

		ItemCooldownManager itemStack = transported.stack;
		Random r = new Random(0);
		HorseEntityRenderer itemRenderer = KeyBinding.B()
			.ac();
		int count = (int) (afj.f((int) (itemStack.E()))) / 2;
		boolean renderUpright = BeltHelper.isItemUpright(itemStack);
		boolean blockItem = itemRenderer.a(itemStack, null, null)
			.b();

		if (renderUpright)
			ms.a(0, 3 / 32d, 0);

		int positive = insertedFrom.getDirection()
			.offset();
		float verticalAngle = positive * offset * 360;
		if (insertedFrom.getAxis() != Axis.X)
			msr.rotateX(verticalAngle);
		if (insertedFrom.getAxis() != Axis.Z)
			msr.rotateZ(-verticalAngle);

		if (renderUpright) {
			apx renderViewEntity = KeyBinding.B().t;
			if (renderViewEntity != null) {
				EntityHitResult positionVec = renderViewEntity.cz();
				EntityHitResult vectorForOffset = itemPosition.e(offsetVec);
				EntityHitResult diff = vectorForOffset.d(positionVec);

				if (insertedFrom.getAxis() != Axis.X)
					diff = VecHelper.rotate(diff, verticalAngle, Axis.X);
				if (insertedFrom.getAxis() != Axis.Z)
					diff = VecHelper.rotate(diff, -verticalAngle, Axis.Z);

				float yRot = (float) afj.d(diff.d, -diff.entity);
				ms.a(Vector3f.POSITIVE_Y.getRadialQuaternion((float) (yRot - Math.PI / 2)));
			}
			ms.a(0, 0, -1 / 16f);
		}

		for (int i = 0; i <= count; i++) {
			ms.a();
			if (blockItem)
				ms.a(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
			ms.a(.5f, .5f, .5f);
			if (!blockItem && !renderUpright)
				msr.rotateX(90);
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

	protected void renderFluid(ItemDrainTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light) {
		SmartFluidTankBehaviour tank = te.internalTank;
		if (tank == null)
			return;

		TankSegment primaryTank = tank.getPrimaryTank();
		FluidStack fluidStack = primaryTank.getRenderedFluid();
		float level = primaryTank.getFluidLevel()
			.getValue(partialTicks);

		if (!fluidStack.isEmpty() && level != 0) {
			float yMin = 5f / 16f;
			float min = 2f / 16f;
			float max = min + (12 / 16f);
			float yOffset = (7 / 16f) * level;
			ms.a();
			ms.a(0, yOffset, 0);
			FluidRenderer.renderTiledFluidBB(fluidStack, min, yMin - yOffset, min, max, yMin, max, buffer, ms, light,
				false);
			ms.b();
		}

		ItemCooldownManager heldItemStack = te.getHeldItemStack();
		if (heldItemStack.a())
			return;
		FluidStack fluidStack2 = EmptyingByBasin.emptyItem(te.v(), heldItemStack, true)
			.getFirst();
		if (fluidStack2.isEmpty()) {
			if (fluidStack.isEmpty())
				return;
			fluidStack2 = fluidStack;
		}

		int processingTicks = te.processingTicks;
		float processingPT = te.processingTicks - partialTicks;
		float processingProgress = 1 - (processingPT - 5) / 10;
		processingProgress = afj.a(processingProgress, 0, 1);
		float radius = 0;

		if (processingTicks != -1) {
			radius = (float) (Math.pow(((2 * processingProgress) - 1), 2) - 1);
			Timer bb = new Timer(0.5, 1.0, 0.5, 0.5, 0.25, 0.5).g(radius / 32f);
			FluidRenderer.renderTiledFluidBB(fluidStack2, (float) bb.LOGGER, (float) bb.callback, (float) bb.events,
				(float) bb.eventCounter, (float) bb.eventsByName, (float) bb.f, buffer, ms, light, true);
		}

	}

}
