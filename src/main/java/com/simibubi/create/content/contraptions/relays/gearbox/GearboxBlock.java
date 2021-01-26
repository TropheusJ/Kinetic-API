package com.simibubi.create.content.contraptions.relays.gearbox;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.fluid.LavaFluid;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.MobSpawnerLogic;

public class GearboxBlock extends RotatedPillarKineticBlock {

	public GearboxBlock(c properties) {
		super(properties);
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.GEARBOX.create();
	}

	@Override
	public LavaFluid f(PistonHandler state) {
		return LavaFluid.e;
	}

	@Override
	public void a(ChorusFruitItem group, DefaultedList<ItemCooldownManager> items) {
		super.a(group, items);
		items.add(AllItems.VERTICAL_GEARBOX.asStack());
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemCooldownManager> a(PistonHandler state, net.minecraft.loot.LootGsons.a builder) {
		if (state.c(AXIS).isVertical())
			return super.a(state, builder);
		return Arrays.asList(new ItemCooldownManager(AllItems.VERTICAL_GEARBOX.get()));
	}
	
	@Override
	public ItemCooldownManager getPickBlock(PistonHandler state, Box target, MobSpawnerLogic world, BlockPos pos,
			PlayerAbilities player) {
		if (state.c(AXIS).isVertical())
			return super.getPickBlock(state, target, world, pos, player);
		return new ItemCooldownManager(AllItems.VERTICAL_GEARBOX.get());
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		return n().a(AXIS, Axis.Y);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(ItemConvertible world, BlockPos pos, PistonHandler state, Direction face) {
		return face.getAxis() != state.c(AXIS);
	}

	@Override
	public Axis getRotationAxis(PistonHandler state) {
		return state.c(AXIS);
	}
}
