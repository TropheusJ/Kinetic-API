package com.simibubi.kinetic_api.content.contraptions.fluids.tank;

import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.BannerItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;

public class FluidTankItem extends BannerItem {

	public FluidTankItem(BeetrootsBlock p_i48527_1_, a p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public Difficulty a(PotionUtil ctx) {
		Difficulty initialResult = super.a(ctx);
		if (!initialResult.a())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	@Override
	protected boolean a(BlockPos p_195943_1_, GameMode p_195943_2_, PlayerAbilities p_195943_3_,
		ItemCooldownManager p_195943_4_, PistonHandler p_195943_5_) {
		MinecraftServer minecraftserver = p_195943_2_.l();
		if (minecraftserver == null)
			return false;
		CompoundTag nbt = p_195943_4_.b("BlockEntityTag");
		if (nbt != null) {
			nbt.remove("Luminosity");
			nbt.remove("Size");
			nbt.remove("Height");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
			if (nbt.contains("TankContent")) {
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
				if (!fluid.isEmpty()) {
					fluid.setAmount(Math.min(FluidTankTileEntity.getCapacityMultiplier(), fluid.getAmount()));
					nbt.put("TankContent", fluid.writeToNBT(new CompoundTag()));
				}
			}
		}
		return super.a(p_195943_1_, p_195943_2_, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	private void tryMultiPlace(PotionUtil ctx) {
		PlayerAbilities player = ctx.n();
		if (player == null)
			return;
		if (player.bt())
			return;
		Direction face = ctx.j();
		if (!face.getAxis()
			.isVertical())
			return;
		ItemCooldownManager stack = ctx.m();
		GameMode world = ctx.p();
		BlockPos pos = ctx.a();
		BlockPos placedOnPos = pos.offset(face.getOpposite());
		PistonHandler placedOnState = world.d_(placedOnPos);

		if (!FluidTankBlock.isTank(placedOnState))
			return;
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, placedOnPos);
		if (tankAt == null)
			return;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null)
			return;

		int width = controllerTE.width;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerTE.o()
			.down()
			: controllerTE.o()
				.up(controllerTE.height);

		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				PistonHandler blockState = world.d_(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				if (!blockState.c()
					.e())
					return;
				tanksToPlace++;
			}
		}

		if (!player.b_() && stack.E() < tanksToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				PistonHandler blockState = world.d_(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				PotionUtil context = PotionUtil.a(ctx, offsetPos, face);
				player.getPersistentData()
					.putBoolean("SilenceTankSound", true);
				super.a(context);
				player.getPersistentData()
					.remove("SilenceTankSound");
			}
		}
	}

}
