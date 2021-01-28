package com.simibubi.create.content.contraptions.base;

//import com.simibubi.create.foundation.item.ItemDescription.Palette;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.*;
//import net.minecraftforge.common.ToolType;

public abstract class KineticBlock extends Block implements IRotate {

	//protected static final Palette color = Palette.Red; todo: tooltips(?)

	public KineticBlock(Settings properties) {
		super(properties);
	}
	//oh god these are forge things not minecraft things
	/*todo: is this even needed?
	@Override
	public ToolType getHarvestTool(BlockState state) {
		return null;
	}

	@Override
	public boolean canHarvestBlock(BlockState state, MobSpawnerLogic world, BlockPos pos, PlayerAbilities player) {
		for (ToolType toolType : player.getStackInHand().getToolTypes()) {
			if (isToolEffective(state, toolType))
				return true;
		}
		return super.canHarvestBlock(state, world, pos, player);
	}

	public boolean isToolEffective(BlockState state, ToolType tool) {
		return tool == ToolType.AXE || tool == ToolType.PICKAXE;
	}
todo: possibly redundant/unused

	@Override
	public void a(PistonHandler state, GameMode worldIn, BlockPos pos, PistonHandler oldState, boolean isMoving) {
		// onBlockAdded is useless for init, as sometimes the TE gets re-instantiated
	}
*/
	//@Override todo: see if these overrides are important. they probably are. kill me.
	public boolean hasShaftTowards(WorldView world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	//@Override
	public boolean hasIntegratedCogwheel(WorldView world, BlockPos pos, BlockState state) {
		return false;
	}

	//@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	//@Override
	public abstract BlockEntity createTileEntity(BlockState state, MobSpawnerLogic world);

	// TODO 1.16 is this the right replacement for updateNeighbors?
	//@Override
	/*todo: why does this error why does this error why does this error why does this error why does this error why does this error why does this error why does this error why does this error why does this error why does this error why does this error
	@Deprecated
	public void getStateForNeighborUpdate(BlockState stateIn, WorldAccess worldIn, BlockPos pos, int flags, int count) {
		super.getStateForNeighborUpdate(stateIn, worldIn, pos, flags, count);
		if (worldIn.isClient())
			return;
todo: kineticTileEntity

		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		// Remove previous information when block is added
		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.warnOfMovement();
		kte.clearKineticInformation();
		kte.updateSpeed = true;
	}
*/
	@Override
	public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (worldIn.isClient)
			return;
/*todo: kineticTileEntity
		BeehiveBlockEntity tileEntity = worldIn.c(pos);
		if (!(tileEntity instanceof KineticTileEntity))
			return;

		KineticTileEntity kte = (KineticTileEntity) tileEntity;
		kte.effects.queueRotationIndicators();
	}
todo: KineticAffectHandler

	public float getParticleTargetRadius() {
		return .65f;
	}

	public float getParticleInitialRadius() {
		return .75f;*/
	}

}
