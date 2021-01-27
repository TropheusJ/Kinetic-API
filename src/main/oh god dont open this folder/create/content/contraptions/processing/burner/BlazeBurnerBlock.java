package com.simibubi.create.content.contraptions.processing.burner;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import afj;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Lang;
import dcg;
import mcp.MethodsReturnNonnullByDefault;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.util.SmoothUtil;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ChorusFruitItem;
import net.minecraft.item.FireworkChargeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.loot.condition.InvertedLootCondition.Serializer;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.potion.PotionUtil;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.MutableWorldProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlazeBurnerBlock extends BeetrootsBlock implements ITE<BlazeBurnerTileEntity> {

	public static final IntProperty<HeatLevel> HEAT_LEVEL = DirectionProperty.a("blaze", HeatLevel.class);

	public BlazeBurnerBlock(c properties) {
		super(properties);
		j(super.n().a(HEAT_LEVEL, HeatLevel.NONE));
	}

	@Override
	protected void a(cef.a<BeetrootsBlock, PistonHandler> builder) {
		super.a(builder);
		builder.a(HEAT_LEVEL);
	}

	@Override
	public void b(PistonHandler state, GameMode world, BlockPos pos, PistonHandler p_220082_4_, boolean p_220082_5_) {
		if (world.v)
			return;
		BeehiveBlockEntity tileEntity = world.c(pos.up());
		if (!(tileEntity instanceof BasinTileEntity))
			return;
		BasinTileEntity basin = (BasinTileEntity) tileEntity;
		basin.notifyChangeOfContents();
	}

	@Override
	public boolean hasTileEntity(PistonHandler state) {
		return state.c(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING);
	}

	@Override
	public void a(ChorusFruitItem p_149666_1_, DefaultedList<ItemCooldownManager> p_149666_2_) {
		p_149666_2_.add(AllItems.EMPTY_BLAZE_BURNER.asStack());
		super.a(p_149666_1_, p_149666_2_);
	}

	@Nullable
	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.HEATER.create();
	}

	@Override
	public Class<BlazeBurnerTileEntity> getTileEntityClass() {
		return BlazeBurnerTileEntity.class;
	}

	@Override
	public Difficulty a(PistonHandler state, GameMode world, BlockPos pos, PlayerAbilities player, ItemScatterer hand,
		dcg blockRayTraceResult) {
		ItemCooldownManager heldItem = player.b(hand);
		boolean dontConsume = player.b_();
		boolean forceOverflow = !(player instanceof FakePlayer);

		if (!state.hasTileEntity()) {
			if (heldItem.b() instanceof FireworkChargeItem) {
				world.a(player, pos, MusicType.eo, SoundEvent.e, 1.0F,
					world.t.nextFloat() * 0.4F + 0.8F);
				if (world.v)
					return Difficulty.SUCCESS;
				heldItem.a(1, player, p -> p.d(hand));
				world.a(pos, AllBlocks.LIT_BLAZE_BURNER.getDefaultState());
				return Difficulty.SUCCESS;
			}
			return Difficulty.PASS;
		}

		LocalDifficulty<ItemCooldownManager> res = tryInsert(state, world, pos, dontConsume ? heldItem.i() : heldItem, forceOverflow, false);
		ItemCooldownManager leftover = res.b();
		if (!world.v && !dontConsume && !leftover.a()) {
			if (heldItem.a()) {
				player.a(hand, leftover);
			} else if (!player.bm.e(leftover)) {
				player.a(leftover, false);
			}
		}

		return res.getGlobalDifficulty() == Difficulty.SUCCESS ? res.getGlobalDifficulty() : Difficulty.PASS;
	}

	public static LocalDifficulty<ItemCooldownManager> tryInsert(PistonHandler state, GameMode world, BlockPos pos, ItemCooldownManager stack, boolean forceOverflow,
		boolean simulate) {
		if (!state.hasTileEntity())
			return LocalDifficulty.d(ItemCooldownManager.tick);

		BeehiveBlockEntity te = world.c(pos);
		if (!(te instanceof BlazeBurnerTileEntity))
			return LocalDifficulty.d(ItemCooldownManager.tick);
		BlazeBurnerTileEntity burnerTE = (BlazeBurnerTileEntity) te;

		if (!burnerTE.tryUpdateFuel(stack, forceOverflow, simulate))
			return LocalDifficulty.d(ItemCooldownManager.tick);
		
		ItemCooldownManager container = stack.getContainerItem();
		if (!simulate && !world.v) {
			world.a(null, pos, MusicType.aP, SoundEvent.e,
				.125f + world.t.nextFloat() * .125f, .75f - world.t.nextFloat() * .25f);
			stack.g(1);
		}
		if (!container.a()) {
			return LocalDifficulty.a(container);
		}
		return LocalDifficulty.a(ItemCooldownManager.tick);
	}

	@Override
	public PistonHandler a(PotionUtil context) {
		ItemCooldownManager stack = context.m();
		HoeItem item = stack.b();
		PistonHandler defaultState = n();
		if (!(item instanceof BlazeBurnerBlockItem))
			return defaultState;
		HeatLevel initialHeat =
			((BlazeBurnerBlockItem) item).hasCapturedBlaze() ? HeatLevel.SMOULDERING : HeatLevel.NONE;
		return defaultState.a(HEAT_LEVEL, initialHeat);
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic reader, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShapes c(PistonHandler p_220071_1_, MobSpawnerLogic p_220071_2_, BlockPos p_220071_3_,
		ArrayVoxelShape p_220071_4_) {
		if (p_220071_4_ == ArrayVoxelShape.a())
			return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return b(p_220071_1_, p_220071_2_, p_220071_3_, p_220071_4_);
	}

	@Override
	public int getLightValue(PistonHandler state, MobSpawnerLogic world, BlockPos pos) {
		return afj.a(state.c(HEAT_LEVEL)
			.ordinal() * 4 - 1, 0, 15);
	}

	public static HeatLevel getHeatLevelOf(PistonHandler blockState) {
		return blockState.b(BlazeBurnerBlock.HEAT_LEVEL) ? blockState.c(BlazeBurnerBlock.HEAT_LEVEL)
			: HeatLevel.NONE;
	}

	public static LootContext.Builder buildLootTable() {
		Serializer survivesExplosion = LootConditionConsumingBuilder.c();
		BlazeBurnerBlock block = AllBlocks.BLAZE_BURNER.get();

		LootContext.Builder builder = LootContext.b();
		BoundedIntUnaryOperator.Serializer poolBuilder = BoundedIntUnaryOperator.a();
		for (HeatLevel level : HeatLevel.values()) {
			GameRules drop =
				level == HeatLevel.NONE ? AllItems.EMPTY_BLAZE_BURNER.get() : AllBlocks.BLAZE_BURNER.get();
			poolBuilder.a(DynamicEntry.a(drop)
				.a(survivesExplosion)
				.a(SurvivesExplosionLootCondition.a(block)
					.a(StatePredicate.Builder.create()
						.a(HEAT_LEVEL, level))));
		}
		builder.a(poolBuilder.a(MutableWorldProperties.a(1)));
		return builder;
	}
	
	@Override
	public boolean a(PistonHandler p_149740_1_) {
		return true;
	}
	
	@Override
	public int a(PistonHandler state, GameMode p_180641_2_, BlockPos p_180641_3_) {
		return Math.max(0, state.c(HEAT_LEVEL).ordinal() -1);
	}

	@Environment(EnvType.CLIENT)
	public void a(PistonHandler state, GameMode world, BlockPos pos, Random random) {
		if (random.nextInt(10) != 0)
			return;
		if (!state.c(HEAT_LEVEL)
			.isAtLeast(HeatLevel.SMOULDERING))
			return;
		world.a((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
			(double) ((float) pos.getZ() + 0.5F), MusicType.bp, SoundEvent.e,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}

	public enum HeatLevel implements SmoothUtil {
		NONE, SMOULDERING, FADING, KINDLED, SEETHING,;

		public static HeatLevel byIndex(int index) {
			return values()[index];
		}

		@Override
		public String a() {
			return Lang.asId(name());
		}

		public boolean isAtLeast(HeatLevel heatLevel) {
			return this.ordinal() >= heatLevel.ordinal();
		}
	}
}
