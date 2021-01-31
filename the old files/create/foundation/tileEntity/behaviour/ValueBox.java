package com.simibubi.kinetic_api.foundation.tileEntity.behaviour;

import com.simibubi.kinetic_api.content.logistics.item.filter.FilterItem;
import com.simibubi.kinetic_api.foundation.gui.AllIcons;
import com.simibubi.kinetic_api.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import com.simibubi.kinetic_api.foundation.utility.outliner.ChasingAABBOutline;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.timer.Timer;

public class ValueBox extends ChasingAABBOutline {

	protected Text label;
	protected Text sublabel = LiteralText.EMPTY;
	protected Text scrollTooltip = LiteralText.EMPTY;
	protected EntityHitResult labelOffset = EntityHitResult.a;

	protected int passiveColor;
	protected int highlightColor;
	public boolean isPassive;

	protected BlockPos pos;
	protected ValueBoxTransform transform;
	protected PistonHandler blockState;

	public ValueBox(Text label, Timer bb, BlockPos pos) {
		super(bb);
		this.label = label;
		this.pos = pos;
		this.blockState = KeyBinding.B().r.d_(pos);
	}

	public ValueBox transform(ValueBoxTransform transform) {
		this.transform = transform;
		return this;
	}

	public ValueBox offsetLabel(EntityHitResult offset) {
		this.labelOffset = offset;
		return this;
	}

	public ValueBox subLabel(Text sublabel) {
		this.sublabel = sublabel;
		return this;
	}

	public ValueBox scrollTooltip(Text scrollTip) {
		this.scrollTooltip = scrollTip;
		return this;
	}

	public ValueBox withColors(int passive, int highlight) {
		this.passiveColor = passive;
		this.highlightColor = highlight;
		return this;
	}

	public ValueBox passive(boolean passive) {
		this.isPassive = passive;
		return this;
	}

	@Override
	public void render(BufferVertexConsumer ms, SuperRenderTypeBuffer buffer) {
		boolean hasTransform = transform != null;
		if (transform instanceof Sided && params.getHighlightedFace() != null)
			((Sided) transform).fromSide(params.getHighlightedFace());
		if (hasTransform && !transform.shouldRender(blockState))
			return;

		ms.a();
		ms.a(pos.getX(), pos.getY(), pos.getZ());
		if (hasTransform)
			transform.transform(blockState, ms);
		transformNormals = ms.c()
			.b()
			.copy();
		params.colored(isPassive ? passiveColor : highlightColor);
		super.render(ms, buffer);

		float fontScale = hasTransform ? -transform.getFontScale() : -1 / 64f;
		ms.a(fontScale, fontScale, fontScale);

		ms.a();
		renderContents(ms, buffer);
		ms.b();

		if (!isPassive) {
			ms.a();
			ms.a(17.5, -.5, 7);
			ms.a(labelOffset.entity, labelOffset.c, labelOffset.d);

			renderHoveringText(ms, buffer, label);
			if (!sublabel.toString().isEmpty()) {
				ms.a(0, 10, 0);
				renderHoveringText(ms, buffer, sublabel);
			}
			if (!scrollTooltip.asString().isEmpty()) {
				ms.a(0, 10, 0);
				renderHoveringText(ms, buffer, scrollTooltip, 0x998899, 0x111111);
			}

			ms.b();
		}

		ms.b();
	}

	public void renderContents(BufferVertexConsumer ms, BackgroundRenderer buffer) {}

	public static class ItemValueBox extends ValueBox {
		ItemCooldownManager stack;
		int count;

		public ItemValueBox(Text label, Timer bb, BlockPos pos, ItemCooldownManager stack, int count) {
			super(label, bb, pos);
			this.stack = stack;
			this.count = count;
		}

		@Override
		public void renderContents(BufferVertexConsumer ms, BackgroundRenderer buffer) {
			super.renderContents(ms, buffer);
			ItemColorProvider font = KeyBinding.B().category;
			Text countString = new LiteralText(count == 0 ? "*" : count + "");
			ms.a(17.5f, -5f, 7f);

			boolean isFilter = stack.b() instanceof FilterItem;
			boolean isEmpty = stack.a();
			float scale = 1.5f;
			ms.a(-font.a(countString), 0, 0);
			
			if (isFilter)
				ms.a(3, 8, 7.25f);
			else if (isEmpty) {
				ms.a(-17, -2, 3f);
				scale = 2f;
			}
			else
				ms.a(-7, 10, 10 + 1 / 4f);

			ms.a(scale, scale, scale);
			drawString(ms, buffer, countString, 0, 0, isFilter ? 0xFFFFFF : 0xEDEDED);
			ms.a(0, 0, -1 / 16f);
			drawString(ms, buffer, countString, 1 - 1 / 8f, 1 - 1 / 8f, 0x4F4F4F);
		}

	}

	public static class TextValueBox extends ValueBox {
		Text text;

		public TextValueBox(Text label, Timer bb, BlockPos pos, Text text) {
			super(label, bb, pos);
			this.text = text;
		}

		@Override
		public void renderContents(BufferVertexConsumer ms, BackgroundRenderer buffer) {
			super.renderContents(ms, buffer);
			ItemColorProvider font = KeyBinding.B().category;
			float scale = 4;
			ms.a(scale, scale, 1);
			ms.a(-4, -4, 5);

			int stringWidth = font.a(text);
			float numberScale = (float) font.a / stringWidth;
			boolean singleDigit = stringWidth < 10;
			if (singleDigit)
				numberScale = numberScale / 2;
			float verticalMargin = (stringWidth - font.a) / 2f;

			ms.a(numberScale, numberScale, numberScale);
			ms.a(singleDigit ? stringWidth / 2 : 0, singleDigit ? -verticalMargin : verticalMargin, 0);

			renderHoveringText(ms, buffer, text, 0xEDEDED, 0x4f4f4f);
		}

	}

	public static class IconValueBox extends ValueBox {
		AllIcons icon;

		public IconValueBox(Text label, INamedIconOptions iconValue, Timer bb, BlockPos pos) {
			super(label, bb, pos);
			subLabel(Lang.translate(iconValue.getTranslationKey()));
			icon = iconValue.getIcon();
		}

		@Override
		public void renderContents(BufferVertexConsumer ms, BackgroundRenderer buffer) {
			super.renderContents(ms, buffer);
			float scale = 4 * 16;
			ms.a(scale, scale, scale);
			ms.a(-.5f, -.5f, 1 / 32f);
			icon.draw(ms, buffer, 0xFFFFFF);
		}

	}

	// util

	protected void renderHoveringText(BufferVertexConsumer ms, BackgroundRenderer buffer, Text text) {
		renderHoveringText(ms, buffer, text, highlightColor, ColorHelper.mixColors(passiveColor, 0, 0.75f));
	}

	protected void renderHoveringText(BufferVertexConsumer ms, BackgroundRenderer buffer, Text text, int color,
		int shadowColor) {
		ms.a();
		drawString(ms, buffer, text, 0, 0, color);
		ms.a(0, 0, -.25);
		drawString(ms, buffer, text, 1, 1, shadowColor);
		ms.b();
	}

	private static void drawString(BufferVertexConsumer ms, BackgroundRenderer buffer, Text text, float x, float y, int color) {
		KeyBinding.B().category.a(text, x, y, color, false, ms.c()
			.a(), buffer, false, 0, 15728880);
	}

}
