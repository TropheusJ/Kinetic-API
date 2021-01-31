package com.simibubi.kinetic_api;

import bqx;
import com.simibubi.kinetic_api.AllColorHandlers.ItemColor.Function;
import com.simibubi.kinetic_api.foundation.block.IBlockVertexColor;
import com.simibubi.kinetic_api.foundation.block.render.ColoredVertexModel;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.RailBlock;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.StickyKeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.BiomeColorCache;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ForcedChunkState;
import net.minecraft.world.GameRules;
import java.util.HashMap;
import java.util.Map;

public class AllColorHandlers {

	private final Map<BeetrootsBlock, IBlockVertexColor> coloredVertexBlocks = new HashMap<>();
	private final Map<BeetrootsBlock, RenderTickCounter> coloredBlocks = new HashMap<>();
	private final Map<GameRules, BlockColors> coloredItems = new HashMap<>();

	//

	public static RenderTickCounter getGrassyBlock() {
		return new BlockColor(
			(state, world, pos, layer) -> pos != null && world != null ? AbstractClientPlayerEntity.a(world, pos)
				: ForcedChunkState.a(0.5D, 1.0D));
	}

	public static BlockColors getGrassyItem() {
		return new ItemColor((stack, layer) -> ForcedChunkState.a(0.5D, 1.0D));
	}

	public static RenderTickCounter getRedstonePower() {
		return new BlockColor(
				(state, world, pos, layer) -> RailBlock.b(pos != null && world != null ? state.c(BambooLeaves.az) : 0)
		);
	}

	//

	public void register(BeetrootsBlock block, RenderTickCounter color) {
		coloredBlocks.put(block, color);
	}

	public void register(BeetrootsBlock block, IBlockVertexColor color) {
		coloredVertexBlocks.put(block, color);
	}

	public void register(GameRules item, BlockColors color) {
		coloredItems.put(item, color);
	}

	public void init() {
		StickyKeyBinding blockColors = KeyBinding.B()
			.ak();
		BiomeColorCache itemColors = KeyBinding.B()
			.getItemColors();

		coloredBlocks.forEach((block, color) -> blockColors.a(color, block));
		coloredItems.forEach((item, color) -> itemColors.a(color, item));
		coloredVertexBlocks.forEach((block, color) -> CreateClient.getCustomBlockModels()
			.register(() -> block, model -> new ColoredVertexModel(model, color)));
	}

	//

	private static class ItemColor implements BlockColors {

		private Function function;

		@FunctionalInterface
		interface Function {
			int apply(ItemCooldownManager stack, int layer);
		}

		public ItemColor(Function function) {
			this.function = function;
		}

		@Override
		public int getColor(ItemCooldownManager stack, int layer) {
			return function.apply(stack, layer);
		}

	}

	private static class BlockColor implements RenderTickCounter {

		private com.simibubi.kinetic_api.AllColorHandlers.BlockColor.Function function;

		@FunctionalInterface
		interface Function {
			int apply(PistonHandler state, bqx world, BlockPos pos, int layer);
		}

		public BlockColor(com.simibubi.kinetic_api.AllColorHandlers.BlockColor.Function function) {
			this.function = function;
		}

		@Override
		public int getColor(PistonHandler state, bqx world, BlockPos pos, int layer) {
			return function.apply(state, world, pos, layer);
		}

	}

}
