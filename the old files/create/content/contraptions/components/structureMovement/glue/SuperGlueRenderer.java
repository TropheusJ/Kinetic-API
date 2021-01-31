package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue;

import com.simibubi.kinetic_api.AllItems;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ejo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.BufferVertexConsumer.a;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.client.render.entity.DragonFireballEntityRenderer;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Environment(EnvType.CLIENT)
public class SuperGlueRenderer extends DragonFireballEntityRenderer<SuperGlueEntity> {

	private Identifier regular = new Identifier(Create.ID, "textures/entity/super_glue/slime.png");

	private EntityHitResult[] quad1;
	private EntityHitResult[] quad2;
	private float[] u = { 0, 1, 1, 0 };
	private float[] v = { 0, 0, 1, 1 };

	public SuperGlueRenderer(DolphinEntityRenderer renderManager) {
		super(renderManager);
		initQuads();
	}

	@Override
	public Identifier getEntityTexture(SuperGlueEntity entity) {
		return regular;
	}

	@Override
	public void render(SuperGlueEntity entity, float p_225623_2_, float p_225623_3_, BufferVertexConsumer ms,
		BackgroundRenderer buffer, int light) {
		super.a(entity, p_225623_2_, p_225623_3_, ms, buffer, light);

		PlayerAbilities player = KeyBinding.B().s;
		boolean visible = entity.isVisible();
		boolean holdingGlue = AllItems.SUPER_GLUE.isIn(player.dC())
			|| AllItems.SUPER_GLUE.isIn(player.dD());

		if (!visible && !holdingGlue)
			return;

		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.c(getEntityTexture(entity)));
		light = getBrightnessForRender(entity);
		Direction face = entity.getFacingDirection();

		ms.a();
		MatrixStacker.of(ms)
			.rotateY(AngleHelper.horizontalAngle(face))
			.rotateX(AngleHelper.verticalAngle(face));
		a peek = ms.c();

		EntityHitResult[][] quads = { quad1, quad2 };
		for (EntityHitResult[] quad : quads) {
			for (int i = 0; i < 4; i++) {
				EntityHitResult vertex = quad[i];
				builder.a(peek.a(), (float) vertex.entity, (float) vertex.c, (float) vertex.d)
					.a(255, 255, 255, 255)
					.a(u[i], v[i])
					.b(ejo.a)
					.a(light)
					.a(peek.b(), face.getOffsetX(), face.getOffsetY(), face.getOffsetZ())
					.d();
			}
			face = face.getOpposite();
		}
		ms.b();
	}

	private void initQuads() {
		EntityHitResult diff = EntityHitResult.b(Direction.SOUTH.getVector());
		EntityHitResult extension = diff.d()
			.a(1 / 32f - 1 / 128f);

		EntityHitResult plane = VecHelper.axisAlingedPlaneOf(diff);
		Axis axis = Direction.getFacing(diff.entity, diff.c, diff.d)
			.getAxis();

		EntityHitResult start = EntityHitResult.a.d(extension);
		EntityHitResult end = EntityHitResult.a.e(extension);

		plane = plane.a(1 / 2f);
		EntityHitResult a1 = plane.e(start);
		EntityHitResult b1 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a2 = plane.e(start);
		EntityHitResult b2 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a3 = plane.e(start);
		EntityHitResult b3 = plane.e(end);
		plane = VecHelper.rotate(plane, -90, axis);
		EntityHitResult a4 = plane.e(start);
		EntityHitResult b4 = plane.e(end);

		quad1 = new EntityHitResult[] { a2, a3, a4, a1 };
		quad2 = new EntityHitResult[] { b3, b2, b1, b4 };
	}

	private int getBrightnessForRender(SuperGlueEntity entity) {
		BlockPos blockpos = entity.getHangingPosition();
		BlockPos blockpos2 = blockpos.offset(entity.getFacingDirection()
			.getOpposite());

		GameMode world = entity.cf();
		int light = world.p(blockpos) ? JsonGlProgram.a(world, blockpos) : 15;
		int light2 = world.p(blockpos2) ? JsonGlProgram.a(world, blockpos2) : 15;
		return Math.max(light, light2);
	}

}
