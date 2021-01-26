package com.simibubi.create.content.contraptions.components.actors;

import apx;
import com.simibubi.create.AllEntityTypes;
import net.minecraft.client.render.block.entity.EndGatewayBlockEntityRenderer;
import net.minecraft.client.render.entity.DolphinEntityRenderer;
import net.minecraft.client.render.entity.DragonFireballEntityRenderer;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class SeatEntity extends apx implements IEntityAdditionalSpawnData {

	public SeatEntity(EntityDimensions<?> p_i48580_1_, GameMode p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public SeatEntity(GameMode world, BlockPos pos) {
		this(AllEntityTypes.SEAT.get(), world);
		H = true;
	}

	public static EntityDimensions.a<?> build(EntityDimensions.a<?> builder) {
		@SuppressWarnings("unchecked")
		EntityDimensions.a<SeatEntity> entityBuilder = (EntityDimensions.a<SeatEntity>) builder;
		return entityBuilder.a(0.25f, 0.35f);
	}

	@Override
	public Timer cb() {
		return super.cb();
	}
	
	@Override
	public void o(double x, double y, double z) {
		super.o(x, y, z);
		Timer bb = cb();
		EntityHitResult diff = new EntityHitResult(x, y, z).d(bb.f());
		a(bb.c(diff));
	}

	@Override
	public void f(EntityHitResult p_213317_1_) {}

	@Override
	public void j() {
		if (l.v) 
			return;
		boolean blockPresent = l.d_(cA())
			.b() instanceof SeatBlock;
		if (br() && blockPresent)
			return;
		this.ac();
	}

	@Override
	protected boolean n(apx p_184228_1_) {
		return true;
	}

	@Override
	protected void p(apx entity) {
		super.p(entity);
		EntityHitResult pos = entity.cz();
		entity.d(pos.entity, pos.c + 0.85f, pos.d);
	}

	@Override
	protected void e() {}

	@Override
	protected void a(CompoundTag p_70037_1_) {}

	@Override
	protected void b(CompoundTag p_213281_1_) {}

	@Override
	public Packet<?> P() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	public static class Render extends DragonFireballEntityRenderer<SeatEntity> {

		public Render(DolphinEntityRenderer p_i46179_1_) {
			super(p_i46179_1_);
		}

		@Override
		public boolean shouldRender(SeatEntity p_225626_1_, EndGatewayBlockEntityRenderer p_225626_2_, double p_225626_3_, double p_225626_5_, double p_225626_7_) {
			return false;
		}

		@Override
		public Identifier getEntityTexture(SeatEntity p_110775_1_) {
			return null;
		}
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {}

	@Override
	public void readSpawnData(PacketByteBuf additionalData) {}
}
