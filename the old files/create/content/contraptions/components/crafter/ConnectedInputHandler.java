package com.simibubi.kinetic_api.content.contraptions.components.crafter;

import static com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public class ConnectedInputHandler {

	public static boolean shouldConnect(GameMode world, BlockPos pos, Direction face, Direction direction) {
		PistonHandler refState = world.d_(pos);
		if (!BlockHelper.hasBlockStateProperty(refState, HORIZONTAL_FACING))
			return false;
		Direction refDirection = refState.c(HORIZONTAL_FACING);
		if (direction.getAxis() == refDirection.getAxis())
			return false;
		if (face == refDirection)
			return false;
		PistonHandler neighbour = world.d_(pos.offset(direction));
		if (!AllBlocks.MECHANICAL_CRAFTER.has(neighbour))
			return false;
		if (refDirection != neighbour.c(HORIZONTAL_FACING))
			return false;
		return true;
	}

	public static void toggleConnection(GameMode world, BlockPos pos, BlockPos pos2) {
		MechanicalCrafterTileEntity crafter1 = CrafterHelper.getCrafter(world, pos);
		MechanicalCrafterTileEntity crafter2 = CrafterHelper.getCrafter(world, pos2);

		if (crafter1 == null || crafter2 == null)
			return;

		BlockPos controllerPos1 = crafter1.o()
			.add(crafter1.input.data.get(0));
		BlockPos controllerPos2 = crafter2.o()
			.add(crafter2.input.data.get(0));

		if (controllerPos1.equals(controllerPos2)) {
			MechanicalCrafterTileEntity controller = CrafterHelper.getCrafter(world, controllerPos1);

			Set<BlockPos> positions = controller.input.data.stream()
				.map(controllerPos1::add)
				.collect(Collectors.toSet());
			List<BlockPos> frontier = new LinkedList<>();
			List<BlockPos> splitGroup = new ArrayList<>();

			frontier.add(pos2);
			positions.remove(pos2);
			positions.remove(pos);
			while (!frontier.isEmpty()) {
				BlockPos current = frontier.remove(0);
				for (Direction direction : Iterate.directions) {
					BlockPos next = current.offset(direction);
					if (!positions.remove(next))
						continue;
					splitGroup.add(next);
					frontier.add(next);
				}
			}

			initAndAddAll(world, crafter1, positions);
			initAndAddAll(world, crafter2, splitGroup);

			crafter1.X_();
			crafter1.connectivityChanged();
			crafter2.X_();
			crafter2.connectivityChanged();
			return;
		}

		if (!crafter1.input.isController)
			crafter1 = CrafterHelper.getCrafter(world, controllerPos1);
		if (!crafter2.input.isController)
			crafter2 = CrafterHelper.getCrafter(world, controllerPos2);
		if (crafter1 == null || crafter2 == null)
			return;

		connectControllers(world, crafter1, crafter2);

		world.a(crafter1.o(), crafter1.p(), 3);

		crafter1.X_();
		crafter1.connectivityChanged();
		crafter2.X_();
		crafter2.connectivityChanged();
	}

	public static void initAndAddAll(GameMode world, MechanicalCrafterTileEntity crafter, Collection<BlockPos> positions) {
		crafter.input = new ConnectedInput();
		positions.forEach(splitPos -> {
			modifyAndUpdate(world, splitPos, input -> {
				input.attachTo(crafter.o(), splitPos);
				crafter.input.data.add(splitPos.subtract(crafter.o()));
			});
		});
	}

	public static void connectControllers(GameMode world, MechanicalCrafterTileEntity crafter1,
		MechanicalCrafterTileEntity crafter2) {

		crafter1.input.data.forEach(offset -> {
			BlockPos connectedPos = crafter1.o()
				.add(offset);
			modifyAndUpdate(world, connectedPos, input -> {
			});
		});

		crafter2.input.data.forEach(offset -> {
			if (offset.equals(BlockPos.ORIGIN))
				return;
			BlockPos connectedPos = crafter2.o()
				.add(offset);
			modifyAndUpdate(world, connectedPos, input -> {
				input.attachTo(crafter1.o(), connectedPos);
				crafter1.input.data.add(BlockPos.ORIGIN.subtract(input.data.get(0)));
			});
		});

		crafter2.input.attachTo(crafter1.o(), crafter2.o());
		crafter1.input.data.add(BlockPos.ORIGIN.subtract(crafter2.input.data.get(0)));
	}

	private static void modifyAndUpdate(GameMode world, BlockPos pos, Consumer<ConnectedInput> callback) {
		BeehiveBlockEntity te = world.c(pos);
		if (!(te instanceof MechanicalCrafterTileEntity))
			return;

		MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
		callback.accept(crafter.input);
		crafter.X_();
		crafter.connectivityChanged();
	}

	public static class ConnectedInput {
		boolean isController;
		List<BlockPos> data = Collections.synchronizedList(new ArrayList<>());

		public ConnectedInput() {
			isController = true;
			data.add(BlockPos.ORIGIN);
		}

		public void attachTo(BlockPos controllerPos, BlockPos myPos) {
			isController = false;
			data.clear();
			data.add(controllerPos.subtract(myPos));
		}

		public IItemHandler getItemHandler(GameMode world, BlockPos pos) {
			if (!isController) {
				BlockPos controllerPos = pos.add(data.get(0));
				ConnectedInput input = CrafterHelper.getInput(world, controllerPos);
				if (input == this || input == null || !input.isController)
					return new ItemStackHandler();
				return input.getItemHandler(world, controllerPos);
			}

			List<IItemHandlerModifiable> list = data.stream()
				.map(l -> CrafterHelper.getCrafter(world, pos.add(l)))
				.filter(Objects::nonNull)
				.map(crafter -> crafter.getInventory())
				.collect(Collectors.toList());
			return new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
		}

		public void write(CompoundTag nbt) {
			nbt.putBoolean("Controller", isController);
			ListTag list = new ListTag();
			data.forEach(pos -> list.add(NbtHelper.fromBlockPos(pos)));
			nbt.put("Data", list);
		}

		public void read(CompoundTag nbt) {
			isController = nbt.getBoolean("Controller");
			data.clear();
			nbt.getList("Data", NBT.TAG_COMPOUND)
				.forEach(inbt -> data.add(NbtHelper.toBlockPos((CompoundTag) inbt)));
		}

	}

}
