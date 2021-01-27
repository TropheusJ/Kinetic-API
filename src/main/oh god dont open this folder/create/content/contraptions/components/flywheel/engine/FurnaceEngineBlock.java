package com.simibubi.create.content.contraptions.components.flywheel.engine;

import btl;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.BannerItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FurnaceEngineBlock extends EngineBlock implements ITE<FurnaceEngineTileEntity> {

	public FurnaceEngineBlock(c properties) {
		super(properties);
	}

	@Override
	protected boolean isValidBaseBlock(PistonHandler baseBlock, MobSpawnerLogic world, BlockPos pos) {
		return baseBlock.b() instanceof btl;
	}

	@Override
	public VoxelShapes b(PistonHandler state, MobSpawnerLogic worldIn, BlockPos pos, ArrayVoxelShape context) {
		return AllShapes.FURNACE_ENGINE.get(state.c(aq));
	}

	@Override
	public AllBlockPartials getFrameModel() {
		return AllBlockPartials.FURNACE_GENERATOR_FRAME;
	}

	@Override
	public BeehiveBlockEntity createTileEntity(PistonHandler state, MobSpawnerLogic world) {
		return AllTileEntities.FURNACE_ENGINE.create();
	}

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, BeetrootsBlock blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.a(state, worldIn, pos, blockIn, fromPos, isMoving);
		if (worldIn instanceof WrappedWorld)
			return;
		if (worldIn.v)
			return;

		if (fromPos.equals(getBaseBlockPos(state, pos)))
			if (a(state, worldIn, pos))
				withTileEntityDo(worldIn, pos, FurnaceEngineTileEntity::updateFurnace);
	}

	@SubscribeEvent
	public static void usingFurnaceEngineOnFurnacePreventsGUI(RightClickBlock event) {
		ItemCooldownManager item = event.getItemStack();
		if (!(item.b() instanceof BannerItem))
			return;
		BannerItem blockItem = (BannerItem) item.b();
		if (blockItem.e() != AllBlocks.FURNACE_ENGINE.get())
			return;
		PistonHandler state = event.getWorld().d_(event.getPos());
		if (event.getFace().getAxis().isVertical())
			return;
		if (state.b() instanceof btl)
			event.setUseBlock(Result.DENY);
	}

	@Override
	public Class<FurnaceEngineTileEntity> getTileEntityClass() {
		return FurnaceEngineTileEntity.class;
	}

}
