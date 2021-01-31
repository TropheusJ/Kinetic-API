package com.simibubi.kinetic_api.content.schematics.block;

import java.util.Random;
import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.schematics.block.LaunchedItem.ForBlockState;
import com.simibubi.kinetic_api.content.schematics.block.LaunchedItem.ForEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
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
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SchematicannonRenderer extends SafeTileEntityRenderer<SchematicannonTileEntity> {

	public SchematicannonRenderer(ebv dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public boolean isGlobalRenderer(SchematicannonTileEntity p_188185_1_) {
		return true;
	}

	@Override
	protected void renderSafe(SchematicannonTileEntity tileEntityIn, float partialTicks, BufferVertexConsumer ms,
			BackgroundRenderer buffer, int light, int overlay) {

		double yaw = 0;
		double pitch = 40;
		double recoil = 0;

		BlockPos pos = tileEntityIn.o();
		if (tileEntityIn.target != null) {

			// Calculate Angle of Cannon
			EntityHitResult diff = EntityHitResult.b(tileEntityIn.target.subtract(pos));
			if (tileEntityIn.previousTarget != null) {
				diff = (EntityHitResult.b(tileEntityIn.previousTarget)
						.e(EntityHitResult.b(tileEntityIn.target.subtract(tileEntityIn.previousTarget)).a(partialTicks)))
								.d(EntityHitResult.b(pos));
			}

			double diffX = diff.getX();
			double diffZ = diff.getZ();
			yaw = afj.d(diffX, diffZ);
			yaw = yaw / Math.PI * 180;

			float distance = afj.a(diffX * diffX + diffZ * diffZ);
			double yOffset = 0 + distance * 2f;
			pitch = afj.d(distance, diff.getY() * 3 + yOffset);
			pitch = pitch / Math.PI * 180 + 10;

		}

		if (!tileEntityIn.flyingBlocks.isEmpty()) {
			for (LaunchedItem launched : tileEntityIn.flyingBlocks) {

				if (launched.ticksRemaining == 0)
					continue;

				// Calculate position of flying block
				EntityHitResult start = EntityHitResult.b(tileEntityIn.o().add(.5f, 1, .5f));
				EntityHitResult target = EntityHitResult.b(launched.target).b(-.5, 0, 1);
				EntityHitResult distance = target.d(start);

				double targetY = target.c - start.c;
				double throwHeight = Math.sqrt(distance.g()) * .6f + targetY;
				EntityHitResult cannonOffset = distance.b(0, throwHeight, 0).d().a(2);
				start = start.e(cannonOffset);

				float progress =
					((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks)) / launched.totalTicks;
				EntityHitResult blockLocationXZ = new EntityHitResult(.5, .5, .5).e(target.d(start).a(progress).d(1, 0, 1));

				// Height is determined through a bezier curve
				float t = progress;
				double yOffset = 2 * (1 - t) * t * throwHeight + t * t * targetY;
				EntityHitResult blockLocation = blockLocationXZ.b(0, yOffset + 1, 0).e(cannonOffset);

				// Offset to position
				ms.a();
				ms.a(blockLocation.entity, blockLocation.c, blockLocation.d);

				ms.a(new Vector3f(0, 1, 0).getDegreesQuaternion(360 * t * 2));
				ms.a(new Vector3f(1, 0, 0).getDegreesQuaternion(360 * t * 2));

				// Render the Block
				if (launched instanceof ForBlockState) {
					float scale = .3f;
					ms.a(scale, scale, scale);
					KeyBinding.B().aa().renderBlock(((ForBlockState) launched).state,
							ms, buffer, light, overlay, EmptyModelData.INSTANCE);
				}

				// Render the item
				if (launched instanceof ForEntity) {
					float scale = 1.2f;
					ms.a(scale, scale, scale);
					KeyBinding.B().ac().a(launched.stack, b.h, light,
							overlay, ms, buffer);
				}

				ms.b();

				// Apply Recoil if block was just launched
				if ((launched.ticksRemaining + 1 - partialTicks) > launched.totalTicks - 10) 
					recoil = Math.max(recoil, (launched.ticksRemaining + 1 - partialTicks) - launched.totalTicks + 10);

				// Render particles for launch
				if (launched.ticksRemaining == launched.totalTicks && tileEntityIn.firstRenderTick) {
					tileEntityIn.firstRenderTick = false;
					for (int i = 0; i < 10; i++) {
						Random r = tileEntityIn.v().getRandom();
						double sX = cannonOffset.entity * .01f;
						double sY = (cannonOffset.c + 1) * .01f;
						double sZ = cannonOffset.d * .01f;
						double rX = r.nextFloat() - sX * 40;
						double rY = r.nextFloat() - sY * 40;
						double rZ = r.nextFloat() - sZ * 40;
						tileEntityIn.v().addParticle(ParticleTypes.CLOUD, start.entity + rX, start.c + rY,
								start.d + rZ, sX, sY, sZ);
					}
				}

			}
		}

		ms.a();
		PistonHandler state = tileEntityIn.p();
		int lightCoords = JsonGlProgram.a(tileEntityIn.v(), pos);

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		SuperByteBuffer connector = AllBlockPartials.SCHEMATICANNON_CONNECTOR.renderOn(state);
		connector.translate(.5f, 0, .5f);
		connector.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
		connector.translate(-.5f, 0, -.5f);
		connector.light(lightCoords).renderInto(ms, vb);

		SuperByteBuffer pipe = AllBlockPartials.SCHEMATICANNON_PIPE.renderOn(state);
		pipe.translate(.5f, 15 / 16f, .5f);
		pipe.rotate(Direction.UP, (float) ((yaw + 90) / 180 * Math.PI));
		pipe.rotate(Direction.SOUTH, (float) (pitch / 180 * Math.PI));
		pipe.translate(-.5f, -15 / 16f, -.5f);
		pipe.translate(0, -recoil / 100, 0);
		pipe.light(lightCoords).renderInto(ms, vb);

		ms.b();
	}

}
