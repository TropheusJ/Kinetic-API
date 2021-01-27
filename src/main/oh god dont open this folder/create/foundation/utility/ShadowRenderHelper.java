package com.simibubi.create.foundation.utility;

import afj;
import ejo;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.timer.Timer;

/**
 * Stolen from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final VertexConsumerProvider SHADOW_LAYER = VertexConsumerProvider.k(new Identifier("textures/misc/shadow.png"));

	public static void renderShadow(BufferVertexConsumer p_229096_0_, BackgroundRenderer p_229096_1_, EntityHitResult pos,
			float p_229096_3_, float p_229096_6_) {
		float f = p_229096_6_;

		double d2 = pos.getX();
		double d0 = pos.getY();
		double d1 = pos.getZ();
		int i = afj.c(d2 - (double) f);
		int j = afj.c(d2 + (double) f);
		int k = afj.c(d0 - (double) f);
		int l = afj.c(d0);
		int i1 = afj.c(d1 - (double) f);
		int j1 = afj.c(d1 + (double) f);
		BufferVertexConsumer.a matrixstack$entry = p_229096_0_.c();
		OverlayVertexConsumer ivertexbuilder = p_229096_1_.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.iterate(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderShadowPart(matrixstack$entry, ivertexbuilder, KeyBinding.B().r, blockpos, d2, d0, d1, f, p_229096_3_);
		}

	}

	private static void renderShadowPart(BufferVertexConsumer.a p_229092_0_, OverlayVertexConsumer p_229092_1_,
			ItemConvertible p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_,
			float p_229092_10_, float p_229092_11_) {
		BlockPos blockpos = p_229092_3_.down();
		PistonHandler blockstate = p_229092_2_.d_(blockpos);
		if (blockstate.h() != RedstoneLampBlock.LIT && p_229092_2_.B(p_229092_3_) > 3) {
			if (blockstate.r(p_229092_2_, blockpos)) {
				VoxelShapes voxelshape = blockstate.j(p_229092_2_, p_229092_3_.down());
				if (!voxelshape.b()) {
					@SuppressWarnings("deprecation")
					float f = (float) (((double) p_229092_11_ - (p_229092_6_ - (double) p_229092_3_.getY()) / 2.0D)
							* 0.5D * (double) p_229092_2_.y(p_229092_3_));
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						Timer axisalignedbb = voxelshape.a();
						double d0 = (double) p_229092_3_.getX() + axisalignedbb.LOGGER;
						double d1 = (double) p_229092_3_.getX() + axisalignedbb.eventCounter;
						double d2 = (double) p_229092_3_.getY() + axisalignedbb.callback;
						double d3 = (double) p_229092_3_.getZ() + axisalignedbb.events;
						double d4 = (double) p_229092_3_.getZ() + axisalignedbb.f;
						float f1 = (float) (d0 - p_229092_4_);
						float f2 = (float) (d1 - p_229092_4_);
						float f3 = (float) (d2 - p_229092_6_ + 0.015625D);
						float f4 = (float) (d3 - p_229092_8_);
						float f5 = (float) (d4 - p_229092_8_);
						float f6 = -f1 / 2.0F / p_229092_10_ + 0.5F;
						float f7 = -f2 / 2.0F / p_229092_10_ + 0.5F;
						float f8 = -f4 / 2.0F / p_229092_10_ + 0.5F;
						float f9 = -f5 / 2.0F / p_229092_10_ + 0.5F;
						shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f4, f6, f8);
						shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f5, f6, f9);
						shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f5, f7, f9);
						shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f4, f7, f8);
					}

				}
			}
		}
	}

	private static void shadowVertex(BufferVertexConsumer.a p_229091_0_, OverlayVertexConsumer p_229091_1_, float p_229091_2_,
			float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_) {
		p_229091_1_.a(p_229091_0_.a(), p_229091_3_, p_229091_4_, p_229091_5_)
				.a(1.0F, 1.0F, 1.0F, p_229091_2_).a(p_229091_6_, p_229091_7_)
				.b(ejo.a).a(15728880).a(p_229091_0_.b(), 0.0F, 1.0F, 0.0F)
				.d();
	}
}
