package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import apx;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.DyeHelper;
import cww;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.DebugStickItem;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.timer.Timer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SeatBlock extends BeetrootsBlock {

	private final boolean inCreativeTab;

	public SeatBlock(c p_i48440_1_, boolean inCreativeTab) {
		super(p_i48440_1_);
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> p_149666_2_) {
		if (group != ChorusFruitItem.g && !inCreativeTab)
			return;
		super.a(group, p_149666_2_);
	}

	@Override
	public void a(GameMode p_180658_1_, BlockPos p_180658_2_, apx p_180658_3_, float p_180658_4_) {
		super.a(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_ * 0.5F);
	}

	@Override
	public void a(MobSpawnerLogic reader, apx entity) {
		BlockPos pos = entity.cA();
		if (entity instanceof PlayerAbilities || !(entity instanceof SaddledComponent) || !canBePickedUp(entity) || isSeatOccupied(entity.l, pos)) {
			BellBlock.aD.a(reader, entity);
			return;
		}
		if (reader.d_(pos)
			.b() != this)
			return;
		sitDown(entity.l, pos, entity);
	}

	@Override
	public cww getAiPathNodeType(PistonHandler state, MobSpawnerLogic world, BlockPos pos,
		@Nullable ItemSteerable entity) {
		return cww.j;
	}

	@Override
	public VoxelShapes b(PistonHandler p_220053_1_, MobSpawnerLogic p_220053_2_, BlockPos p_220053_3_,
		ArrayVoxelShape p_220053_4_) {
		return AllShapes.SEAT;
	}

	@Override
	public VoxelShapes c(PistonHandler p_220071_1_, MobSpawnerLogic p_220071_2_, BlockPos p_220071_3_,
		ArrayVoxelShape p_220071_4_) {
		return AllShapes.SEAT_COLLISION;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg p_225533_6_) {
		if (player.bt())
			return Difficulty.PASS;

		ItemCooldownManager heldItem = player.b(hand);
		for (DebugStickItem color : DebugStickItem.values()) {
			if (!heldItem.b()
				.a(DyeHelper.getTagOfDye(color)))
				continue;
			if (world.v)
				return Difficulty.SUCCESS;

			PistonHandler newState = AllBlocks.SEATS[color.ordinal()].getDefaultState();
			if (newState != state)
				world.a(pos, newState);
			return Difficulty.SUCCESS;
		}

		List<SeatEntity> seats = world.a(SeatEntity.class, new Timer(pos));
		if (!seats.isEmpty()) {
			SeatEntity seatEntity = seats.get(0);
			List<apx> passengers = seatEntity.cm();
			if (!passengers.isEmpty() && passengers.get(0) instanceof PlayerAbilities)
				return Difficulty.PASS;
			if (!world.v) {
				seatEntity.bd();
				player.m(seatEntity);
			}
			return Difficulty.SUCCESS;
		}

		if (world.v)
			return Difficulty.SUCCESS;
		sitDown(world, pos, player);
		return Difficulty.SUCCESS;
	}

	public static boolean isSeatOccupied(GameMode world, BlockPos pos) {
		return !world.a(SeatEntity.class, new Timer(pos))
			.isEmpty();
	}

	public static boolean canBePickedUp(apx passenger) {
		return !(passenger instanceof PlayerAbilities) && (passenger instanceof SaddledComponent);
	}

	public static void sitDown(GameMode world, BlockPos pos, apx entity) {
		if (world.v)
			return;
		SeatEntity seat = new SeatEntity(world, pos);
		seat.o(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
		world.c(seat);
		entity.a(seat, true);
	}

}
