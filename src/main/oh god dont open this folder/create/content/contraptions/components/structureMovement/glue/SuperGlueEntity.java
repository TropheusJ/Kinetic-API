package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import afj;
import apx;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementTraits;
import com.simibubi.create.content.schematics.ISpecialEntityItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.BlockFace;
import dcg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LoomBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

public class SuperGlueEntity extends apx implements IEntityAdditionalSpawnData, ISpecialEntityItemRequirement {

	private int validationTimer;
	protected BlockPos hangingPosition;
	protected Direction facingDirection = Direction.SOUTH;

	public SuperGlueEntity(EntityDimensions<?> type, GameMode world) {
		super(type, world);
	}

	public SuperGlueEntity(GameMode world, BlockPos pos, Direction direction) {
		this(AllEntityTypes.SUPER_GLUE.get(), world);
		hangingPosition = pos;
		facingDirection = direction;
		updateFacingWithBoundingBox();
	}

	@Override
	protected void e() {}

	public int getWidthPixels() {
		return 12;
	}

	public int getHeightPixels() {
		return 12;
	}

	public void onBroken(@Nullable apx breaker) {
		a(MusicType.om, 1.0F, 1.0F);
		if (onValidSurface()) {
			AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
				new GlueEffectPacket(getHangingPosition(), getFacingDirection().getOpposite(), false));
			a(AllSoundEvents.SLIME_ADDED.get(), 0.5F, 0.5F);
		}
	}

	public void playPlaceSound() {
		a(AllSoundEvents.SLIME_ADDED.get(), 0.5F, 0.75F);
	}

	protected void updateFacingWithBoundingBox() {
		Validate.notNull(getFacingDirection());
		if (getFacingDirection().getAxis()
			.isHorizontal()) {
			this.q = 0.0F;
			this.p = getFacingDirection().getHorizontal() * 90;
		} else {
			this.q = -90 * getFacingDirection().getDirection()
				.offset();
			this.p = 0.0F;
		}

		this.s = this.q;
		this.r = this.p;
		this.updateBoundingBox();
	}

	protected void updateBoundingBox() {
		if (this.getFacingDirection() != null) {
			double offset = 0.5 - 1 / 256d;
			double x = hangingPosition.getX() + 0.5 - facingDirection.getOffsetX() * offset;
			double y = hangingPosition.getY() + 0.5 - facingDirection.getOffsetY() * offset;
			double z = hangingPosition.getZ() + 0.5 - facingDirection.getOffsetZ() * offset;
			this.o(x, y, z);
			double w = getWidthPixels();
			double h = getHeightPixels();
			double l = getWidthPixels();
			Axis axis = this.getFacingDirection()
				.getAxis();
			double depth = 2 - 1 / 128f;

			switch (axis) {
			case X:
				w = depth;
				break;
			case Y:
				h = depth;
				break;
			case Z:
				l = depth;
			}

			w = w / 32.0D;
			h = h / 32.0D;
			l = l / 32.0D;
			this.a(new Timer(x - w, y - h, z - l, x + w, y + h, z + l));
		}
	}

	@Override
	public void j() {
		if (this.validationTimer++ == 10 && !this.l.v) {
			this.validationTimer = 0;
			if (aW() && !this.onValidSurface()) {
				ac();
				onBroken(null);
			}
		}

	}

	public boolean isVisible() {
		if (!aW())
			return false;
		BlockPos pos = hangingPosition;
		BlockPos pos2 = pos.offset(getFacingDirection().getOpposite());
		return isValidFace(l, pos2, getFacingDirection()) != isValidFace(l, pos,
			getFacingDirection().getOpposite());
	}

	public boolean onValidSurface() {
		BlockPos pos = hangingPosition;
		BlockPos pos2 = hangingPosition.offset(getFacingDirection().getOpposite());
		if (!l.isAreaLoaded(pos, 0) || !l.isAreaLoaded(pos2, 0))
			return true;
		if (!isValidFace(l, pos2, getFacingDirection())
			&& !isValidFace(l, pos, getFacingDirection().getOpposite()))
			return false;
		return l.a(this, cb(), e -> e instanceof SuperGlueEntity)
			.isEmpty();
	}

	public static boolean isValidFace(GameMode world, BlockPos pos, Direction direction) {
		PistonHandler state = world.d_(pos);
		if (BlockMovementTraits.isBlockAttachedTowards(world, pos, state, direction))
			return true;
		if (!BlockMovementTraits.movementNecessary(world, pos))
			return false;
		if (BlockMovementTraits.notSupportive(state, direction))
			return false;
		return true;
	}

	@Override
	public boolean aS() {
		return true;
	}

	@Override
	public boolean t(apx entity) {
		return entity instanceof PlayerAbilities
			? a(DamageRecord.a((PlayerAbilities) entity), 0)
			: false;
	}

	@Override
	public Direction bY() {
		return this.getFacingDirection();
	}

	@Override
	public boolean a(DamageRecord source, float amount) {
		if (this.b(source))
			return false;
		if (aW() && !l.v && isVisible()) {
			ac();
			aR();
			onBroken(source.k());
		}

		return true;
	}

	@Override
	public void a(SpawnGroup typeIn, EntityHitResult pos) {
		if (!l.v && aW() && pos.g() > 0.0D) {
			ac();
			onBroken(null);
		}
	}

	@Override
	public void i(double x, double y, double z) {
		if (!l.v && aW() && x * x + y * y + z * z > 0.0D) {
			ac();
			onBroken(null);
		}
	}

	@Override
	protected float a(PathAwareEntity poseIn, PassiveEntity sizeIn) {
		return 0.0F;
	}

	@Override
	public ItemCooldownManager getPickedResult(Box target) {
		return AllItems.SUPER_GLUE.asStack();
	}

	@Override
	public void i(apx entityIn) {
		super.i(entityIn);
	}

	@Override
	public Difficulty a(PlayerAbilities player, ItemScatterer hand) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			triggerPlaceBlock(player, hand);
		});
		return Difficulty.CONSUME;
	}

	@Environment(EnvType.CLIENT)
	private void triggerPlaceBlock(PlayerAbilities player, ItemScatterer hand) {
		if (!(player instanceof FishingParticle))
			return;
		if (!(player.l instanceof DragonHeadEntityModel))
			return;

		FishingParticle cPlayer = (FishingParticle) player;
		KeyBinding mc = KeyBinding.B();
		Box ray =
			cPlayer.a(mc.q.c(), mc.ai(), false);

		if (!(ray instanceof dcg))
			return;
		if (ray.c() == net.minecraft.util.math.Box.a.a)
			return;
		dcg blockRay = (dcg) ray;
		BlockFace rayFace = new BlockFace(blockRay.a(), blockRay.b());
		BlockFace hangingFace = new BlockFace(getHangingPosition(), getFacingDirection().getOpposite());
		if (!rayFace.isEquivalent(hangingFace))
			return;

		for (ItemScatterer handIn : ItemScatterer.values()) {
			ItemCooldownManager itemstack = cPlayer.b(handIn);
			int countBefore = itemstack.E();
			Difficulty actionResultType =
				mc.q.a(cPlayer, (DragonHeadEntityModel) cPlayer.l, handIn, blockRay);
			if (actionResultType != Difficulty.SUCCESS)
				return;

			cPlayer.a(handIn);
			if (!itemstack.a() && (itemstack.E() != countBefore || mc.q.g()))
				mc.boundKey.GRASS_COLOR.a(handIn);
			return;
		}
	}

	@Override
	public void b(CompoundTag compound) {
		compound.putByte("Facing", (byte) this.getFacingDirection()
			.getId());
		BlockPos blockpos = this.getHangingPosition();
		compound.putInt("TileX", blockpos.getX());
		compound.putInt("TileY", blockpos.getY());
		compound.putInt("TileZ", blockpos.getZ());
	}

	@Override
	public void a(CompoundTag compound) {
		this.hangingPosition =
			new BlockPos(compound.getInt("TileX"), compound.getInt("TileY"), compound.getInt("TileZ"));
		this.facingDirection = Direction.byId(compound.getByte("Facing"));
		updateFacingWithBoundingBox();
	}

	@Override
	public PaintingEntity a(ItemCooldownManager stack, float yOffset) {
		float xOffset = (float) this.getFacingDirection()
			.getOffsetX() * 0.15F;
		float zOffset = (float) this.getFacingDirection()
			.getOffsetZ() * 0.15F;
		PaintingEntity itementity =
			new PaintingEntity(this.l, this.cC() + xOffset, this.cD() + yOffset, this.cG() + zOffset, stack);
		itementity.m();
		this.l.c(itementity);
		return itementity;
	}

	@Override
	protected boolean aU() {
		return false;
	}

	@Override
	public void d(double x, double y, double z) {
		hangingPosition = new BlockPos(x, y, z);
		updateBoundingBox();
		Z = true;
	}

	@Override
	public float a(RespawnAnchorBlock transformRotation) {
		if (this.getFacingDirection()
			.getAxis() != Direction.Axis.Y) {
			switch (transformRotation) {
			case field_26443:
				facingDirection = facingDirection.getOpposite();
				break;
			case d:
				facingDirection = facingDirection.rotateYCounterclockwise();
				break;
			case field_26442:
				facingDirection = facingDirection.rotateYClockwise();
			default:
				break;
			}
		}

		float f = afj.g(this.p);
		switch (transformRotation) {
		case field_26443:
			return f + 180.0F;
		case d:
			return f + 90.0F;
		case field_26442:
			return f + 270.0F;
		default:
			return f;
		}
	}

	public BlockPos getHangingPosition() {
		return this.hangingPosition;
	}

	@Override
	public float a(LoomBlock transformMirror) {
		return this.a(transformMirror.a(this.getFacingDirection()));
	}

	public Direction getAttachedDirection(BlockPos pos) {
		return !pos.equals(hangingPosition) ? getFacingDirection() : getFacingDirection().getOpposite();
	}

	@Override
	public void a(ServerWorld world, Arm lightningBolt) {}

	@Override
	public void x_() {}

	public static EntityDimensions.a<?> build(EntityDimensions.a<?> builder) {
		@SuppressWarnings("unchecked")
		EntityDimensions.a<SuperGlueEntity> entityBuilder = (EntityDimensions.a<SuperGlueEntity>) builder;
		return entityBuilder;
	}

	@Override
	public Packet<?> P() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public void writeSpawnData(PacketByteBuf buffer) {
		CompoundTag compound = new CompoundTag();
		b(compound);
		buffer.writeCompoundTag(compound);
	}

	@Override
	public void readSpawnData(PacketByteBuf additionalData) {
		a(additionalData.readCompoundTag());
	}

	public Direction getFacingDirection() {
		return facingDirection;
	}

	@Override
	public ItemRequirement getRequiredItems() {
		return new ItemRequirement(ItemUseType.DAMAGE, AllItems.SUPER_GLUE.get());
	}

	@Override
	public boolean bP() {
		return true;
	}
}
