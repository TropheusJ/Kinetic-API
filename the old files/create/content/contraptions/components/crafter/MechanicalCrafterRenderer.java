package com.simibubi.kinetic_api.content.contraptions.components.crafter;

import static com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer.standardKineticRotationTransform;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllSpriteShifts;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCrafterTileEntity.Phase;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.RecipeGridHandler.GroupedItems;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class MechanicalCrafterRenderer extends SafeTileEntityRenderer<MechanicalCrafterTileEntity> {

	public MechanicalCrafterRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(MechanicalCrafterTileEntity te, float partialTicks, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		ms.a();
		Direction facing = te.p()
			.c(HORIZONTAL_FACING);
		EntityHitResult vec = EntityHitResult.b(facing.getVector()).a(.58)
			.b(.5, .5, .5);

		if (te.phase == Phase.EXPORTING) {
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(te.p());
			float progress =
				afj.a((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
			vec = vec.e(EntityHitResult.b(targetDirection.getVector()).a(progress * .75f));
		}

		ms.a(vec.entity, vec.c, vec.d);
		ms.a(1 / 2f, 1 / 2f, 1 / 2f);
		float yRot = AngleHelper.horizontalAngle(facing);
		ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(yRot));
		renderItems(te, partialTicks, ms, buffer, light, overlay);
		ms.b();

		renderFast(te, partialTicks, ms, buffer, light);
	}

	public void renderItems(MechanicalCrafterTileEntity te, float partialTicks, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light, int overlay) {
		if (te.phase == Phase.IDLE) {
			ItemCooldownManager stack = te.getInventory().a(0);
			if (!stack.a()) {
				ms.a();
				ms.a(0, 0, -1 / 256f);
				KeyBinding.B()
					.ac()
					.a(stack, b.i, light, overlay, ms, buffer);
				ms.b();
			}
		} else {
			// render grouped items
			GroupedItems items = te.groupedItems;
			float distance = .5f;

			ms.a();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItemsBeforeCraft;
				items.calcStats();
				float progress =
					afj.a((2000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = afj.a(progress * 2, 0, 1);
				float lateProgress = afj.a(progress * 2 - 1, 0, 1);

				ms.a(1 - lateProgress, 1 - lateProgress, 1 - lateProgress);
				EntityHitResult centering =
					new EntityHitResult(-items.minX + (-items.width + 1) / 2f, -items.minY + (-items.height + 1) / 2f, 0)
						.a(earlyProgress);
				ms.a(centering.entity * .5f, centering.c * .5f, 0);
				distance += (-4 * (progress - .5f) * (progress - .5f) + 1) * .25f;
			}

			boolean onlyRenderFirst = te.phase == Phase.INSERTING || te.phase == Phase.CRAFTING && te.countDown < 1000;
			final float spacing = distance;
			items.grid.forEach((pair, stack) -> {
				if (onlyRenderFirst && (pair.getLeft()
					.intValue() != 0
					|| pair.getRight()
						.intValue() != 0))
					return;

				ms.a();
				Integer x = pair.getKey();
				Integer y = pair.getValue();
				ms.a(x * spacing, y * spacing, 0);
				MatrixStacker.of(ms)
					.nudge(x * 13 + y + te.o()
						.hashCode());
				KeyBinding.B()
					.ac()
					.a(stack, b.i, light, overlay, ms, buffer);
				ms.b();
			});

			ms.b();

			if (te.phase == Phase.CRAFTING) {
				items = te.groupedItems;
				float progress =
					afj.a((1000 - te.countDown + te.getCountDownSpeed() * partialTicks) / 1000f, 0, 1);
				float earlyProgress = afj.a(progress * 2, 0, 1);
				float lateProgress = afj.a(progress * 2 - 1, 0, 1);

				ms.a(Vector3f.POSITIVE_Z.getDegreesQuaternion(earlyProgress * 2 * 360));
				float upScaling = earlyProgress * 1.125f;
				float downScaling = 1 + (1 - lateProgress) * .125f;
				ms.a(upScaling, upScaling, upScaling);
				ms.a(downScaling, downScaling, downScaling);

				items.grid.forEach((pair, stack) -> {
					if (pair.getLeft()
						.intValue() != 0
						|| pair.getRight()
							.intValue() != 0)
						return;
					KeyBinding.B()
						.ac()
						.a(stack, b.i, light, overlay, ms, buffer);
				});
			}

		}
	}

	public void renderFast(MechanicalCrafterTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light) {
		PistonHandler blockState = te.p();
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		SuperByteBuffer superBuffer = AllBlockPartials.SHAFTLESS_COGWHEEL.renderOn(blockState);
		standardKineticRotationTransform(superBuffer, te, light);
		superBuffer.rotateCentered(Direction.UP, (float) (blockState.c(HORIZONTAL_FACING)
			.getAxis() != Axis.X ? 0 : Math.PI / 2));
		superBuffer.rotateCentered(Direction.EAST, (float) (Math.PI / 2));
		superBuffer.renderInto(ms, vb);

		Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(blockState);
		BlockPos pos = te.o();

		if ((te.covered || te.phase != Phase.IDLE) && te.phase != Phase.CRAFTING && te.phase != Phase.INSERTING) {
			SuperByteBuffer lidBuffer =
				renderAndTransform(te, AllBlockPartials.MECHANICAL_CRAFTER_LID, blockState, pos);
			lidBuffer.renderInto(ms, vb);
		}

		if (MechanicalCrafterBlock.isValidTarget(te.v(), pos.offset(targetDirection), blockState)) {
			SuperByteBuffer beltBuffer =
				renderAndTransform(te, AllBlockPartials.MECHANICAL_CRAFTER_BELT, blockState, pos);
			SuperByteBuffer beltFrameBuffer =
				renderAndTransform(te, AllBlockPartials.MECHANICAL_CRAFTER_BELT_FRAME, blockState, pos);

			if (te.phase == Phase.EXPORTING) {
				int textureIndex = (int) ((te.getCountDownSpeed() / 128f * AnimationTickHolder.ticks));
				beltBuffer.shiftUVtoSheet(AllSpriteShifts.CRAFTER_THINGIES, (textureIndex % 4) / 4f, 0, 1);
			} 

			beltBuffer.renderInto(ms, vb);
			beltFrameBuffer.renderInto(ms, vb);

		} else {
			SuperByteBuffer arrowBuffer =
				renderAndTransform(te, AllBlockPartials.MECHANICAL_CRAFTER_ARROW, blockState, pos);
			arrowBuffer.renderInto(ms, vb);
		}

	}

	private SuperByteBuffer renderAndTransform(MechanicalCrafterTileEntity te, AllBlockPartials renderBlock,
		PistonHandler crafterState, BlockPos pos) {
		SuperByteBuffer buffer = renderBlock.renderOn(crafterState);
		float xRot = crafterState.c(MechanicalCrafterBlock.POINTING)
			.getXRotation();
		float yRot = AngleHelper.horizontalAngle(crafterState.c(HORIZONTAL_FACING));
		buffer.rotateCentered(Direction.UP, (float) ((yRot + 90) / 180 * Math.PI));
		buffer.rotateCentered(Direction.EAST, (float) ((xRot) / 180 * Math.PI));
		buffer.light(JsonGlProgram.a(te.v(), crafterState, pos));
		return buffer;
	}

}
