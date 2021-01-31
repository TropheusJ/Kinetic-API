package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isExtensionPole;
import static com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.isPistonHead;

import apx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.AllMovementBehaviours;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SeatBlock;
import com.simibubi.kinetic_api.content.contraptions.components.actors.SeatEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.bearing.StabilizedContraption;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.AbstractChassisBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.chassis.ChassisTileEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue.SuperGlueEntity;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.glue.SuperGlueHandler;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley.PulleyBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley.PulleyBlock.MagnetBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley.PulleyBlock.RopeBlock;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.kinetic_api.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.kinetic_api.content.logistics.block.inventories.AdjustableCrateBlock;
import com.simibubi.kinetic_api.content.logistics.block.redstone.RedstoneContactBlock;
import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.fluid.CombinedTankWrapper;
import com.simibubi.kinetic_api.foundation.utility.BlockFace;
import com.simibubi.kinetic_api.foundation.utility.BlockHelper;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import com.simibubi.kinetic_api.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.PotatoesBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.structure.processor.StructureProcessor.c;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.world.GameMode;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public abstract class Contraption {

	public AbstractContraptionEntity entity;
	public CombinedInvWrapper inventory;
	public CombinedTankWrapper fluidInventory;
	public Timer bounds;
	public BlockPos anchor;
	public boolean stalled;

	protected Map<BlockPos, c> blocks;
	protected Map<BlockPos, MountedStorage> storage;
	protected Map<BlockPos, MountedFluidStorage> fluidStorage;
	protected List<MutablePair<c, MovementContext>> actors;
	protected Set<Pair<BlockPos, Direction>> superglue;
	protected List<BlockPos> seats;
	protected Map<UUID, Integer> seatMapping;
	protected Map<UUID, BlockFace> stabilizedSubContraptions;

	private List<SuperGlueEntity> glueToRemove;
	private Map<BlockPos, apx> initialPassengers;
	private List<BlockFace> pendingSubContraptions;

	// Client
	public Map<BlockPos, BeehiveBlockEntity> presentTileEntities;
	public List<BeehiveBlockEntity> renderedTileEntities;

	public Contraption() {
		blocks = new HashMap<>();
		storage = new HashMap<>();
		seats = new ArrayList<>();
		actors = new ArrayList<>();
		superglue = new HashSet<>();
		seatMapping = new HashMap<>();
		fluidStorage = new HashMap<>();
		glueToRemove = new ArrayList<>();
		initialPassengers = new HashMap<>();
		presentTileEntities = new HashMap<>();
		renderedTileEntities = new ArrayList<>();
		pendingSubContraptions = new ArrayList<>();
		stabilizedSubContraptions = new HashMap<>();
	}

	public abstract boolean assemble(GameMode world, BlockPos pos);

	protected abstract boolean canAxisBeStabilized(Axis axis);

	protected abstract AllContraptionTypes getType();

	protected boolean customBlockPlacement(GrassColors world, BlockPos pos, PistonHandler state) {
		return false;
	}

	protected boolean customBlockRemoval(GrassColors world, BlockPos pos, PistonHandler state) {
		return false;
	}

	protected boolean addToInitialFrontier(GameMode world, BlockPos pos, Direction forcedDirection,
		List<BlockPos> frontier) {
		return true;
	}

	public static Contraption fromNBT(GameMode world, CompoundTag nbt, boolean spawnData) {
		String type = nbt.getString("Type");
		Contraption contraption = AllContraptionTypes.fromType(type);
		contraption.readNBT(world, nbt, spawnData);
		return contraption;
	}

	public boolean searchMovedStructure(GameMode world, BlockPos pos, @Nullable Direction forcedDirection) {
		initialPassengers.clear();
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		anchor = pos;

		if (bounds == null)
			bounds = new Timer(BlockPos.ORIGIN);

		if (!BlockMovementTraits.isBrittle(world.d_(pos)))
			frontier.add(pos);
		if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
			return false;
		for (int limit = 100000; limit > 0; limit--) {
			if (frontier.isEmpty())
				return true;
			if (!moveBlock(world, frontier.remove(0), forcedDirection, frontier, visited))
				return false;
		}
		return false;
	}

	public void onEntityCreated(AbstractContraptionEntity entity) {
		this.entity = entity;

		// KineticAPI subcontraptions
		for (BlockFace blockFace : pendingSubContraptions) {
			Direction face = blockFace.getFace();
			StabilizedContraption subContraption = new StabilizedContraption(face);
			GameMode world = entity.l;
			BlockPos pos = blockFace.getPos();
			if (!subContraption.assemble(world, pos))
				continue;
			subContraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
			OrientedContraptionEntity movedContraption =
				OrientedContraptionEntity.create(world, subContraption, Optional.of(face));
			BlockPos anchor = blockFace.getConnectedPos();
			movedContraption.d(anchor.getX() + .5f, anchor.getY(), anchor.getZ() + .5f);
			world.c(movedContraption);
			stabilizedSubContraptions.put(movedContraption.bR(), new BlockFace(toLocalPos(pos), face));
		}

		// Gather itemhandlers of mounted storage
		List<IItemHandlerModifiable> list = storage.values()
			.stream()
			.map(MountedStorage::getItemHandler)
			.collect(Collectors.toList());
		inventory = new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));

		List<IFluidHandler> fluidHandlers = fluidStorage.values()
			.stream()
			.map(MountedFluidStorage::getFluidHandler)
			.collect(Collectors.toList());
		fluidInventory = new CombinedTankWrapper(
			Arrays.copyOf(fluidHandlers.toArray(), fluidHandlers.size(), IFluidHandler[].class));
	}

	public void onEntityInitialize(GameMode world, AbstractContraptionEntity contraptionEntity) {
		if (world.v)
			return;

		for (OrientedContraptionEntity orientedCE : world.a(OrientedContraptionEntity.class,
			contraptionEntity.cb()
				.g(1)))
			if (stabilizedSubContraptions.containsKey(orientedCE.bR()))
				orientedCE.m(contraptionEntity);

		for (BlockPos seatPos : getSeats()) {
			apx passenger = initialPassengers.get(seatPos);
			if (passenger == null)
				continue;
			int seatIndex = getSeats().indexOf(seatPos);
			if (seatIndex == -1)
				continue;
			contraptionEntity.addSittingPassenger(passenger, seatIndex);
		}
	}

	public void onEntityTick(GameMode world) {
		fluidStorage.forEach((pos, mfs) -> mfs.tick(entity, pos, world.v));
	}

	protected boolean moveBlock(GameMode world, BlockPos pos, Direction forcedDirection, List<BlockPos> frontier,
		Set<BlockPos> visited) {
		visited.add(pos);
		frontier.remove(pos);

		if (!world.p(pos))
			return false;
		if (isAnchoringBlockAt(pos))
			return true;
		if (!BlockMovementTraits.movementNecessary(world, pos))
			return true;
		if (!movementAllowed(world, pos))
			return false;
		PistonHandler state = world.d_(pos);
		if (state.b() instanceof AbstractChassisBlock
			&& !moveChassis(world, pos, forcedDirection, frontier, visited))
			return false;

		if (AllBlocks.ADJUSTABLE_CRATE.has(state))
			AdjustableCrateBlock.splitCrate(world, pos);

		if (AllBlocks.BELT.has(state))
			moveBelt(pos, frontier, visited, state);

		// Bearings potentially kinetic_api stabilized sub-contraptions
		if (AllBlocks.MECHANICAL_BEARING.has(state))
			moveBearing(pos, frontier, visited, state);

		// Seats transfer their passenger to the contraption
		if (state.b() instanceof SeatBlock)
			moveSeat(world, pos);

		// Pulleys drag their rope and their attached structure
		if (state.b() instanceof PulleyBlock)
			movePulley(world, pos, frontier, visited);

		// Pistons drag their attaches poles and extension
		if (state.b() instanceof MechanicalPistonBlock)
			if (!moveMechanicalPiston(world, pos, frontier, visited, state))
				return false;

		// Doors try to stay whole
		if (state.b() instanceof AbstractRedstoneGateBlock) {
			BlockPos otherPartPos = pos.up(state.c(AbstractRedstoneGateBlock.e) == ComparatorMode.LOWER ? 1 : -1);
			if (!visited.contains(otherPartPos))
				frontier.add(otherPartPos);
		}

		// Cart assemblers attach themselves
		PistonHandler stateBelow = world.d_(pos.down());
		if (!visited.contains(pos.down()) && AllBlocks.CART_ASSEMBLER.has(stateBelow))
			frontier.add(pos.down());

		Map<Direction, SuperGlueEntity> superglue = SuperGlueHandler.gatherGlue(world, pos);

		// Slime blocks and super glue drag adjacent blocks if possible
		boolean isStickyBlock = state.isStickyBlock();
		for (Direction offset : Iterate.directions) {
			BlockPos offsetPos = pos.offset(offset);
			PistonHandler blockState = world.d_(offsetPos);
			if (isAnchoringBlockAt(offsetPos))
				continue;
			if (!movementAllowed(world, offsetPos)) {
				if (offset == forcedDirection && isStickyBlock)
					return false;
				continue;
			}

			boolean wasVisited = visited.contains(offsetPos);
			boolean faceHasGlue = superglue.containsKey(offset);
			boolean blockAttachedTowardsFace =
				BlockMovementTraits.isBlockAttachedTowards(world, offsetPos, blockState, offset.getOpposite());
			boolean brittle = BlockMovementTraits.isBrittle(blockState);

			if (!wasVisited && ((isStickyBlock && !brittle) || blockAttachedTowardsFace || faceHasGlue))
				frontier.add(offsetPos);
			if (faceHasGlue)
				addGlue(superglue.get(offset));
		}

		addBlock(pos, capture(world, pos));
		return blocks.size() <= AllConfigs.SERVER.kinetics.maxBlocksMoved.get();
	}

	private void moveBearing(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited, PistonHandler state) {
		Direction facing = state.c(MechanicalBearingBlock.FACING);
		if (!canAxisBeStabilized(facing.getAxis())) {
			BlockPos offset = pos.offset(facing);
			if (!visited.contains(offset))
				frontier.add(offset);
			return;
		}
		pendingSubContraptions.add(new BlockFace(pos, facing));
	}

	private void moveBelt(BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited, PistonHandler state) {
		BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
		BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
		if (nextPos != null && !visited.contains(nextPos))
			frontier.add(nextPos);
		if (prevPos != null && !visited.contains(prevPos))
			frontier.add(prevPos);
	}

	private void moveSeat(GameMode world, BlockPos pos) {
		BlockPos local = toLocalPos(pos);
		getSeats().add(local);
		List<SeatEntity> seatsEntities = world.a(SeatEntity.class, new Timer(pos));
		if (!seatsEntities.isEmpty()) {
			SeatEntity seat = seatsEntities.get(0);
			List<apx> passengers = seat.cm();
			if (!passengers.isEmpty())
				initialPassengers.put(local, passengers.get(0));
		}
	}

	private void movePulley(GameMode world, BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited) {
		int limit = AllConfigs.SERVER.kinetics.maxRopeLength.get();
		BlockPos ropePos = pos;
		while (limit-- >= 0) {
			ropePos = ropePos.down();
			if (!world.p(ropePos))
				break;
			PistonHandler ropeState = world.d_(ropePos);
			BeetrootsBlock block = ropeState.b();
			if (!(block instanceof RopeBlock) && !(block instanceof MagnetBlock)) {
				if (!visited.contains(ropePos))
					frontier.add(ropePos);
				break;
			}
			addBlock(ropePos, capture(world, ropePos));
		}
	}

	private boolean moveMechanicalPiston(GameMode world, BlockPos pos, List<BlockPos> frontier, Set<BlockPos> visited,
		PistonHandler state) {
		int limit = AllConfigs.SERVER.kinetics.maxPistonPoles.get();
		Direction direction = state.c(MechanicalPistonBlock.FACING);
		if (state.c(MechanicalPistonBlock.STATE) == PistonState.EXTENDED) {
			BlockPos searchPos = pos;
			while (limit-- >= 0) {
				searchPos = searchPos.offset(direction);
				PistonHandler blockState = world.d_(searchPos);
				if (isExtensionPole(blockState)) {
					if (blockState.c(PistonExtensionPoleBlock.SHAPE)
						.getAxis() != direction.getAxis())
						break;
					if (!visited.contains(searchPos))
						frontier.add(searchPos);
					continue;
				}
				if (isPistonHead(blockState))
					if (!visited.contains(searchPos))
						frontier.add(searchPos);
				break;
			}
			if (limit <= -1)
				return false;
		}

		BlockPos searchPos = pos;
		while (limit-- >= 0) {
			searchPos = searchPos.offset(direction.getOpposite());
			PistonHandler blockState = world.d_(searchPos);
			if (isExtensionPole(blockState)) {
				if (blockState.c(PistonExtensionPoleBlock.SHAPE)
					.getAxis() != direction.getAxis())
					break;
				if (!visited.contains(searchPos))
					frontier.add(searchPos);
				continue;
			}
			break;
		}

		if (limit <= -1)
			return false;
		return true;
	}

	private boolean moveChassis(GameMode world, BlockPos pos, Direction movementDirection, List<BlockPos> frontier,
		Set<BlockPos> visited) {
		BeehiveBlockEntity te = world.c(pos);
		if (!(te instanceof ChassisTileEntity))
			return false;
		ChassisTileEntity chassis = (ChassisTileEntity) te;
		chassis.addAttachedChasses(frontier, visited);
		List<BlockPos> includedBlockPositions = chassis.getIncludedBlockPositions(movementDirection, false);
		if (includedBlockPositions == null)
			return false;
		for (BlockPos blockPos : includedBlockPositions)
			if (!visited.contains(blockPos))
				frontier.add(blockPos);
		return true;
	}

	protected Pair<c, BeehiveBlockEntity> capture(GameMode world, BlockPos pos) {
		PistonHandler blockstate = world.d_(pos);
		if (blockstate.b() instanceof CarvedPumpkinBlock)
			blockstate = blockstate.a(CarvedPumpkinBlock.snowGolemPattern, Attachment.SINGLE);
		if (AllBlocks.ADJUSTABLE_CRATE.has(blockstate))
			blockstate = blockstate.a(AdjustableCrateBlock.DOUBLE, false);
		if (AllBlocks.REDSTONE_CONTACT.has(blockstate))
			blockstate = blockstate.a(RedstoneContactBlock.POWERED, true);
		if (blockstate.b() instanceof BubbleColumnBlock) {
			blockstate = blockstate.a(BubbleColumnBlock.DRAG, false);
			world.I()
				.a(pos, blockstate.b(), -1);
		}
		if (blockstate.b() instanceof PotatoesBlock) {
			blockstate = blockstate.a(PotatoesBlock.d, false);
			world.I()
				.a(pos, blockstate.b(), -1);
		}
		CompoundTag compoundnbt = getTileEntityNBT(world, pos);
		BeehiveBlockEntity tileentity = world.c(pos);
		return Pair.of(new c(pos, blockstate, compoundnbt), tileentity);
	}

	protected void addBlock(BlockPos pos, Pair<c, BeehiveBlockEntity> pair) {
		c captured = pair.getKey();
		BlockPos localPos = pos.subtract(anchor);
		c blockInfo = new c(localPos, captured.b, captured.c);

		if (blocks.put(localPos, blockInfo) != null)
			return;
		bounds = bounds.b(new Timer(localPos));

		BeehiveBlockEntity te = pair.getValue();
		if (te != null && MountedStorage.canUseAsStorage(te))
			storage.put(localPos, new MountedStorage(te));
		if (te != null && MountedFluidStorage.canUseAsStorage(te))
			fluidStorage.put(localPos, new MountedFluidStorage(te));
		if (AllMovementBehaviours.contains(captured.b.b()))
			actors.add(MutablePair.of(blockInfo, null));
	}

	@Nullable
	protected CompoundTag getTileEntityNBT(GameMode world, BlockPos pos) {
		BeehiveBlockEntity tileentity = world.c(pos);
		if (tileentity == null)
			return null;
		CompoundTag nbt = tileentity.a(new CompoundTag());
		nbt.remove("x");
		nbt.remove("y");
		nbt.remove("z");

		if (tileentity instanceof FluidTankTileEntity && nbt.contains("Controller"))
			nbt.put("Controller",
				NbtHelper.fromBlockPos(toLocalPos(NbtHelper.toBlockPos(nbt.getCompound("Controller")))));

		return nbt;
	}

	protected void addGlue(SuperGlueEntity entity) {
		BlockPos pos = entity.getHangingPosition();
		Direction direction = entity.getFacingDirection();
		this.superglue.add(Pair.of(toLocalPos(pos), direction));
		glueToRemove.add(entity);
	}

	protected BlockPos toLocalPos(BlockPos globalPos) {
		return globalPos.subtract(anchor);
	}

	protected boolean movementAllowed(GameMode world, BlockPos pos) {
		return BlockMovementTraits.movementAllowed(world, pos);
	}

	protected boolean isAnchoringBlockAt(BlockPos pos) {
		return pos.equals(anchor);
	}

	public void readNBT(GameMode world, CompoundTag nbt, boolean spawnData) {
		blocks.clear();
		presentTileEntities.clear();
		renderedTileEntities.clear();

		nbt.getList("Blocks", 10)
			.forEach(c -> {
				CompoundTag comp = (CompoundTag) c;
				c info = new c(NbtHelper.toBlockPos(comp.getCompound("Pos")),
					NbtHelper.c(comp.getCompound("Block")),
					comp.contains("Data") ? comp.getCompound("Data") : null);
				blocks.put(info.a, info);

				if (world.v) {
					BeetrootsBlock block = info.b.b();
					CompoundTag tag = info.c;
					MovementBehaviour movementBehaviour = AllMovementBehaviours.of(block);
					if (tag == null || (movementBehaviour != null && movementBehaviour.hasSpecialMovementRenderer()))
						return;

					tag.putInt("x", info.a.getX());
					tag.putInt("y", info.a.getY());
					tag.putInt("z", info.a.getZ());

					BeehiveBlockEntity te = BeehiveBlockEntity.b(info.b, tag);
					if (te == null)
						return;
					te.a(new WrappedWorld(world) {

						@Override
						public PistonHandler d_(BlockPos pos) {
							if (!pos.equals(te.o()))
								return BellBlock.FACING.n();
							return info.b;
						}

					}, te.o());
					if (te instanceof KineticTileEntity)
						((KineticTileEntity) te).setSpeed(0);
					te.p();
					presentTileEntities.put(info.a, te);
					renderedTileEntities.add(te);
				}
			});

		actors.clear();
		nbt.getList("Actors", 10)
			.forEach(c -> {
				CompoundTag comp = (CompoundTag) c;
				c info = blocks.get(NbtHelper.toBlockPos(comp.getCompound("Pos")));
				MovementContext context = MovementContext.readNBT(world, info, comp, this);
				getActors().add(MutablePair.of(info, context));
			});

		superglue.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Superglue", NBT.TAG_COMPOUND), c -> superglue
			.add(Pair.of(NbtHelper.toBlockPos(c.getCompound("Pos")), Direction.byId(c.getByte("Direction")))));

		seats.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Seats", NBT.TAG_COMPOUND), c -> seats.add(NbtHelper.toBlockPos(c)));

		seatMapping.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Passengers", NBT.TAG_COMPOUND),
			c -> seatMapping.put(NbtHelper.toUuid(NBTHelper.getINBT(c, "Id")), c.getInt("Seat")));

		stabilizedSubContraptions.clear();
		NBTHelper.iterateCompoundList(nbt.getList("SubContraptions", NBT.TAG_COMPOUND), c -> stabilizedSubContraptions
			.put(c.getUuid("Id"), BlockFace.fromNBT(c.getCompound("Location"))));

		storage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("Storage", NBT.TAG_COMPOUND), c -> storage
			.put(NbtHelper.toBlockPos(c.getCompound("Pos")), MountedStorage.deserialize(c.getCompound("Data"))));

		fluidStorage.clear();
		NBTHelper.iterateCompoundList(nbt.getList("FluidStorage", NBT.TAG_COMPOUND), c -> fluidStorage
			.put(NbtHelper.toBlockPos(c.getCompound("Pos")), MountedFluidStorage.deserialize(c.getCompound("Data"))));

		if (spawnData)
			fluidStorage.forEach((pos, mfs) -> {
				BeehiveBlockEntity tileEntity = presentTileEntities.get(pos);
				if (!(tileEntity instanceof FluidTankTileEntity))
					return;
				FluidTankTileEntity tank = (FluidTankTileEntity) tileEntity;
				IFluidTank tankInventory = tank.getTankInventory();
				if (tankInventory instanceof FluidTank)
					((FluidTank) tankInventory).setFluid(mfs.tank.getFluid());
				tank.getFluidLevel()
					.start(tank.getFillState());
				mfs.assignTileEntity(tank);
			});

		IItemHandlerModifiable[] handlers = new IItemHandlerModifiable[storage.size()];
		int index = 0;
		for (MountedStorage mountedStorage : storage.values())
			handlers[index++] = mountedStorage.getItemHandler();

		IFluidHandler[] fluidHandlers = new IFluidHandler[fluidStorage.size()];
		index = 0;
		for (MountedFluidStorage mountedStorage : fluidStorage.values())
			fluidHandlers[index++] = mountedStorage.getFluidHandler();

		inventory = new CombinedInvWrapper(handlers);
		fluidInventory = new CombinedTankWrapper(fluidHandlers);

		if (nbt.contains("BoundsFront"))
			bounds = NBTHelper.readAABB(nbt.getList("BoundsFront", 5));

		stalled = nbt.getBoolean("Stalled");
		anchor = NbtHelper.toBlockPos(nbt.getCompound("Anchor"));
	}

	public CompoundTag writeNBT(boolean spawnPacket) {
		CompoundTag nbt = new CompoundTag();
		nbt.putString("Type", getType().id);
		ListTag blocksNBT = new ListTag();
		for (c block : this.blocks.values()) {
			CompoundTag c = new CompoundTag();
			c.put("Block", NbtHelper.a(block.b));
			c.put("Pos", NbtHelper.fromBlockPos(block.a));
			if (block.c != null)
				c.put("Data", block.c);
			blocksNBT.add(c);
		}

		ListTag actorsNBT = new ListTag();
		for (MutablePair<c, MovementContext> actor : getActors()) {
			CompoundTag compound = new CompoundTag();
			compound.put("Pos", NbtHelper.fromBlockPos(actor.left.a));
			AllMovementBehaviours.of(actor.left.b)
				.writeExtraData(actor.right);
			actor.right.writeToNBT(compound);
			actorsNBT.add(compound);
		}

		ListTag superglueNBT = new ListTag();
		for (Pair<BlockPos, Direction> glueEntry : superglue) {
			CompoundTag c = new CompoundTag();
			c.put("Pos", NbtHelper.fromBlockPos(glueEntry.getKey()));
			c.putByte("Direction", (byte) glueEntry.getValue()
				.getId());
			superglueNBT.add(c);
		}

		ListTag storageNBT = new ListTag();
		if (!spawnPacket) {
			for (BlockPos pos : storage.keySet()) {
				CompoundTag c = new CompoundTag();
				MountedStorage mountedStorage = storage.get(pos);
				if (!mountedStorage.isValid())
					continue;
				c.put("Pos", NbtHelper.fromBlockPos(pos));
				c.put("Data", mountedStorage.serialize());
				storageNBT.add(c);
			}
		}

		ListTag fluidStorageNBT = new ListTag();
		for (BlockPos pos : fluidStorage.keySet()) {
			CompoundTag c = new CompoundTag();
			MountedFluidStorage mountedStorage = fluidStorage.get(pos);
			if (!mountedStorage.isValid())
				continue;
			c.put("Pos", NbtHelper.fromBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			fluidStorageNBT.add(c);
		}

		nbt.put("Seats", NBTHelper.writeCompoundList(getSeats(), NbtHelper::fromBlockPos));
		nbt.put("Passengers", NBTHelper.writeCompoundList(getSeatMapping().entrySet(), e -> {
			CompoundTag tag = new CompoundTag();
			tag.put("Id", NbtHelper.fromUuid(e.getKey()));
			tag.putInt("Seat", e.getValue());
			return tag;
		}));

		nbt.put("SubContraptions", NBTHelper.writeCompoundList(stabilizedSubContraptions.entrySet(), e -> {
			CompoundTag tag = new CompoundTag();
			tag.putUuid("Id", e.getKey());
			tag.put("Location", e.getValue()
				.serializeNBT());
			return tag;
		}));

		nbt.put("Blocks", blocksNBT);
		nbt.put("Actors", actorsNBT);
		nbt.put("Superglue", superglueNBT);
		nbt.put("Storage", storageNBT);
		nbt.put("FluidStorage", fluidStorageNBT);
		nbt.put("Anchor", NbtHelper.fromBlockPos(anchor));
		nbt.putBoolean("Stalled", stalled);

		if (bounds != null) {
			ListTag bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
	}

	public void removeBlocksFromWorld(GameMode world, BlockPos offset) {
		storage.values()
			.forEach(MountedStorage::removeStorageFromWorld);
		fluidStorage.values()
			.forEach(MountedFluidStorage::removeStorageFromWorld);
		glueToRemove.forEach(SuperGlueEntity::ac);

		for (boolean brittles : Iterate.trueAndFalse) {
			for (Iterator<c> iterator = blocks.values()
				.iterator(); iterator.hasNext();) {
				c block = iterator.next();
				if (brittles != BlockMovementTraits.isBrittle(block.b))
					continue;

				BlockPos add = block.a.add(anchor).add(offset);
				if (customBlockRemoval(world, add, block.b))
					continue;
				PistonHandler oldState = world.d_(add);
				BeetrootsBlock blockIn = oldState.b();
				if (block.b.b() != blockIn)
					iterator.remove();
				world.o(add);
				int flags = BlockFlags.IS_MOVING | BlockFlags.NO_NEIGHBOR_DROPS | BlockFlags.UPDATE_NEIGHBORS;
				if (blockIn instanceof SeagrassBlock && oldState.b(BambooLeaves.C)
					&& oldState.c(BambooLeaves.C)
						.booleanValue()) {
					world.a(add, BellBlock.A.n(), flags);
					continue;
				}
				world.a(add, BellBlock.FACING.n(), flags);
			}
		}
		for (c block : blocks.values()) {
			BlockPos add = block.a.add(anchor).add(offset);
			world.markAndNotifyBlock(add, world.n(add), block.b, BellBlock.FACING.n(), BlockFlags.IS_MOVING | BlockFlags.DEFAULT, 512);
		}
	}

	public void addBlocksToWorld(GameMode world, StructureTransform transform) {
		for (boolean nonBrittles : Iterate.trueAndFalse) {
			for (c block : blocks.values()) {
				if (nonBrittles == BlockMovementTraits.isBrittle(block.b))
					continue;

				BlockPos targetPos = transform.apply(block.a);
				PistonHandler state = transform.apply(block.b);

				if (customBlockPlacement(world, targetPos, state))
					continue;

				if (nonBrittles)
					for (Direction face : Iterate.directions)
						state = state.a(face, world.d_(targetPos.offset(face)), world,
							targetPos, targetPos.offset(face));

				PistonHandler blockState = world.d_(targetPos);
				if (blockState.h(world, targetPos) == -1 || (state.k(world, targetPos)
					.b()
					&& !blockState.k(world, targetPos)
						.b())) {
					if (targetPos.getY() == 0)
						targetPos = targetPos.up();
					world.syncWorldEvent(2001, targetPos, BeetrootsBlock.i(state));
					BeetrootsBlock.a(state, world, targetPos, null);
					continue;
				}
				if (state.b() instanceof SeagrassBlock && BlockHelper.hasBlockStateProperty(state, BambooLeaves.C)) {
					EmptyFluid FluidState = world.b(targetPos);
					state = state.a(BambooLeaves.C,
						FluidState.a() == FlowableFluid.c);
				}

				world.b(targetPos, true);
				world.a(targetPos, state, 3 | BlockFlags.IS_MOVING);

				boolean verticalRotation = transform.rotationAxis == null || transform.rotationAxis.isHorizontal();
				verticalRotation = verticalRotation && transform.rotation != RespawnAnchorBlock.CHARGES;
				if (verticalRotation) {
					if (state.b() instanceof RopeBlock || state.b() instanceof MagnetBlock)
						world.b(targetPos, true);
				}

				BeehiveBlockEntity tileEntity = world.c(targetPos);
				CompoundTag tag = block.c;
				if (tileEntity != null && tag != null) {
					tag.putInt("x", targetPos.getX());
					tag.putInt("y", targetPos.getY());
					tag.putInt("z", targetPos.getZ());

					if (verticalRotation && tileEntity instanceof PulleyTileEntity) {
						tag.remove("Offset");
						tag.remove("InitialOffset");
					}

					if (tileEntity instanceof FluidTankTileEntity && tag.contains("LastKnownPos"))
						tag.put("LastKnownPos", NbtHelper.fromBlockPos(BlockPos.ORIGIN.down()));

					tileEntity.a(block.b, tag);

					if (storage.containsKey(block.a)) {
						MountedStorage mountedStorage = storage.get(block.a);
						if (mountedStorage.isValid())
							mountedStorage.addStorageToWorld(tileEntity);
					}

					if (fluidStorage.containsKey(block.a)) {
						MountedFluidStorage mountedStorage = fluidStorage.get(block.a);
						if (mountedStorage.isValid())
							mountedStorage.addStorageToWorld(tileEntity);
					}
				}
			}
		}
		for (c block : blocks.values()) {
			BlockPos targetPos = transform.apply(block.a);
			world.markAndNotifyBlock(targetPos, world.n(targetPos), block.b, block.b, BlockFlags.IS_MOVING | BlockFlags.DEFAULT, 512);
		}

		for (int i = 0; i < inventory.getSlots(); i++)
			inventory.setStackInSlot(i, ItemCooldownManager.tick);
		for (int i = 0; i < fluidInventory.getTanks(); i++)
			fluidInventory.drain(fluidInventory.getFluidInTank(i), FluidAction.EXECUTE);

		for (Pair<BlockPos, Direction> pair : superglue) {
			BlockPos targetPos = transform.apply(pair.getKey());
			Direction targetFacing = transform.transformFacing(pair.getValue());

			SuperGlueEntity entity = new SuperGlueEntity(world, targetPos, targetFacing);
			if (entity.onValidSurface()) {
				if (!world.v)
					world.c(entity);
			}
		}
	}

	public void addPassengersToWorld(GameMode world, StructureTransform transform, List<apx> seatedEntities) {
		for (apx seatedEntity : seatedEntities) {
			if (getSeatMapping().isEmpty())
				continue;
			Integer seatIndex = getSeatMapping().get(seatedEntity.bR());
			BlockPos seatPos = getSeats().get(seatIndex);
			seatPos = transform.apply(seatPos);
			if (!(world.d_(seatPos)
				.b() instanceof SeatBlock))
				continue;
			if (SeatBlock.isSeatOccupied(world, seatPos))
				continue;
			SeatBlock.sitDown(world, seatPos, seatedEntity);
		}
	}

	public void startMoving(GameMode world) {
		for (MutablePair<c, MovementContext> pair : actors) {
			MovementContext context = new MovementContext(world, pair.left, this);
			AllMovementBehaviours.of(pair.left.b)
				.startMoving(context);
			pair.setRight(context);
		}
	}

	public void stop(GameMode world) {
		foreachActor(world, (behaviour, ctx) -> {
			behaviour.stopMoving(ctx);
			ctx.position = null;
			ctx.motion = EntityHitResult.a;
			ctx.relativeMotion = EntityHitResult.a;
			ctx.rotation = v -> v;
		});
	}

	public void foreachActor(GameMode world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<c, MovementContext> pair : actors)
			callBack.accept(AllMovementBehaviours.of(pair.getLeft().b), pair.getRight());
	}

	public void expandBoundsAroundAxis(Axis axis) {
		Timer bb = bounds;
		double maxXDiff = Math.max(bb.eventCounter - 1, -bb.LOGGER);
		double maxYDiff = Math.max(bb.eventsByName - 1, -bb.callback);
		double maxZDiff = Math.max(bb.f - 1, -bb.events);
		double maxDiff = 0;

		if (axis == Axis.X)
			maxDiff = Math.max(maxZDiff, maxYDiff);
		if (axis == Axis.Y)
			maxDiff = Math.max(maxZDiff, maxXDiff);
		if (axis == Axis.Z)
			maxDiff = Math.max(maxXDiff, maxYDiff);

		EntityHitResult vec = EntityHitResult.b(Direction.get(AxisDirection.POSITIVE, axis)
			.getVector());
		EntityHitResult planeByNormal = VecHelper.axisAlingedPlaneOf(vec);
		EntityHitResult min = vec.d(bb.LOGGER, bb.callback, bb.events)
			.e(planeByNormal.a(-maxDiff));
		EntityHitResult max = vec.d(bb.eventCounter, bb.eventsByName, bb.f)
			.e(planeByNormal.a(maxDiff + 1));
		bounds = new Timer(min, max);
	}

	public void addExtraInventories(apx entity) {}

	public Map<UUID, Integer> getSeatMapping() {
		return seatMapping;
	}

	public BlockPos getSeatOf(UUID entityId) {
		if (!getSeatMapping().containsKey(entityId))
			return null;
		int seatIndex = getSeatMapping().get(entityId);
		if (seatIndex >= getSeats().size())
			return null;
		return getSeats().get(seatIndex);
	}

	public BlockPos getBearingPosOf(UUID subContraptionEntityId) {
		if (stabilizedSubContraptions.containsKey(subContraptionEntityId))
			return stabilizedSubContraptions.get(subContraptionEntityId)
				.getConnectedPos();
		return null;
	}

	public void setSeatMapping(Map<UUID, Integer> seatMapping) {
		this.seatMapping = seatMapping;
	}

	public List<BlockPos> getSeats() {
		return seats;
	}

	public Map<BlockPos, c> getBlocks() {
		return blocks;
	}

	public List<MutablePair<c, MovementContext>> getActors() {
		return actors;
	}

	public void updateContainedFluid(BlockPos localPos, FluidStack containedFluid) {
		MountedFluidStorage mountedFluidStorage = fluidStorage.get(localPos);
		if (mountedFluidStorage != null)
			mountedFluidStorage.updateFluid(containedFluid);
	}

}