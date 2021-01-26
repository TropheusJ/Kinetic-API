package com.simibubi.create.content.contraptions.particle;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.ItemPickupParticle.b;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleEffect.Factory;
import net.minecraft.particle.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticleDataWithSprite<T extends ParticleEffect> extends ICustomParticleData<T> {

	Factory<T> getDeserializer();
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> getCodec() {
				return ICustomParticleDataWithSprite.this.getCodec(this);
			}
		};
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	default LargeFireSmokeParticle<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}
	
	@Environment(EnvType.CLIENT)
	public b<T> getMetaFactory();
	
	@Override
	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, ItemPickupParticle particles) {
		particles.a(type, getMetaFactory());
	}
	
}
