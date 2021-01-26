package com.simibubi.create.foundation.fluid;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.fluids.actors.GenericItemFilling;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.Pair;
import cut;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;

public class FluidHelper {

	public static enum FluidExchange {
		ITEM_TO_TANK, TANK_TO_ITEM;
	}

	public static boolean isWater(cut fluid) {
		return convertToStill(fluid) == FlowableFluid.c;
	}

	public static boolean isLava(cut fluid) {
		return convertToStill(fluid) == FlowableFluid.field_15901;
	}
	
	public static boolean hasBlockState(cut fluid) {
		PistonHandler blockState = fluid.h().g();
		return blockState != null && blockState != BellBlock.FACING.n();
	}
	
	public static FluidStack copyStackWithAmount(FluidStack fs, int amount) {
		if (fs.isEmpty())
			return FluidStack.EMPTY;
		FluidStack copy = fs.copy();
		copy.setAmount(amount);
		return copy;
	}

	public static cut convertToFlowing(cut fluid) {
		if (fluid == FlowableFluid.c)
			return FlowableFluid.LEVEL;
		if (fluid == FlowableFluid.field_15901)
			return FlowableFluid.d;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).d();
		return fluid;
	}

	public static cut convertToStill(cut fluid) {
		if (fluid == FlowableFluid.LEVEL)
			return FlowableFluid.c;
		if (fluid == FlowableFluid.d)
			return FlowableFluid.field_15901;
		if (fluid instanceof ForgeFlowingFluid)
			return ((ForgeFlowingFluid) fluid).e();
		return fluid;
	}

	public static JsonElement serializeFluidStack(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", stack.getFluid()
			.getRegistryName()
			.toString());
		json.addProperty("amount", stack.getAmount());
		if (stack.hasTag())
			json.addProperty("nbt", stack.getTag()
				.toString());
		return json;
	}

	public static FluidStack deserializeFluidStack(JsonObject json) {
		Identifier id = new Identifier(OrderedText.h(json, "fluid"));
		cut fluid = ForgeRegistries.FLUIDS.getValue(id);
		if (fluid == null)
			throw new JsonSyntaxException("Unknown fluid '" + id + "'");
		int amount = OrderedText.n(json, "amount");
		FluidStack stack = new FluidStack(fluid, amount);

		if (!json.has("nbt"))
			return stack;

		try {
			JsonElement element = json.get("nbt");
			stack.setTag(StringNbtReader.parse(
				element.isJsonObject() ? Create.GSON.toJson(element) : OrderedText.a(element, "nbt")));

		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		return stack;
	}

	public static boolean tryEmptyItemIntoTE(GameMode worldIn, PlayerAbilities player, ItemScatterer handIn, ItemCooldownManager heldItem,
		SmartTileEntity te) {
		if (!EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return false;

		Pair<FluidStack, ItemCooldownManager> emptyingResult = EmptyingByBasin.emptyItem(worldIn, heldItem, true);
		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);
		FluidStack fluidStack = emptyingResult.getFirst();

		if (tank == null || fluidStack.getAmount() != tank.fill(fluidStack, FluidAction.SIMULATE))
			return false;
		if (worldIn.v)
			return true;

		ItemCooldownManager copyOfHeld = heldItem.i();
		emptyingResult = EmptyingByBasin.emptyItem(worldIn, copyOfHeld, false);
		tank.fill(fluidStack, FluidAction.EXECUTE);

		if (!player.b_()) {
			if (copyOfHeld.a())
				player.a(handIn, emptyingResult.getSecond());
			else {
				player.a(handIn, copyOfHeld);
				player.bm.a(worldIn, emptyingResult.getSecond());
			}
		}
		return true;
	}

	public static boolean tryFillItemFromTE(GameMode world, PlayerAbilities player, ItemScatterer handIn, ItemCooldownManager heldItem,
		SmartTileEntity te) {
		if (!GenericItemFilling.canItemBeFilled(world, heldItem))
			return false;

		LazyOptional<IFluidHandler> capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		IFluidHandler tank = capability.orElse(null);

		if (tank == null)
			return false;

		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluid = tank.getFluidInTank(i);
			if (fluid.isEmpty())
				continue;
			int requiredAmountForItem = GenericItemFilling.getRequiredAmountForItem(world, heldItem, fluid.copy());
			if (requiredAmountForItem == -1)
				continue;
			if (requiredAmountForItem > fluid.getAmount())
				continue;

			if (world.v)
				return true;

			if (player.b_())
				heldItem = heldItem.i();
			ItemCooldownManager out = GenericItemFilling.fillItem(world, requiredAmountForItem, heldItem, fluid.copy());

			FluidStack copy = fluid.copy();
			copy.setAmount(requiredAmountForItem);
			tank.drain(copy, FluidAction.EXECUTE);

			if (!player.b_())
				player.bm.a(world, out);
			te.notifyUpdate();
			return true;
		}

		return false;
	}

	@Nullable
	public static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
		int maxAmount) {
		return exchange(fluidTank, fluidItem, preferred, true, maxAmount);
	}

	@Nullable
	public static FluidExchange exchangeAll(IFluidHandler fluidTank, IFluidHandlerItem fluidItem,
		FluidExchange preferred) {
		return exchange(fluidTank, fluidItem, preferred, false, Integer.MAX_VALUE);
	}

	@Nullable
	private static FluidExchange exchange(IFluidHandler fluidTank, IFluidHandlerItem fluidItem, FluidExchange preferred,
		boolean singleOp, int maxTransferAmountPerTank) {

		// Locks in the transfer direction of this operation
		FluidExchange lockedExchange = null;

		for (int tankSlot = 0; tankSlot < fluidTank.getTanks(); tankSlot++) {
			for (int slot = 0; slot < fluidItem.getTanks(); slot++) {

				FluidStack fluidInTank = fluidTank.getFluidInTank(tankSlot);
				int tankCapacity = fluidTank.getTankCapacity(tankSlot) - fluidInTank.getAmount();
				boolean tankEmpty = fluidInTank.isEmpty();

				FluidStack fluidInItem = fluidItem.getFluidInTank(tankSlot);
				int itemCapacity = fluidItem.getTankCapacity(tankSlot) - fluidInItem.getAmount();
				boolean itemEmpty = fluidInItem.isEmpty();

				boolean undecided = lockedExchange == null;
				boolean canMoveToTank = (undecided || lockedExchange == FluidExchange.ITEM_TO_TANK) && tankCapacity > 0;
				boolean canMoveToItem = (undecided || lockedExchange == FluidExchange.TANK_TO_ITEM) && itemCapacity > 0;

				// Incompatible Liquids
				if (!tankEmpty && !itemEmpty && !fluidInItem.isFluidEqual(fluidInTank))
					continue;

				// Transfer liquid to tank
				if (((tankEmpty || itemCapacity <= 0) && canMoveToTank)
					|| undecided && preferred == FluidExchange.ITEM_TO_TANK) {

					int amount = fluidTank.fill(
						fluidItem.drain(Math.min(maxTransferAmountPerTank, tankCapacity), FluidAction.EXECUTE),
						FluidAction.EXECUTE);
					if (amount > 0) {
						lockedExchange = FluidExchange.ITEM_TO_TANK;
						if (singleOp)
							return lockedExchange;
						continue;
					}
				}

				// Transfer liquid from tank
				if (((itemEmpty || tankCapacity <= 0) && canMoveToItem)
					|| undecided && preferred == FluidExchange.TANK_TO_ITEM) {

					int amount = fluidItem.fill(
						fluidTank.drain(Math.min(maxTransferAmountPerTank, itemCapacity), FluidAction.EXECUTE),
						FluidAction.EXECUTE);
					if (amount > 0) {
						lockedExchange = FluidExchange.TANK_TO_ITEM;
						if (singleOp)
							return lockedExchange;
						continue;
					}

				}

			}
		}

		return null;
	}

}
