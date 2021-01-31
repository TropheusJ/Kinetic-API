package com.simibubi.kinetic_api.content.contraptions.components.crusher;

import java.util.Random;
import apx;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllTileEntities;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.foundation.block.ITE;
import com.simibubi.kinetic_api.foundation.item.ItemHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import ddb;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.timer.Timer;

public class CrushingWheelControllerBlock extends BeetrootsBlock
		implements ITE<CrushingWheelControllerTileEntity> {

	public CrushingWheelControllerBlock(c p_i48440_1_) {
		super(p_i48440_1_);
	}

	public static final BedPart VALID = BedPart.a("valid");

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return true;
	}

	@Override
	public boolean a(PistonHandler state, PotionUtil useContext) {
		return false;
	}

	@Override
	public boolean addRunningEffects(PistonHandler state, GameMode world, BlockPos pos, apx entity) {
		return true;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.CRUSHING_WHEEL_CONTROLLER.create();
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		builder.a(VALID);
		super.a(builder);
	}

	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, apx entityIn) {
		if (!state.c(VALID))
			return;
		withTileEntityDo(worldIn, pos, te -> {
			if (te.processingEntity == entityIn)
				entityIn.a(state, new EntityHitResult(0.25D, (double) 0.05F, 0.25D));
		});
	}

	@Override
	public void a(MobSpawnerLogic worldIn, apx entityIn) {
		super.a(worldIn, entityIn);
		try {
			CrushingWheelControllerTileEntity te = getTileEntity(worldIn, entityIn.cA().down());
			if (te.crushingspeed == 0)
				return;
			if (entityIn instanceof PaintingEntity)
				((PaintingEntity) entityIn).a(10);
			if (te.isOccupied())
				return;
			boolean isPlayer = entityIn instanceof PlayerAbilities;
			if (isPlayer && ((PlayerAbilities) entityIn).b_())
				return;
			if (isPlayer && entityIn.l.ac() == Inventories.a)
				return;

			te.startCrushing(entityIn);
		} catch (TileEntityException e) {}
	}

	@Override
	public void a(PistonHandler stateIn, GameMode worldIn, BlockPos pos, Random rand) {
		if (!stateIn.c(VALID))
			return;
		if (rand.nextInt(1) != 0)
			return;
		double d0 = (double) ((float) pos.getX() + rand.nextFloat());
		double d1 = (double) ((float) pos.getY() + rand.nextFloat());
		double d2 = (double) ((float) pos.getZ() + rand.nextFloat());
		worldIn.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public PistonHandler a(PistonHandler stateIn, Direction facing, PistonHandler facingState, GrassColors worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		updateSpeed(stateIn, worldIn, currentPos);
		return stateIn;
	}

	public void updateSpeed(PistonHandler state, GrassColors world, BlockPos pos) {
		withTileEntityDo(world, pos, te -> {
			if (!state.c(VALID)) {
				if (te.crushingspeed != 0) {
					te.crushingspeed = 0;
					te.sendData();
				}
				return;
			}

			for (Direction d : Iterate.horizontalDirections) {
				PistonHandler neighbour = world.d_(pos.offset(d));
				if (!AllBlocks.CRUSHING_WHEEL.has(neighbour))
					continue;
				if (neighbour.c(BambooLeaves.F) == d.getAxis())
					continue;
				KineticTileEntity wheelTe = (KineticTileEntity) world.c(pos.offset(d));
				te.crushingspeed = Math.abs(wheelTe.getSpeed() / 50f);
				te.sendData();
				break;
			}
		});
	}

	@Override
	public VoxelShapes c(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos,
			ArrayVoxelShape context) {
		if (!state.c(VALID))
			return ddb.b();

		apx entity = context.getEntity();
		if (entity != null) {
			if (entity != null) {
				CompoundTag data = entity.getPersistentData();
				if (data.contains("BypassCrushingWheel")) {
					if (pos.equals(NbtHelper.toBlockPos(data.getCompound("BypassCrushingWheel"))))
						return ddb.a();
				}
			}

			if (new Timer(pos).d(entity.cz()))
				return ddb.a();

			try {
				CrushingWheelControllerTileEntity te = getTileEntity(worldIn, pos);
				if (te.processingEntity == entity)
					return ddb.a();
			} catch (TileEntityException e) {}
		}
		return ddb.b();
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.b() == newState.b())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.o(pos);
	}

	@Override
	public Class<CrushingWheelControllerTileEntity> getTileEntityClass() {
		return CrushingWheelControllerTileEntity.class;
	}

}
