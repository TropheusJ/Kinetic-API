package com.simibubi.kinetic_api.content.contraptions.fluids.particle;

import com.simibubi.kinetic_api.AllParticleTypes;
import com.simibubi.kinetic_api.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.particle.SpellParticle;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackParticle extends SpellParticle {
	private final float field_217587_G;
	private final float field_217588_H;
	private FluidStack fluid;

	public static FluidStackParticle create(ParticleType<FluidParticleData> type, DragonHeadEntityModel world, FluidStack fluid, double x,
		double y, double z, double vx, double vy, double vz) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return new BasinFluidParticle(world, fluid, x, y, z, vx, vy, vz);
		return new FluidStackParticle(world, fluid, x, y, z, vx, vy, vz);
	}

	public FluidStackParticle(DragonHeadEntityModel world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, x, y, z, vx, vy, vz);
		this.fluid = fluid;
		this.a(KeyBinding.B()
			.a(GrindstoneScreenHandler.result)
			.apply(fluid.getFluid()
				.getAttributes()
				.getStillTexture()));

		this.u = 1.0F;
		this.v = 0.8F;
		this.w = 0.8F;
		this.x = 0.8F;
		this.multiplyColor(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		
		this.j = vx;
		this.k = vy;
		this.l = vz;

		this.B /= 2.0F;
		this.field_217587_G = this.r.nextFloat() * 3.0F;
		this.field_217588_H = this.r.nextFloat() * 3.0F;
	}

	@Override
	protected int a(float p_189214_1_) {
		int brightnessForRender = super.a(p_189214_1_);
		int skyLight = brightnessForRender >> 20;
		int blockLight = (brightnessForRender >> 4) & 0xf;
		blockLight = Math.max(blockLight, fluid.getFluid()
			.getAttributes()
			.getLuminosity(fluid));
		return (skyLight << 20) | (blockLight << 4);
	}

	protected void multiplyColor(int color) {
		this.v *= (float) (color >> 16 & 255) / 255.0F;
		this.w *= (float) (color >> 8 & 255) / 255.0F;
		this.x *= (float) (color & 255) / 255.0F;
	}

	protected float c() {
		return this.C.a((double) ((this.field_217587_G + 1.0F) / 4.0F * 16.0F));
	}

	protected float d() {
		return this.C.a((double) (this.field_217587_G / 4.0F * 16.0F));
	}

	protected float e() {
		return this.C.b((double) (this.field_217588_H / 4.0F * 16.0F));
	}

	protected float f() {
		return this.C.b((double) ((this.field_217588_H + 1.0F) / 4.0F * 16.0F));
	}

	@Override
	public void clearAtlas() {
		super.a();
		if (!canEvaporate())
			return;
		if (m)
			j();
		if (!o)
			return;
		if (!m && c.t.nextFloat() < 1 / 8f)
			return;

		EntityHitResult rgb = ColorHelper.getRGB(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		c.addParticle(ParticleTypes.ENTITY_EFFECT, g, h, i, rgb.entity, rgb.c, rgb.d);
	}
	
	protected boolean canEvaporate() {
		return fluid.getFluid() instanceof PotionFluid;
	}

	@Override
	public LavaEmberParticle b() {
		return LavaEmberParticle.a;
	}

}
