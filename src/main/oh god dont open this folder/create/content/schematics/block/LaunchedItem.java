package com.simibubi.create.content.schematics.block;

import java.util.Optional;
import afj;
import apx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.content.contraptions.relays.elementary.AbstractShaftBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.Constants;

public abstract class LaunchedItem {

	public int totalTicks;
	public int ticksRemaining;
	public BlockPos target;
	public ItemCooldownManager stack;

	private LaunchedItem(BlockPos start, BlockPos target, ItemCooldownManager stack) {
		this(target, stack, ticksForDistance(start, target), ticksForDistance(start, target));
	}

	private static int ticksForDistance(BlockPos start, BlockPos target) {
		return (int) (Math.max(10, afj.c(afj.a(target.getSquaredDistance(start))) * 4f));
	}

	LaunchedItem() {}

	private LaunchedItem(BlockPos target, ItemCooldownManager stack, int ticksLeft, int total) {
		this.target = target;
		this.stack = stack;
		this.totalTicks = total;
		this.ticksRemaining = ticksLeft;
	}

	public boolean update(GameMode world) {
		if (ticksRemaining > 0) {
			ticksRemaining--;
			return false;
		}
		if (world.v)
			return false;

		place(world);
		return true;
	}

	public CompoundTag serializeNBT() {
		CompoundTag c = new CompoundTag();
		c.putInt("TotalTicks", totalTicks);
		c.putInt("TicksLeft", ticksRemaining);
		c.put("Stack", stack.serializeNBT());
		c.put("Target", NbtHelper.fromBlockPos(target));
		return c;
	}

	public static LaunchedItem fromNBT(CompoundTag c) {
		LaunchedItem launched = c.contains("Length") ? new LaunchedItem.ForBelt()
				: c.contains("BlockState") ? new LaunchedItem.ForBlockState() : new LaunchedItem.ForEntity();
		launched.readNBT(c);
		return launched;
	}

	abstract void place(GameMode world);

	void readNBT(CompoundTag c) {
		target = NbtHelper.toBlockPos(c.getCompound("Target"));
		ticksRemaining = c.getInt("TicksLeft");
		totalTicks = c.getInt("TotalTicks");
		stack = ItemCooldownManager.a(c.getCompound("Stack"));
	}

	public static class ForBlockState extends LaunchedItem {
		public PistonHandler state;
		public CompoundTag data;

		ForBlockState() {}

		public ForBlockState(BlockPos start, BlockPos target, ItemCooldownManager stack, PistonHandler state, CompoundTag data) {
			super(start, target, stack);
			this.state = state;
			this.data = data;
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			serializeNBT.put("BlockState", NbtHelper.a(state));
			if (data != null) {
				data.remove("x");
				data.remove("y");
				data.remove("z");
				data.remove("id");
				serializeNBT.put("Data", data);
			}
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			super.readNBT(nbt);
			state = NbtHelper.c(nbt.getCompound("BlockState"));
			if (nbt.contains("Data", Constants.NBT.TAG_COMPOUND)) {
				data = nbt.getCompound("Data");
			}
		}

		@Override
		void place(GameMode world) {
			// Piston
			if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.g))
				state = state.a(BambooLeaves.g, Boolean.FALSE);
			if (BlockHelper.hasBlockStateProperty(state, BambooLeaves.C))
				state = state.a(BambooLeaves.C, Boolean.FALSE);

			if (AllBlocks.BELT.has(state)) {
				world.a(target, state, 2);
				return;
			}
			else if (state.b() == BellBlock.na)
				state = BellBlock.na.n();
			else if (state.b() != BellBlock.kU && state.b() instanceof IPlantable)
				state = ((IPlantable) state.b()).getPlant(world, target);

			if (world.k().d() && state.m().a().a(BlockTags.field_15481)) {
				int i = target.getX();
				int j = target.getY();
				int k = target.getZ();
				world.a(null, target, MusicType.ej, SoundEvent.e, 0.5F, 2.6F + (world.t.nextFloat() - world.t.nextFloat()) * 0.8F);

				for (int l = 0; l < 8; ++l) {
					world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(), 0.0D, 0.0D, 0.0D);
				}
				BeetrootsBlock.c(state, world, target);
				return;
			}
			world.a(target, state, 18);
			if (data != null) {
				BeehiveBlockEntity tile = world.c(target);
				if (tile != null) {
					data.putInt("x", target.getX());
					data.putInt("y", target.getY());
					data.putInt("z", target.getZ());
					if (tile instanceof KineticTileEntity)
						((KineticTileEntity) tile).warnOfMovement();
					tile.a(state, data);
				}
			}
			state.b().a(world, target, state, null, stack);
		}

	}

	public static class ForBelt extends ForBlockState {
		public int length;

		public ForBelt() {}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			serializeNBT.putInt("Length", length);
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			length = nbt.getInt("Length");
			super.readNBT(nbt);
		}

		public ForBelt(BlockPos start, BlockPos target, ItemCooldownManager stack, PistonHandler state, int length) {
			super(start, target, stack, state, null);
			this.length = length;
		}

		@Override
		void place(GameMode world) {
			// todo place belt
			boolean isStart = state.c(BeltBlock.PART) == BeltPart.START;
			BlockPos offset = BeltBlock.nextSegmentPosition(state, BlockPos.ORIGIN, isStart);
			int i = length - 1;
			Axis axis = state.c(BeltBlock.HORIZONTAL_FACING).rotateYClockwise().getAxis();
			world.a(target, AllBlocks.SHAFT.getDefaultState().a(AbstractShaftBlock.AXIS, axis));
			BeltConnectorItem
					.createBelts(world, target, target.add(offset.getX() * i, offset.getY() * i, offset.getZ() * i));
		}

	}

	public static class ForEntity extends LaunchedItem {
		public apx entity;
		private CompoundTag deferredTag;

		ForEntity() {}

		public ForEntity(BlockPos start, BlockPos target, ItemCooldownManager stack, apx entity) {
			super(start, target, stack);
			this.entity = entity;
		}

		@Override
		public boolean update(GameMode world) {
			if (deferredTag != null && entity == null) {
				try {
					Optional<apx> loadEntityUnchecked = EntityDimensions.a(deferredTag, world);
					if (!loadEntityUnchecked.isPresent())
						return true;
					entity = loadEntityUnchecked.get();
				} catch (Exception var3) {
					return true;
				}
				deferredTag = null;
			}
			return super.update(world);
		}

		@Override
		public CompoundTag serializeNBT() {
			CompoundTag serializeNBT = super.serializeNBT();
			if (entity != null)
				serializeNBT.put("Entity", entity.serializeNBT());
			return serializeNBT;
		}

		@Override
		void readNBT(CompoundTag nbt) {
			super.readNBT(nbt);
			if (nbt.contains("Entity"))
				deferredTag = nbt.getCompound("Entity");
		}

		@Override
		void place(GameMode world) {
			world.c(entity);
		}

	}

}