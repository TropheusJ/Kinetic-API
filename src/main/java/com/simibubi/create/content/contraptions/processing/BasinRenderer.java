package com.simibubi.create.content.contraptions.processing;

import java.util.Random;
import afj;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.tileEntity.renderer.SmartTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.VecHelper;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class BasinRenderer extends SmartTileEntityRenderer<BasinTileEntity> {

	public BasinRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BasinTileEntity basin, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(basin, partialTicks, ms, buffer, light, overlay);

		float fluidLevel = renderFluids(basin, partialTicks, ms, buffer, light, overlay);
		float level = afj.a(fluidLevel - .3f, .125f, .6f);

		ms.a();

		BlockPos pos = basin.o();
		ms.a(.5, .2f, .5);
		MatrixStacker.of(ms)
			.rotateY(basin.ingredientRotation.getValue(partialTicks));

		Random r = new Random(pos.hashCode());
		EntityHitResult baseVector = new EntityHitResult(.125, level, 0);

		IItemHandlerModifiable inv = basin.itemCapability.orElse(new ItemStackHandler());
		int itemCount = 0;
		for (int slot = 0; slot < inv.getSlots(); slot++)
			if (!inv.getStackInSlot(slot)
				.a())
				itemCount++;

		if (itemCount == 1)
			baseVector = new EntityHitResult(0, level, 0);

		float anglePartition = 360f / itemCount;
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemCooldownManager stack = inv.getStackInSlot(slot);
			if (stack.a())
				continue;

			ms.a();

			if (fluidLevel > 0) {
				ms.a(0,
					(afj.a(AnimationTickHolder.getRenderTick() / 12f + anglePartition * itemCount) + 1.5f) * 1
						/ 32f,
					0);
			}

			EntityHitResult itemPosition = VecHelper.rotate(baseVector, anglePartition * itemCount, Axis.Y);
			ms.a(itemPosition.entity, itemPosition.c, itemPosition.d);
			MatrixStacker.of(ms)
				.rotateY(anglePartition * itemCount + 35)
				.rotateX(65);

			for (int i = 0; i <= stack.E() / 8; i++) {
				ms.a();
				
				EntityHitResult vec = VecHelper.offsetRandomly(EntityHitResult.a, r, 1 / 16f);
				
				ms.a(vec.entity, vec.c, vec.d);
				renderItem(ms, buffer, light, overlay, stack);
				ms.b();
			}
			ms.b();

			itemCount--;
		}
		ms.b();

		PistonHandler blockState = basin.p();
		if (!(blockState.b() instanceof BasinBlock))
			return;
		Direction direction = blockState.c(BasinBlock.FACING);
		if (direction == Direction.DOWN)
			return;
		EntityHitResult directionVec = EntityHitResult.b(direction.getVector());
		EntityHitResult outVec = VecHelper.getCenterOf(BlockPos.ORIGIN)
			.e(directionVec.a(.55)
				.a(0, 1 / 2f, 0));

		boolean outToBasin = basin.v()
			.d_(basin.o()
				.offset(direction))
			.b() instanceof BasinBlock;
		
		for (IntAttached<ItemCooldownManager> intAttached : basin.visualizedOutputItems) {
			float progress = 1 - (intAttached.getFirst() - partialTicks) / BasinTileEntity.OUTPUT_ANIMATION_TIME;
			
			if (!outToBasin && progress > .35f)
				continue;
			
			ms.a();
			MatrixStacker.of(ms)
				.translate(outVec)
				.translate(new EntityHitResult(0, Math.max(-.55f, -(progress * progress * 2)), 0))
				.translate(directionVec.a(progress * .5f))
				.rotateY(AngleHelper.horizontalAngle(direction))
				.rotateX(progress * 180);
			renderItem(ms, buffer, light, overlay, intAttached.getValue());
			ms.b();
		}
	}

	protected void renderItem(BufferVertexConsumer ms, BackgroundRenderer buffer, int light, int overlay, ItemCooldownManager stack) {
		KeyBinding.B()
			.ac()
			.a(stack, b.h, light, overlay, ms, buffer);
	}

	protected float renderFluids(BasinTileEntity basin, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		SmartFluidTankBehaviour inputFluids = basin.getBehaviour(SmartFluidTankBehaviour.INPUT);
		SmartFluidTankBehaviour outputFluids = basin.getBehaviour(SmartFluidTankBehaviour.OUTPUT);
		SmartFluidTankBehaviour[] tanks = { inputFluids, outputFluids };
		float totalUnits = basin.getTotalFluidUnits(partialTicks);
		if (totalUnits < 1)
			return 0;

		float fluidLevel = afj.a(totalUnits / 2000, 0, 1);

		float xMin = 2 / 16f;
		float xMax = 2 / 16f;
		final float yMin = 2 / 16f;
		final float yMax = yMin + 12 / 16f * fluidLevel;
		final float zMin = 2 / 16f;
		final float zMax = 14 / 16f;

		for (SmartFluidTankBehaviour behaviour : tanks) {
			if (behaviour == null)
				continue;
			for (TankSegment tankSegment : behaviour.getTanks()) {
				FluidStack renderedFluid = tankSegment.getRenderedFluid();
				if (renderedFluid.isEmpty())
					continue;
				float units = tankSegment.getTotalUnits(partialTicks);
				if (units < 1)
					continue;

				float partial = afj.a(units / totalUnits, 0, 1);
				xMax += partial * 12 / 16f;
				FluidRenderer.renderTiledFluidBB(renderedFluid, xMin, yMin, zMin, xMax, yMax, zMax, buffer, ms, light,
					false);

				xMin = xMax;
			}
		}

		return yMax;
	}

}
