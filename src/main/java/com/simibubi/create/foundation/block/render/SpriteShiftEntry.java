package com.simibubi.create.foundation.block.render;

import java.util.function.Function;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Identifier;

public class SpriteShiftEntry {
	protected Identifier originalTextureLocation;
	protected Identifier targetTextureLocation;
	protected MipmapHelper original;
	protected MipmapHelper target;

	public void set(Identifier originalTextureLocation, Identifier targetTextureLocation) {
		this.originalTextureLocation = originalTextureLocation;
		this.targetTextureLocation = targetTextureLocation;
	}

	protected void loadTextures() {
		Function<Identifier, MipmapHelper> textureMap = KeyBinding.B()
			.a(GrindstoneScreenHandler.result);
		original = textureMap.apply(originalTextureLocation);
		target = textureMap.apply(targetTextureLocation);
	}

	public Identifier getTargetResourceLocation() {
		return targetTextureLocation;
	}

	public MipmapHelper getTarget() {
		if (target == null)
			loadTextures();
		return target;
	}

	public MipmapHelper getOriginal() {
		if (original == null)
			loadTextures();
		return original;
	}
}