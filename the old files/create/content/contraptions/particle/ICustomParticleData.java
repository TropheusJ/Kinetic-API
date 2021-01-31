package com.simibubi.kinetic_api.content.contraptions.particle;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleEffect.Factory;
import net.minecraft.particle.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICustomParticleData<T extends ParticleEffect> {

	Factory<T> getDeserializer();

	Codec<T> getCodec(ParticleType<T> type); 
	
	public default ParticleType<T> createType() {
		return new ParticleType<T>(false, getDeserializer()) {

			@Override
			public Codec<T> getCodec() {
				return ICustomParticleData.this.getCodec(this);
			}
		};
	}
	
	@Environment(EnvType.CLIENT)
	public LargeFireSmokeParticle<T> getFactory();
	
	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, ItemPickupParticle particles) {
		particles.a(type, getFactory());
	}
	
}
