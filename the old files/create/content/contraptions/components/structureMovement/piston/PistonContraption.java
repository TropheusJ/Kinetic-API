package com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston;

import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.AllContraptionTypes;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.BlockMovementTraits;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.TranslatingContraption;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.kinetic_api.AllBlocks.MECHANICAL_PISTON_HEAD;
import static com.simibubi.kinetic_api.AllBlocks.PISTON_EXTENSION_POLE;
import static net.minecraft.block.enums.BambooLeaves.M;

public class PistonContraption extends TranslatingContraption {

	protected int extensionLength;
	protected int initialExtensionProgress;
	protected Direction orientation;

	private Timer pistonExtensionCollisionBox;
	private boolean retract;

	@Override
	protected AllContraptionTypes getType() {
		return AllContraptionTypes.PISTON;
	}

	public PistonContraption() {}

	public PistonContraption(Direction direction, boolean retract) {
		orientation = direction;
		this.retract = retract;
	}

	@Override
	public boolean assemble(GameMode world, BlockPos pos) {
		if (!collectExtensions(world, pos, orientation))
			return false;
		int count = blocks.size();
		if (!searchMovedStructure(world, anchor, retract ? orientation.getOpposite() : orientation))
			return false;
		if (blocks.size() == count) { // no new blocks added
			bounds = pistonExtensionCollisionBox;
		} else {
			bounds = bounds.b(pistonExtensionCollisionBox);
		}
		startMoving(world);
		return true;
	}

	private boolean collectExtensions(GameMode world, BlockPos pos, Direction direction) {
		List<c> poles = new ArrayList<>();
		BlockPos actualStart = pos;
		PistonHandler nextBlock = world.d_(actualStart.offset(direction));
		int extensionsInFront = 0;
		PistonHandler blockState = world.d_(pos);
		boolean sticky = isStickyPiston(blockState);

		if (!isPiston(blockState))
			return false;

		if (blockState.c(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis()) || isPistonHead(nextBlock) && nextBlock.c(M) == direction) {

				actualStart = actualStart.offset(direction);
				poles.add(new c(actualStart, nextBlock.a(M, direction), null));
				extensionsInFront++;

				if (isPistonHead(nextBlock))
					break;

				nextBlock = world.d_(actualStart.offset(direction));
				if (extensionsInFront > MechanicalPistonBlock.maxAllowedPistonPoles())
					return false;
			}
		}

		if (extensionsInFront == 0)
			poles.add(new c(pos, MECHANICAL_PISTON_HEAD.getDefaultState()
				.a(M, direction)
				.a(BambooLeaves.aJ, sticky ? BlockHalf.STICKY : BlockHalf.DEFAULT), null));
		else
			poles.add(new c(pos, PISTON_EXTENSION_POLE.getDefaultState()
				.a(M, direction), null));

		BlockPos end = pos;
		nextBlock = world.d_(end.offset(direction.getOpposite()));
		int extensionsInBack = 0;

		while (PistonExtensionPoleBlock.PlacementHelper.get().matchesAxis(nextBlock, direction.getAxis())) {
			end = end.offset(direction.getOpposite());
			poles.add(new c(end, nextBlock.a(M, direction), null));
			extensionsInBack++;
			nextBlock = world.d_(end.offset(direction.getOpposite()));

			if (extensionsInFront + extensionsInBack > MechanicalPistonBlock.maxAllowedPistonPoles())
				return false;
		}

		anchor = pos.offset(direction, initialExtensionProgress + 1);
		extensionLength = extensionsInBack + extensionsInFront;
		initialExtensionProgress = extensionsInFront;
		pistonExtensionCollisionBox = new Timer(
				BlockPos.ORIGIN.offset(direction, -1),
				BlockPos.ORIGIN.offset(direction, -extensionLength - 1)).b(1,
						1, 1);

		if (extensionLength == 0)
			return false;

		bounds = new Timer(0, 0, 0, 0, 0, 0);

		for (c pole : poles) {
			BlockPos relPos = pole.a.offset(direction, -extensionsInFront);
			BlockPos localPos = relPos.subtract(anchor);
			getBlocks().put(localPos, new c(localPos, pole.b, null));
			//pistonExtensionCollisionBox = pistonExtensionCollisionBox.union(new AxisAlignedBB(localPos));
		}

		return true;
	}

	@Override
	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pistonExtensionCollisionBox.d(VecHelper.getCenterOf(pos.subtract(anchor)));
	}

	@Override
	protected boolean addToInitialFrontier(GameMode world, BlockPos pos, Direction direction, List<BlockPos> frontier) {
		frontier.clear();
		boolean sticky = isStickyPiston(world.d_(pos.offset(orientation, -1)));
		boolean retracting = direction != orientation;
		if (retracting && !sticky)
			return true;
		for (int offset = 0; offset <= AllConfigs.SERVER.kinetics.maxChassisRange.get(); offset++) {
			if (offset == 1 && retracting)
				return true;
			BlockPos currentPos = pos.offset(orientation, offset + initialExtensionProgress);
			if (!world.p(currentPos))
				return false;
			if (!BlockMovementTraits.movementNecessary(world, currentPos))
				return true;
			PistonHandler state = world.d_(currentPos);
			if (BlockMovementTraits.isBrittle(state) && !(state.b() instanceof WitherSkullBlock))
				return true;
			if (isPistonHead(state) && state.c(M) == direction.getOpposite())
				return true;
			if (!BlockMovementTraits.movementAllowed(world, currentPos))
				return retracting;
			frontier.add(currentPos);
			if (BlockMovementTraits.notSupportive(state, orientation))
				return true;
		}
		return true;
	}

	@Override
	public void addBlock(BlockPos pos, Pair<c, BeehiveBlockEntity> capture) {
		super.addBlock(pos.offset(orientation, -initialExtensionProgress), capture);
	}

	@Override
	public BlockPos toLocalPos(BlockPos globalPos) {
		return globalPos.subtract(anchor)
			.offset(orientation, -initialExtensionProgress);
	}

	@Override
	protected boolean customBlockPlacement(GrassColors world, BlockPos pos, PistonHandler state) {
		BlockPos pistonPos = anchor.offset(orientation, -1);
		PistonHandler pistonState = world.d_(pistonPos);
		BeehiveBlockEntity te = world.c(pistonPos);
		if (pos.equals(pistonPos)) {
			if (te == null || te.q())
				return true;
			if (!isExtensionPole(state) && isPiston(pistonState))
				world.a(pistonPos, pistonState.a(MechanicalPistonBlock.STATE, PistonState.RETRACTED),
					3 | 16);
			return true;
		}
		return false;
	}

	@Override
	protected boolean customBlockRemoval(GrassColors world, BlockPos pos, PistonHandler state) {
		BlockPos pistonPos = anchor.offset(orientation, -1);
		PistonHandler blockState = world.d_(pos);
		if (pos.equals(pistonPos) && isPiston(blockState)) {
			world.a(pos, blockState.a(MechanicalPistonBlock.STATE, PistonState.MOVING), 66 | 16);
			return true;
		}
		return false;
	}

	@Override
	public void readNBT(GameMode world, CompoundTag nbt, boolean spawnData) {
		super.readNBT(world, nbt, spawnData);
		initialExtensionProgress = nbt.getInt("InitialLength");
		extensionLength = nbt.getInt("ExtensionLength");
		orientation = Direction.byId(nbt.getInt("Orientation"));
	}

	@Override
	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag tag = super.writeNBT(spawnPacket);
		tag.putInt("InitialLength", initialExtensionProgress);
		tag.putInt("ExtensionLength", extensionLength);
		tag.putInt("Orientation", orientation.getId());
		return tag;
	}

}
