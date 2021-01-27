package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import apx;
import bnx;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.OrientedContraptionEntity;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.ai.brain.ScheduleBuilder.ActivityEntry;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.HoeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class MinecartContraptionItem extends HoeItem {

	private final ScheduleBuilder.ActivityEntry minecartType;

	public static MinecartContraptionItem rideable(a builder) {
		return new MinecartContraptionItem(ActivityEntry.startTime, builder);
	}

	public static MinecartContraptionItem furnace(a builder) {
		return new MinecartContraptionItem(ActivityEntry.c, builder);
	}

	public static MinecartContraptionItem chest(a builder) {
		return new MinecartContraptionItem(ActivityEntry.activity, builder);
	}

	private MinecartContraptionItem(ActivityEntry minecartTypeIn, a builder) {
		super(builder);
		this.minecartType = minecartTypeIn;
		DetectorRailBlock.a(this, DISPENSER_BEHAVIOR);
	}

	// Taken and adjusted from MinecartItem
	private static final DispenserBehavior DISPENSER_BEHAVIOR = new ItemDispenserBehavior() {
		private final ItemDispenserBehavior behaviourDefaultDispenseItem = new ItemDispenserBehavior();

		@Override
		public ItemCooldownManager a(BlockPointer source, ItemCooldownManager stack) {
			Direction direction = source.e()
				.c(DetectorRailBlock.a);
			GameMode world = source.getWorld();
			double d0 = source.getX() + (double) direction.getOffsetX() * 1.125D;
			double d1 = Math.floor(source.getY()) + (double) direction.getOffsetY();
			double d2 = source.getZ() + (double) direction.getOffsetZ() * 1.125D;
			BlockPos blockpos = source.getBlockPos()
				.offset(direction);
			PistonHandler blockstate = world.d_(blockpos);
			Instrument railshape = blockstate.b() instanceof BlockWithEntity
				? ((BlockWithEntity) blockstate.b()).getRailDirection(blockstate, world, blockpos, null)
				: Instrument.NORTH_SOUTH;
			double d3;
			if (blockstate.a(StatHandler.H)) {
				if (railshape.c()) {
					d3 = 0.6D;
				} else {
					d3 = 0.1D;
				}
			} else {
				if (!blockstate.isAir(world, blockpos) || !world.d_(blockpos.down())
					.a(StatHandler.H)) {
					return this.behaviourDefaultDispenseItem.dispense(source, stack);
				}

				PistonHandler blockstate1 = world.d_(blockpos.down());
				Instrument railshape1 = blockstate1.b() instanceof BlockWithEntity
					? ((BlockWithEntity) blockstate1.b()).getRailDirection(blockstate1, world, blockpos.down(),
						null)
					: Instrument.NORTH_SOUTH;
				if (direction != Direction.DOWN && railshape1.c()) {
					d3 = -0.4D;
				} else {
					d3 = -0.9D;
				}
			}

			ScheduleBuilder abstractminecartentity = ScheduleBuilder.a(world, d0, d1 + d3, d2,
				((MinecartContraptionItem) stack.b()).minecartType);
			if (stack.t())
				abstractminecartentity.a(stack.r());
			world.c(abstractminecartentity);
			addContraptionToMinecart(world, stack, abstractminecartentity, direction);

			stack.g(1);
			return stack;
		}

		@Override
		protected void playSound(BlockPointer source) {
			source.getWorld()
				.syncWorldEvent(1000, source.getBlockPos(), 0);
		}
	};

	// Taken and adjusted from MinecartItem
	@Override
	public Difficulty a(bnx context) {
		GameMode world = context.p();
		BlockPos blockpos = context.a();
		PistonHandler blockstate = world.d_(blockpos);
		if (!blockstate.a(StatHandler.H)) {
			return Difficulty.FAIL;
		} else {
			ItemCooldownManager itemstack = context.m();
			if (!world.v) {
				Instrument railshape = blockstate.b() instanceof BlockWithEntity
					? ((BlockWithEntity) blockstate.b()).getRailDirection(blockstate, world, blockpos, null)
					: Instrument.NORTH_SOUTH;
				double d0 = 0.0D;
				if (railshape.c()) {
					d0 = 0.5D;
				}

				ScheduleBuilder abstractminecartentity =
					ScheduleBuilder.a(world, (double) blockpos.getX() + 0.5D,
						(double) blockpos.getY() + 0.0625D + d0, (double) blockpos.getZ() + 0.5D, this.minecartType);
				if (itemstack.t())
					abstractminecartentity.a(itemstack.r());
				PlayerAbilities player = context.n();
				world.c(abstractminecartentity);
				addContraptionToMinecart(world, itemstack, abstractminecartentity,
					player == null ? null : player.bY());
			}

			itemstack.g(1);
			return Difficulty.SUCCESS;
		}
	}

	public static void addContraptionToMinecart(GameMode world, ItemCooldownManager itemstack, ScheduleBuilder cart,
		@Nullable Direction newFacing) {
		CompoundTag tag = itemstack.p();
		if (tag.contains("Contraption")) {
			CompoundTag contraptionTag = tag.getCompound("Contraption");

			Optional<Direction> intialOrientation = Optional.empty();
			if (contraptionTag.contains("InitialOrientation"))
				intialOrientation =
					Optional.of(NBTHelper.readEnum(contraptionTag, "InitialOrientation", Direction.class));

			Contraption mountedContraption = Contraption.fromNBT(world, contraptionTag, false);
			OrientedContraptionEntity contraptionEntity =
				OrientedContraptionEntity.create(world, mountedContraption, intialOrientation);

			contraptionEntity.m(cart);
			contraptionEntity.d(cart.cC(), cart.cD(), cart.cG());
			world.c(contraptionEntity);
		}
	}

	@Override
	public String f(ItemCooldownManager stack) {
		return "item.create.minecart_contraption";
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {}

	@SubscribeEvent
	public static void wrenchCanBeUsedToPickUpMinecartContraptions(PlayerInteractEvent.EntityInteract event) {
		apx entity = event.getTarget();
		PlayerAbilities player = event.getPlayer();
		if (player == null || entity == null)
			return;

		ItemCooldownManager wrench = player.b(event.getHand());
		if (!AllItems.WRENCH.isIn(wrench))
			return;
		if (entity instanceof AbstractContraptionEntity)
			entity = entity.cs();
		if (!(entity instanceof ScheduleBuilder))
			return;
		ScheduleBuilder cart = (ScheduleBuilder) entity;
		ActivityEntry type = cart.o();
		if (type != ActivityEntry.startTime && type != ActivityEntry.c && type != ActivityEntry.activity)
			return;
		List<apx> passengers = cart.cm();
		if (passengers.isEmpty() || !(passengers.get(0) instanceof OrientedContraptionEntity))
			return;
		OrientedContraptionEntity contraption = (OrientedContraptionEntity) passengers.get(0);

		if (!event.getWorld().v) {
			player.bm.a(event.getWorld(), create(type, contraption).a(entity.S()));
			contraption.ac();
			entity.ac();
		}

		event.setCancellationResult(Difficulty.SUCCESS);
		event.setCanceled(true);
	}

	public static ItemCooldownManager create(ActivityEntry type, OrientedContraptionEntity entity) {
		ItemCooldownManager stack = ItemCooldownManager.tick;

		switch (type) {
		case startTime:
			stack = AllItems.MINECART_CONTRAPTION.asStack();
			break;
		case c:
			stack = AllItems.FURNACE_MINECART_CONTRAPTION.asStack();
			break;
		case activity:
			stack = AllItems.CHEST_MINECART_CONTRAPTION.asStack();
			break;
		default:
			break;
		}

		if (stack.a())
			return stack;

		CompoundTag tag = entity.getContraption()
			.writeNBT(false);
		tag.remove("UUID");
		tag.remove("Pos");
		tag.remove("Motion");

		if (entity.isInitialOrientationPresent())
			NBTHelper.writeEnum(tag, "InitialOrientation", entity.getInitialOrientation());

		stack.p()
			.put("Contraption", tag);
		return stack;
	}
}
