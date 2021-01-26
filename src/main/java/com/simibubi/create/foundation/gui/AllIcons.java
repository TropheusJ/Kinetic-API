package com.simibubi.create.foundation.gui;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.ColorHelper;
import dkt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.PresetsScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.BufferVertexConsumer.a;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AllIcons {

	public static final Identifier ICON_ATLAS = Create.asResource("textures/gui/icons.png");
	private static int x = 0, y = -1;
	private int iconX;
	private int iconY;

	public static final AllIcons 
		I_ADD = newRow(), 
		I_TRASH = next(), 
		I_3x3 = next(), 
		I_TARGET = next(),
		I_PRIORITY_VERY_LOW = next(), 
		I_PRIORITY_LOW = next(), 
		I_PRIORITY_HIGH = next(), 
		I_PRIORITY_VERY_HIGH = next(),
		I_BLACKLIST = next(), 
		I_WHITELIST = next(), 
		I_WHITELIST_OR = next(), 
		I_WHITELIST_AND = next(),
		I_WHITELIST_NOT = next(), 
		I_RESPECT_NBT = next(), 
		I_IGNORE_NBT = next();

	public static final AllIcons 
		I_CONFIRM = newRow(),
		I_NONE = next(),
		I_OPEN_FOLDER = next(),
		I_REFRESH = next(),
		I_ACTIVE = next(),
		I_PASSIVE = next(),
		I_ROTATE_PLACE = next(),
		I_ROTATE_PLACE_RETURNED = next(),
		I_ROTATE_NEVER_PLACE = next(),
		I_MOVE_PLACE = next(),
		I_MOVE_PLACE_RETURNED = next(),
		I_MOVE_NEVER_PLACE = next(),
		I_CART_ROTATE = next(),
		I_CART_ROTATE_PAUSED = next(),
		I_CART_ROTATE_LOCKED = next();
	
	public static final AllIcons 
		I_DONT_REPLACE = newRow(),
		I_REPLACE_SOLID = next(),
		I_REPLACE_ANY = next(),
		I_REPLACE_EMPTY = next(),
		I_CENTERED = next(),
		I_ATTACHED = next(),
		I_INSERTED = next(),
		I_FILL = next(),
		I_PLACE = next(),
		I_REPLACE = next(),
		I_CLEAR = next(),
		I_OVERLAY = next(),
		I_FLATTEN = next();
	
	public static final AllIcons 
		I_TOOL_DEPLOY = newRow(),
		I_SKIP_MISSING = next(),
		I_SKIP_TILES = next(),
		I_DICE = next(),
		I_TUNNEL_SPLIT = next(),
		I_TUNNEL_FORCED_SPLIT = next(),
		I_TUNNEL_ROUND_ROBIN = next(),
		I_TUNNEL_FORCED_ROUND_ROBIN = next(),
		I_TUNNEL_PREFER_NEAREST = next(),
		I_TUNNEL_RANDOMIZE = next(),
		I_TUNNEL_SYNCHRONIZE = next(),
	
		I_TOOL_MOVE_XZ = newRow(),
		I_TOOL_MOVE_Y = next(),
		I_TOOL_ROTATE = next(),
		I_TOOL_MIRROR = next(),
		I_ARM_ROUND_ROBIN = next(),
		I_ARM_FORCED_ROUND_ROBIN = next(),
		I_ARM_PREFER_FIRST = next(),
		
		I_ADD_INVERTED_ATTRIBUTE = next(),
		I_FLIP = next(),
	
		I_PLAY = newRow(),
		I_PAUSE = next(),
		I_STOP = next(),
		I_PLACEMENT_SETTINGS = next(),
		I_ROTATE_CCW = next(),
		I_HOUR_HAND_FIRST = next(),
		I_MINUTE_HAND_FIRST = next(),
		I_HOUR_HAND_FIRST_24 = next(),
	
		I_PATTERN_SOLID = newRow(),
		I_PATTERN_CHECKERED = next(),
		I_PATTERN_CHECKERED_INVERSED = next(),
		I_PATTERN_CHANCE_25 = next(),
	
		I_PATTERN_CHANCE_50 = newRow(),
		I_PATTERN_CHANCE_75 = next(),
		I_FOLLOW_DIAGONAL = next(),
		I_FOLLOW_MATERIAL = next(),
		
		I_SCHEMATIC = newRow();
	
	public AllIcons(int x, int y) {
		iconX = x * 16;
		iconY = y * 16;
	}

	private static AllIcons next() {
		return new AllIcons(++x, y);
	}

	private static AllIcons newRow() {
		return new AllIcons(x = 0, ++y);
	}

	@Environment(EnvType.CLIENT)
	public void bind() {
		KeyBinding.B()
			.L()
			.a(ICON_ATLAS);
	}

	@Environment(EnvType.CLIENT)
	public void draw(BufferVertexConsumer matrixStack, dkt screen, int x, int y) {
		bind();
		screen.b(matrixStack, x, y, iconX, iconY, 16, 16);
	}

	@Environment(EnvType.CLIENT)
	public void draw(BufferVertexConsumer matrixStack, int x, int y) {
		draw(matrixStack, new PresetsScreen(null) {
		}, x, y);
	}

	@Environment(EnvType.CLIENT)
	public void draw(BufferVertexConsumer ms, BackgroundRenderer buffer, int color) {
		OverlayVertexConsumer builder = buffer.getBuffer(VertexConsumerProvider.q(ICON_ATLAS));
		float sheetSize = 256;
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		a peek = ms.c();
		EntityHitResult rgb = ColorHelper.getRGB(color);

		EntityHitResult vec4 = new EntityHitResult(1, 1, 0);
		EntityHitResult vec3 = new EntityHitResult(0, 1, 0);
		EntityHitResult vec2 = new EntityHitResult(0, 0, 0);
		EntityHitResult vec1 = new EntityHitResult(1, 0, 0);

		float u1 = (iconX + 16) / sheetSize;
		float u2 = iconX / sheetSize;
		float v1 = iconY / sheetSize;
		float v2 = (iconY + 16) / sheetSize;

		vertex(peek, builder, j, k, rgb, vec1, u1, v1);
		vertex(peek, builder, j, k, rgb, vec2, u2, v1);
		vertex(peek, builder, j, k, rgb, vec3, u2, v2);
		vertex(peek, builder, j, k, rgb, vec4, u1, v2);
	}

	@Environment(EnvType.CLIENT)
	private void vertex(a peek, OverlayVertexConsumer builder, int j, int k, EntityHitResult rgb, EntityHitResult vec, float u, float v) {
		builder.a(peek.a(), (float) vec.entity, (float) vec.c, (float) vec.d)
			.a((float) rgb.entity, (float) rgb.c, (float) rgb.d, 1)
			.a(u, v)
			.b(j, k)
			.d();
	}

}
