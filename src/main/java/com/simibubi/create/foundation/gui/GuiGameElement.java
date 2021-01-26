package com.simibubi.create.foundation.gui;

import javax.annotation.Nullable;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.VirtualEmptyModelData;
import cut;
import ejo;
import elg;
import net.minecraft.block.BellBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.WindowSettings.j;
import net.minecraft.client.WindowSettings.q;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FpsSmoother;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraftforge.fluids.FluidStack;

public class GuiGameElement {

	public static GuiRenderBuilder of(ItemCooldownManager stack) {
		return new GuiItemRenderBuilder(stack);
	}

	public static GuiRenderBuilder of(GameRules itemProvider) {
		return new GuiItemRenderBuilder(itemProvider);
	}

	public static GuiRenderBuilder of(PistonHandler state) {
		return new GuiBlockStateRenderBuilder(state);
	}

	public static GuiRenderBuilder of(AllBlockPartials partial) {
		return new GuiBlockPartialRenderBuilder(partial);
	}

	public static GuiRenderBuilder of(cut fluid) {
		return new GuiBlockStateRenderBuilder(fluid.h()
			.g()
			.a(LecternBlock.FACING, 0));
	}

	public static abstract class GuiRenderBuilder {
		double xBeforeScale, yBeforeScale, zBeforeScale = 0;
		double x, y, z;
		double xRot, yRot, zRot;
		double scale = 1;
		int color = 0xFFFFFF;
		EntityHitResult rotationOffset = EntityHitResult.a;

		public GuiRenderBuilder atLocal(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public GuiRenderBuilder at(double x, double y) {
			this.xBeforeScale = x;
			this.yBeforeScale = y;
			return this;
		}

		public GuiRenderBuilder at(double x, double y, double z) {
			this.xBeforeScale = x;
			this.yBeforeScale = y;
			this.zBeforeScale = z;
			return this;
		}

		public GuiRenderBuilder rotate(double xRot, double yRot, double zRot) {
			this.xRot = xRot;
			this.yRot = yRot;
			this.zRot = zRot;
			return this;
		}

		public GuiRenderBuilder rotateBlock(double xRot, double yRot, double zRot) {
			return this.rotate(xRot, yRot, zRot)
				.withRotationOffset(VecHelper.getCenterOf(BlockPos.ORIGIN));
		}

		public GuiRenderBuilder scale(double scale) {
			this.scale = scale;
			return this;
		}

		public GuiRenderBuilder color(int color) {
			this.color = color;
			return this;
		}

		public GuiRenderBuilder withRotationOffset(EntityHitResult offset) {
			this.rotationOffset = offset;
			return this;
		}

		public abstract void render(BufferVertexConsumer matrixStack);

		@Deprecated
		protected void prepare() {}

		protected void prepareMatrix(BufferVertexConsumer matrixStack) {
			matrixStack.a();
			RenderSystem.enableBlend();
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableAlphaTest();
			GlStateManager.disableAlphaTest();
			RenderSystem.alphaFunc(516, 0.1F);
			RenderSystem.blendFunc(q.l, j.j);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		@Deprecated
		protected void transform() {
			RenderSystem.translated(xBeforeScale, yBeforeScale, 0);
			RenderSystem.scaled(scale, scale, scale);
			RenderSystem.translated(x, y, z);
			RenderSystem.scaled(1, -1, 1);
			RenderSystem.translated(rotationOffset.entity, rotationOffset.c, rotationOffset.d);
			RenderSystem.rotatef((float) zRot, 0, 0, 1);
			RenderSystem.rotatef((float) xRot, 1, 0, 0);
			RenderSystem.rotatef((float) yRot, 0, 1, 0);
			RenderSystem.translated(-rotationOffset.entity, -rotationOffset.c, -rotationOffset.d);
		}

		protected void transformMatrix(BufferVertexConsumer matrixStack) {
			matrixStack.a(xBeforeScale, yBeforeScale, zBeforeScale);
			matrixStack.a((float) scale, (float) scale, (float) scale);
			matrixStack.a(x, y, z);
			matrixStack.a(1, -1, 1);
			matrixStack.a(rotationOffset.entity, rotationOffset.c, rotationOffset.d);
			matrixStack.a(Vector3f.POSITIVE_Z.getDegreesQuaternion((float) zRot));
			matrixStack.a(Vector3f.POSITIVE_X.getDegreesQuaternion((float) xRot));
			matrixStack.a(Vector3f.POSITIVE_Y.getDegreesQuaternion((float) yRot));
			matrixStack.a(-rotationOffset.entity, -rotationOffset.c, -rotationOffset.d);
		}

		@Deprecated
		protected void cleanUp() {}

		protected void cleanUpMatrix(BufferVertexConsumer matrixStack) {
			matrixStack.b();
			RenderSystem.disableAlphaTest();
			RenderSystem.disableRescaleNormal();
		}
	}

	private static class GuiBlockModelRenderBuilder extends GuiRenderBuilder {

		protected elg blockmodel;
		protected PistonHandler blockState;

		public GuiBlockModelRenderBuilder(elg blockmodel, @Nullable PistonHandler blockState) {
			this.blockState = blockState == null ? BellBlock.FACING.n() : blockState;
			this.blockmodel = blockmodel;
		}

		@Override
		public void render(BufferVertexConsumer matrixStack) {
			prepareMatrix(matrixStack);

			KeyBinding mc = KeyBinding.B();
			FpsSmoother blockRenderer = mc.aa();
			BackgroundRenderer.FogType buffer = mc.aC()
				.b();
			VertexConsumerProvider renderType = blockState.b() == BellBlock.FACING ? ShaderEffect.j()
				: BlockBufferBuilderStorage.a(blockState, true);
			OverlayVertexConsumer vb = buffer.getBuffer(renderType);

			transformMatrix(matrixStack);

			mc.L()
				.a(GrindstoneScreenHandler.result);
			renderModel(blockRenderer, buffer, renderType, vb, matrixStack);

			cleanUpMatrix(matrixStack);
		}

		protected void renderModel(FpsSmoother blockRenderer, BackgroundRenderer.FogType buffer,
			VertexConsumerProvider renderType, OverlayVertexConsumer vb, BufferVertexConsumer ms) {
			int color = KeyBinding.B().ak().a(blockState, null, null, 0);
			EntityHitResult rgb = ColorHelper.getRGB(color == -1 ? this.color : color);
			blockRenderer.b()
				.renderModel(ms.c(), vb, blockState, blockmodel, (float) rgb.entity, (float) rgb.c, (float) rgb.d,
					0xF000F0, ejo.a, VirtualEmptyModelData.INSTANCE);
			buffer.method_23792();
		}
	}

	public static class GuiBlockStateRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockStateRenderBuilder(PistonHandler blockstate) {
			super(KeyBinding.B()
				.aa()
				.a(blockstate), blockstate);
		}

		@Override
		protected void renderModel(FpsSmoother blockRenderer, BackgroundRenderer.FogType buffer,
			VertexConsumerProvider renderType, OverlayVertexConsumer vb, BufferVertexConsumer ms) {
			if (blockState.b() instanceof FarmlandBlock) {
				GlStateManager.popAttributes();
				blockRenderer.renderBlock(blockState, ms, buffer, 0xF000F0, ejo.a,
					VirtualEmptyModelData.INSTANCE);
				GlStateManager.pushLightingAttributes();
				buffer.method_23792();
				return;
			}

			super.renderModel(blockRenderer, buffer, renderType, vb, ms);

			if (blockState.m()
				.c())
				return;

			RenderSystem.pushMatrix();
			GlStateManager.pushTextureAttributes();
			FluidRenderer.renderTiledFluidBB(new FluidStack(blockState.m()
				.a(), 1000), 0, 0, 0, 1.0001f, 1.0001f, 1.0001f, buffer, ms, 0xf000f0, true);
			buffer.a(VertexConsumerProvider.f());
			GlStateManager.pushLightingAttributes();
			RenderSystem.popMatrix();
		}
	}

	public static class GuiBlockPartialRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockPartialRenderBuilder(AllBlockPartials partial) {
			super(partial.get(), null);
		}

	}

	public static class GuiItemRenderBuilder extends GuiRenderBuilder {

		private final ItemCooldownManager stack;

		public GuiItemRenderBuilder(ItemCooldownManager stack) {
			this.stack = stack;
		}

		public GuiItemRenderBuilder(GameRules provider) {
			this(new ItemCooldownManager(provider));
		}

		@Override
		public void render(BufferVertexConsumer matrixStack) {
			prepareMatrix(matrixStack);
//			matrixStack.translate(0, 80, 0);
			transformMatrix(matrixStack);
			renderItemIntoGUI(matrixStack, stack);
			cleanUpMatrix(matrixStack);
		}
		/*
		public void render() {
			prepare();
			transform();
			RenderSystem.scaled(1, -1, 1);
			RenderSystem.translated(0, 0, -75);
			Minecraft.getInstance()
				.getItemRenderer()
				.renderItemIntoGUI(stack, 0, 0);
			cleanUp();
			}
		 */

		public static void renderItemIntoGUI(BufferVertexConsumer matrixStack, ItemCooldownManager stack) {
			HorseEntityRenderer renderer = KeyBinding.B()
				.ac();
			elg bakedModel = renderer.a(stack, null, null);
			matrixStack.a();
			renderer.e.a(PlayerSkinTexture.d);
			renderer.e.b(PlayerSkinTexture.d)
				.a(false, false);
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableAlphaTest();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(WindowSettings.q.l,
				WindowSettings.j.j);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.a((float) 0, (float) 0, 100.0F + renderer.b);
			matrixStack.a(8.0F, 8.0F, 0.0F);
			matrixStack.a(16.0F, 16.0F, 16.0F);
			BackgroundRenderer.FogType irendertypebuffer$impl = KeyBinding.B()
				.aC()
				.b();
			boolean flag = !bakedModel.c();
			if (flag) {
				GlStateManager.popAttributes();
			}

			renderer.a(stack, ModelElementTexture.b.g, false, matrixStack,
				irendertypebuffer$impl, 15728880, ejo.a, bakedModel);
			irendertypebuffer$impl.method_23792();
			RenderSystem.enableDepthTest();
			if (flag) {
				GlStateManager.disableAlphaTest();
			}

			RenderSystem.disableAlphaTest();
			RenderSystem.disableRescaleNormal();
			RenderSystem.enableCull();
			matrixStack.b();
		}

	}

}
