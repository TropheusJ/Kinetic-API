package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted;

import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerBlock.RAIL_SHAPE;

import apx;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.mounted.CartAssemblerTileEntity.CartMovementMode;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.InvWrapper;

public class MountedContraption extends Contraption {

	public CartMovementMode rotationMode;
	public ScheduleBuilder connectedCart;

	public MountedContraption() {
		this(CartMovementMode.ROTATE);
	}

	public MountedContraption(CartMovementMode mode) {
		rotationMode = mode;
	}

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.MOUNTED;
	}
	
	@Override
	public boolean assemble(GameMode world, BlockPos pos) {
		PistonHandler state = world.d_(pos);
		if (!BlockHelper.hasBlockStateProperty(state, RAIL_SHAPE))
			return false;
		if (!searchMovedStructure(world, pos, null))
			return false;
		
		Axis axis = state.c(RAIL_SHAPE) == Instrument.EAST_WEST ? Axis.X : Axis.Z;
		addBlock(pos, Pair.of(new c(pos, AllBlocks.MINECART_ANCHOR.getDefaultState()
			.a(BambooLeaves.E, axis), null), null));
		
		if (blocks.size() == 1)
			return false;
		
		return true;
	}
	
	@Override
	protected boolean addToInitialFrontier(GameMode world, BlockPos pos, Direction direction, List<BlockPos> frontier) {
		frontier.clear();
		frontier.add(pos.up());
		return true;
	}

	@Override
	protected Pair<c, BeehiveBlockEntity> capture(GameMode world, BlockPos pos) {
		Pair<c, BeehiveBlockEntity> pair = super.capture(world, pos);
		c capture = pair.getKey();
		if (!AllBlocks.CART_ASSEMBLER.has(capture.b))
			return pair;

		Pair<c, BeehiveBlockEntity> anchorSwap =
			Pair.of(new c(pos, CartAssemblerBlock.createAnchor(capture.b), null), pair.getValue());
		if (pos.equals(anchor) || connectedCart != null)
			return anchorSwap;

		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (ScheduleBuilder abstractMinecartEntity : world
				.a(ScheduleBuilder.class, new Timer(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				connectedCart = abstractMinecartEntity;
				connectedCart.d(pos.getX() + .5, pos.getY(), pos.getZ() + .5f);
			}
		}

		return anchorSwap;
	}

	@Override
	protected boolean movementAllowed(GameMode world, BlockPos pos) {
		PistonHandler blockState = world.d_(pos);
		if (!pos.equals(anchor) && AllBlocks.CART_ASSEMBLER.has(blockState))
			return testSecondaryCartAssembler(world, blockState, pos);
		return super.movementAllowed(world, pos);
	}

	protected boolean testSecondaryCartAssembler(GameMode world, PistonHandler state, BlockPos pos) {
		for (Axis axis : Iterate.axes) {
			if (axis.isVertical() || !VecHelper.onSameAxis(anchor, pos, axis))
				continue;
			for (ScheduleBuilder abstractMinecartEntity : world
				.a(ScheduleBuilder.class, new Timer(pos))) {
				if (!CartAssemblerBlock.canAssembleTo(abstractMinecartEntity))
					break;
				return true;
			}
		}
		return false;
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		NBTHelper.writeEnum(tag, "RotationMode", rotationMode);
		return tag;
	}

	@Override
	public void readNBT(GameMode world, CompoundTag nbt, boolean spawnData) {
		rotationMode = NBTHelper.readEnum(nbt, "RotationMode", CartMovementMode.class);
		super.readNBT(world, nbt, spawnData);
	}

	@Override
	protected boolean customBlockPlacement(GrassColors world, BlockPos pos, PistonHandler state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}

	@Override
	protected boolean customBlockRemoval(GrassColors world, BlockPos pos, PistonHandler state) {
		return AllBlocks.MINECART_ANCHOR.has(state);
	}
	
	@Override
	protected boolean canAxisBeStabilized(Axis axis) {
		return true;
	}
	
	@Override
	public void addExtraInventories(apx cart) {
		if (!(cart instanceof BossBar))
			return;
		IItemHandlerModifiable handlerFromInv = new InvWrapper((BossBar) cart);
		inventory = new CombinedInvWrapper(handlerFromInv, inventory);
	}
}
