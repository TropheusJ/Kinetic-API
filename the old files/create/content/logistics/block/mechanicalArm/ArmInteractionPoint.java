package com.simibubi.kinetic_api.content.logistics.block.mechanicalArm;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.kinetic_api.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.kinetic_api.content.contraptions.components.saw.SawBlock;
import com.simibubi.kinetic_api.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.kinetic_api.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.kinetic_api.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Basin;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Belt;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.BlazeBurner;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Chute;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Crafter;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.CrushingWheels;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Deployer;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Depot;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Funnel;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Jukebox;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Millstone;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Mode;
import com.simibubi.kinetic_api.content.logistics.block.mechanicalArm.ArmInteractionPoint.Saw;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.item.SmartInventory;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.InfestedBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.Hopper;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class ArmInteractionPoint {

	static enum Mode {
		DEPOSIT, TAKE
	}

	BlockPos pos;
	PistonHandler state;
	Mode mode;

	private LazyOptional<IItemHandler> cachedHandler;
	private ArmAngleTarget cachedAngles;

	private static ImmutableMap<ArmInteractionPoint, Supplier<ArmInteractionPoint>> POINTS =
		ImmutableMap.<ArmInteractionPoint, Supplier<ArmInteractionPoint>>builder()
			.put(new Saw(), Saw::new)
			.put(new Belt(), Belt::new)
			.put(new Depot(), Depot::new)
			.put(new Chute(), Chute::new)
			.put(new Basin(), Basin::new)
			.put(new Funnel(), Funnel::new)
			.put(new Jukebox(), Jukebox::new)
			.put(new Crafter(), Crafter::new)
			.put(new Deployer(), Deployer::new)
			.put(new Millstone(), Millstone::new)
			.put(new BlazeBurner(), BlazeBurner::new)
			.put(new CrushingWheels(), CrushingWheels::new)
			.build();

	public ArmInteractionPoint() {
		cachedHandler = LazyOptional.empty();
	}

	@Environment(EnvType.CLIENT)
	void transformFlag(BufferVertexConsumer stack) {}

	AllBlockPartials getFlagType() {
		return mode == Mode.TAKE ? AllBlockPartials.FLAG_LONG_OUT : AllBlockPartials.FLAG_LONG_IN;
	}

	void cycleMode() {
		mode = mode == Mode.DEPOSIT ? Mode.TAKE : Mode.DEPOSIT;
	}

	EntityHitResult getInteractionPositionVector() {
		return VecHelper.getCenterOf(pos);
	}

	Direction getInteractionDirection() {
		return Direction.DOWN;
	}

	boolean isStillValid(MobSpawnerLogic reader) {
		return isValid(reader, pos, reader.d_(pos));
	}

	abstract boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state);

	static boolean isInteractable(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(reader, pos, state))
				return true;
		return false;
	}

	ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
		if (cachedAngles == null)
			cachedAngles =
				new ArmAngleTarget(armPos, getInteractionPositionVector(), getInteractionDirection(), ceiling);
		return cachedAngles;
	}

	@Nullable
	IItemHandler getHandler(GameMode world) {
		if (!cachedHandler.isPresent()) {
			BeehiveBlockEntity te = world.c(pos);
			if (te == null)
				return null;
			cachedHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		}
		return cachedHandler.orElse(null);
	}

	ItemCooldownManager insert(GameMode world, ItemCooldownManager stack, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return stack;
		return ItemHandlerHelper.insertItem(handler, stack, simulate);
	}

	ItemCooldownManager extract(GameMode world, int slot, int amount, boolean simulate) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return ItemCooldownManager.tick;
		return handler.extractItem(slot, amount, simulate);
	}

	ItemCooldownManager extract(GameMode world, int slot, boolean simulate) {
		return extract(world, slot, 64, simulate);
	}

	int getSlotCount(GameMode world) {
		IItemHandler handler = getHandler(world);
		if (handler == null)
			return 0;
		return handler.getSlots();
	}

	@Nullable
	static ArmInteractionPoint createAt(MobSpawnerLogic world, BlockPos pos) {
		PistonHandler state = world.d_(pos);
		ArmInteractionPoint point = null;

		for (ArmInteractionPoint armInteractionPoint : POINTS.keySet())
			if (armInteractionPoint.isValid(world, pos, state))
				point = POINTS.get(armInteractionPoint)
					.get();

		if (point != null) {
			point.state = state;
			point.pos = pos;
			point.mode = Mode.DEPOSIT;
		}

		return point;
	}

	CompoundTag serialize(BlockPos anchor) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("Pos", NbtHelper.fromBlockPos(pos.subtract(anchor)));
		NBTHelper.writeEnum(nbt, "Mode", mode);
		return nbt;
	}

	static ArmInteractionPoint deserialize(MobSpawnerLogic world, BlockPos anchor, CompoundTag nbt) {
		BlockPos pos = NbtHelper.toBlockPos(nbt.getCompound("Pos"));
		ArmInteractionPoint interactionPoint = createAt(world, pos.add(anchor));
		if (interactionPoint == null)
			return null;
		interactionPoint.mode = NBTHelper.readEnum(nbt, "Mode", Mode.class);
		return interactionPoint;
	}

	static abstract class TopFaceArmInteractionPoint extends ArmInteractionPoint {

		@Override
		EntityHitResult getInteractionPositionVector() {
			return EntityHitResult.b(pos).b(.5f, 1, .5f);
		}

	}

	static class Depot extends ArmInteractionPoint {

		@Override
		EntityHitResult getInteractionPositionVector() {
			return EntityHitResult.b(pos).b(.5f, 14 / 16f, .5f);
		}

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.DEPOT.has(state);
		}

	}

	static class Saw extends Depot {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.c(SawBlock.FACING) == Direction.UP
				&& ((KineticTileEntity) reader.c(pos)).getSpeed() != 0;
		}

	}

	static class Millstone extends ArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.MILLSTONE.has(state);
		}

	}

	static class CrushingWheels extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

	}

	static class Deployer extends ArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		Direction getInteractionDirection() {
			return state.c(DeployerBlock.FACING)
				.getOpposite();
		}

		@Override
		EntityHitResult getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.e(EntityHitResult.b(getInteractionDirection().getVector()).a(.65f));
		}

	}

	static class BlazeBurner extends ArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		ItemCooldownManager extract(GameMode world, int slot, int amount, boolean simulate) {
			return ItemCooldownManager.tick;
		}

		@Override
		ItemCooldownManager insert(GameMode world, ItemCooldownManager stack, boolean simulate) {
			ItemCooldownManager input = stack.i();
			if (!BlazeBurnerBlock.tryInsert(state, world, pos, input, false, true).b().a()) {
				return stack;
			}
			LocalDifficulty<ItemCooldownManager> res = BlazeBurnerBlock.tryInsert(state, world, pos, input, false, simulate);
			return res.getGlobalDifficulty() == Difficulty.SUCCESS ? ItemHandlerHelper.copyStackWithSize(stack, stack.E() - 1) : stack;
		}

		@Override
		void cycleMode() {}

	}

	static class Crafter extends ArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		Direction getInteractionDirection() {
			return state.c(MechanicalCrafterBlock.HORIZONTAL_FACING)
				.getOpposite();
		}

		@Override
		ItemCooldownManager extract(GameMode world, int slot, int amount, boolean simulate) {
			BeehiveBlockEntity te = world.c(pos);
			if (!(te instanceof MechanicalCrafterTileEntity))
				return ItemCooldownManager.tick;
			MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
			SmartInventory inventory = crafter.getInventory();
			inventory.allowExtraction();
			ItemCooldownManager extract = super.extract(world, slot, amount, simulate);
			inventory.forbidExtraction();
			return extract;
		}

		@Override
		EntityHitResult getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.e(EntityHitResult.b(getInteractionDirection().getVector()).a(.5f));
		}

	}

	static class Basin extends ArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.BASIN.has(state);
		}

	}

	static class Jukebox extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return state.b() instanceof InfestedBlock;
		}

		@Override
		int getSlotCount(GameMode world) {
			return 1;
		}

		@Override
		ItemCooldownManager insert(GameMode world, ItemCooldownManager stack, boolean simulate) {
			BeehiveBlockEntity tileEntity = world.c(pos);
			if (!(tileEntity instanceof Hopper))
				return stack;
			if (!(state.b() instanceof InfestedBlock))
				return stack;
			InfestedBlock jukeboxBlock = (InfestedBlock) state.b();
			Hopper jukeboxTE = (Hopper) tileEntity;
			if (!jukeboxTE.d()
				.a())
				return stack;
			if (!(stack.b() instanceof PotionItem))
				return stack;
			ItemCooldownManager remainder = stack.i();
			ItemCooldownManager toInsert = remainder.a(1);
			if (!simulate && !world.v) {
				jukeboxBlock.a(world, pos, state, toInsert);
				world.a((PlayerAbilities) null, 1010, pos, HoeItem.a(toInsert.b()));
				AllTriggers.triggerForNearbyPlayers(AllTriggers.MUSICAL_ARM, world, pos, 10);
			}
			return remainder;
		}

		@Override
		ItemCooldownManager extract(GameMode world, int slot, int amount, boolean simulate) {
			BeehiveBlockEntity tileEntity = world.c(pos);
			if (!(tileEntity instanceof Hopper))
				return ItemCooldownManager.tick;
			if (!(state.b() instanceof InfestedBlock))
				return ItemCooldownManager.tick;
			Hopper jukeboxTE = (Hopper) tileEntity;
			ItemCooldownManager itemstack = jukeboxTE.d();
			if (itemstack.a())
				return ItemCooldownManager.tick;
			if (!simulate && !world.v) {
				world.syncWorldEvent(1010, pos, 0);
				jukeboxTE.Y_();
				world.a(pos, state.a(InfestedBlock.regularBlock, false), 2);
			}
			return itemstack;
		}

	}

	static class Belt extends Depot {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.BELT.has(state) && !(reader.d_(pos.up())
				.b() instanceof BeltTunnelBlock);
		}
	}

	static class Chute extends TopFaceArmInteractionPoint {

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return AllBlocks.CHUTE.has(state);
		}
	}

	static class Funnel extends ArmInteractionPoint {

		@Override
		EntityHitResult getInteractionPositionVector() {
			return VecHelper.getCenterOf(pos)
				.e(EntityHitResult.b(FunnelBlock.getFunnelFacing(state)
					.getVector()).a(.5f));
		}

		@Override
		int getSlotCount(GameMode world) {
			return 0;
		}

		@Override
		ItemCooldownManager extract(GameMode world, int slot, int amount, boolean simulate) {
			return ItemCooldownManager.tick;
		}

		@Override
		Direction getInteractionDirection() {
			return FunnelBlock.getFunnelFacing(state)
				.getOpposite();
		}

		@Override
		ItemCooldownManager insert(GameMode world, ItemCooldownManager stack, boolean simulate) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = TileEntityBehaviour.get(world, pos, InvManipulationBehaviour.TYPE);
			PistonHandler state = world.d_(pos);
			if (state.d(BambooLeaves.w).orElse(false))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;
			if (simulate)
				inserter.simulate();
			ItemCooldownManager insert = inserter.insert(stack);
			if (!simulate && insert.E() != stack.E()) {
				BeehiveBlockEntity tileEntity = world.c(pos);
				if (tileEntity instanceof FunnelTileEntity)
					((FunnelTileEntity) tileEntity).onTransfer(stack);
			}
			return insert;
		}

		@Override
		boolean isValid(MobSpawnerLogic reader, BlockPos pos, PistonHandler state) {
			return state.b() instanceof FunnelBlock;
		}

		@Override
		void cycleMode() {}

	}

}
