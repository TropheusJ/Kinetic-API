package com.simibubi.create.content.contraptions.components.crafter;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import afj;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity.Inventory;
import com.simibubi.create.content.contraptions.components.crafter.RecipeGridHandler.GroupedItems;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pointing;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MechanicalCrafterTileEntity extends KineticTileEntity {

	enum Phase {
		IDLE, ACCEPTING, ASSEMBLING, EXPORTING, WAITING, CRAFTING, INSERTING;
	}

	static class Inventory extends SmartInventory {

		private MechanicalCrafterTileEntity te;

		public Inventory(MechanicalCrafterTileEntity te) {
			super(1, te, 1, false);
			this.te = te;
			forbidExtraction();
			whenContentsChanged(slot -> {
				if (a(slot).a())
					return;
				if(te.phase == Phase.IDLE)
					te.checkCompletedRecipe(false);
			});
		}
		
		@Override
		public ItemCooldownManager insertItem(int slot, ItemCooldownManager stack, boolean simulate) {
			if (te.phase != Phase.IDLE)
				return stack;
			if (te.covered)
				return stack;
			return super.insertItem(slot, stack, simulate);
		}
		
	}
	
	protected Inventory inventory;
	protected GroupedItems groupedItems = new GroupedItems();
	protected ConnectedInput input = new ConnectedInput();
	protected LazyOptional<IItemHandler> invSupplier = LazyOptional.of(() -> input.getItemHandler(d, e));
	protected boolean reRender;
	protected Phase phase;
	protected int countDown;
	protected boolean covered;
	protected boolean wasPoweredBefore;

	protected GroupedItems groupedItemsBeforeCraft; // for rendering on client
	private InvManipulationBehaviour inserting;
	private EdgeInteractionBehaviour connectivity;

	public MechanicalCrafterTileEntity(BellBlockEntity<? extends MechanicalCrafterTileEntity> type) {
		super(type);
		setLazyTickRate(20);
		phase = Phase.IDLE;
		groupedItemsBeforeCraft = new GroupedItems();
		inventory = new Inventory(this);
		
		// Does not get serialized due to active checking in tick
		wasPoweredBefore = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		inserting = new InvManipulationBehaviour(this, this::getTargetFace);
		connectivity = new EdgeInteractionBehaviour(this, ConnectedInputHandler::toggleConnection)
			.connectivity(ConnectedInputHandler::shouldConnect)
			.require(AllItems.WRENCH.get());
		behaviours.add(inserting);
		behaviours.add(connectivity);
	}

	public void blockChanged() {
		removeBehaviour(InvManipulationBehaviour.TYPE);
		inserting = new InvManipulationBehaviour(this, this::getTargetFace);
		attachBehaviourLate(inserting);
	}

	public BlockFace getTargetFace(GameMode world, BlockPos pos, PistonHandler state) {
		return new BlockFace(pos, MechanicalCrafterBlock.getTargetDirection(state));
	}
	
	public Direction getTargetDirection() {
		return MechanicalCrafterBlock.getTargetDirection(p());
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.put("Inventory", inventory.serializeNBT());

		CompoundTag inputNBT = new CompoundTag();
		input.write(inputNBT);
		compound.put("ConnectedInput", inputNBT);

		CompoundTag groupedItemsNBT = new CompoundTag();
		groupedItems.write(groupedItemsNBT);
		compound.put("GroupedItems", groupedItemsNBT);

		compound.putString("Phase", phase.name());
		compound.putInt("CountDown", countDown);
		compound.putBoolean("Cover", covered);

		super.write(compound, clientPacket);
		
		if (clientPacket && reRender) {
			compound.putBoolean("Redraw", true);
			reRender = false;
		}
	}

	@Override
	protected void fromTag(PistonHandler state, CompoundTag compound, boolean clientPacket) {
		Phase phaseBefore = phase;
		GroupedItems before = this.groupedItems;
		
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		input.read(compound.getCompound("ConnectedInput"));
		groupedItems = GroupedItems.read(compound.getCompound("GroupedItems"));
		phase = Phase.IDLE;
		String name = compound.getString("Phase");
		for (Phase phase : Phase.values())
			if (phase.name()
				.equals(name))
				this.phase = phase;
		countDown = compound.getInt("CountDown");
		covered = compound.getBoolean("Cover");
		super.fromTag(state, compound, clientPacket);
		
		if (!clientPacket)
			return;
		if (compound.contains("Redraw"))
			d.a(o(), p(), p(), 16);
		if (phaseBefore != phase && phase == Phase.CRAFTING)
			groupedItemsBeforeCraft = before;
		if (phaseBefore == Phase.EXPORTING && phase == Phase.WAITING) {
			Direction facing = p().c(MechanicalCrafterBlock.HORIZONTAL_FACING);
			EntityHitResult vec = EntityHitResult.b(facing.getVector()).a(.75)
				.e(VecHelper.getCenterOf(e));
			Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(p());
			vec = vec.e(EntityHitResult.b(targetDirection.getVector()).a(1));
			d.addParticle(ParticleTypes.CRIT, vec.entity, vec.c, vec.d, 0, 0, 0);
		}
	}

	@Override
	public void al_() {
		invSupplier.invalidate();
		super.al_();
	}

	public int getCountDownSpeed() {
		if (getSpeed() == 0)
			return 0;
		return afj.a((int) Math.abs(getSpeed()), 4, 250);
	}

	@Override
	public void aj_() {
		super.aj_();

		if (phase == Phase.ACCEPTING)
			return;

		if (wasPoweredBefore != d.r(e)) {
			wasPoweredBefore = d.r(e);
			if (wasPoweredBefore) {
				if (d.v)
					return;
				checkCompletedRecipe(true);
			}
		}

		if (phase == Phase.ASSEMBLING) {
			countDown -= getCountDownSpeed();
			if (countDown < 0) {
				countDown = 0;
				if (d.v)
					return;
				if (RecipeGridHandler.getTargetingCrafter(this) != null) {
					phase = Phase.EXPORTING;
					countDown = 1000;
					sendData();
					return;
				}
				ItemCooldownManager result = RecipeGridHandler.tryToApplyRecipe(d, groupedItems);
				if (result != null) {

					List<ItemCooldownManager> containers = new ArrayList<>();
					groupedItems.grid.values()
						.forEach(stack -> {
							if (stack.hasContainerItem())
								containers.add(stack.getContainerItem()
									.i());
						});

					groupedItems = new GroupedItems(result);
					for (int i = 0; i < containers.size(); i++) {
						ItemCooldownManager stack = containers.get(i);
						GroupedItems container = new GroupedItems();
						container.grid.put(Pair.of(i, 0), stack);
						container.mergeOnto(groupedItems, Pointing.LEFT);
					}

					phase = Phase.CRAFTING;
					countDown = 2000;
					sendData();
					return;
				}
				ejectWholeGrid();
				return;
			}
		}

		if (phase == Phase.EXPORTING) {
			countDown -= getCountDownSpeed();

			if (countDown < 0) {
				countDown = 0;
				if (d.v)
					return;

				MechanicalCrafterTileEntity targetingCrafter = RecipeGridHandler.getTargetingCrafter(this);
				if (targetingCrafter == null) {
					ejectWholeGrid();
					return;
				}

				Pointing pointing = p().c(MechanicalCrafterBlock.POINTING);
				groupedItems.mergeOnto(targetingCrafter.groupedItems, pointing);
				groupedItems = new GroupedItems();
				phase = Phase.WAITING;
				countDown = 0;
				sendData();
				targetingCrafter.continueIfAllPrecedingFinished();
				targetingCrafter.sendData();
				return;
			}
		}

		if (phase == Phase.CRAFTING) {

			if (d.v) {
				Direction facing = p().c(MechanicalCrafterBlock.HORIZONTAL_FACING);
				float progress = countDown / 2000f;
				EntityHitResult facingVec = EntityHitResult.b(facing.getVector());
				EntityHitResult vec = facingVec.a(.65)
					.e(VecHelper.getCenterOf(e));
				EntityHitResult offset = VecHelper.offsetRandomly(EntityHitResult.a, d.t, .125f)
					.h(VecHelper.axisAlingedPlaneOf(facingVec))
					.d()
					.a(progress * .5f)
					.e(vec);
				if (progress > .5f)
					d.addParticle(ParticleTypes.CRIT, offset.entity, offset.c, offset.d, 0, 0, 0);

				if (!groupedItemsBeforeCraft.grid.isEmpty() && progress < .5f) {
					if (groupedItems.grid.containsKey(Pair.of(0, 0))) {
						ItemCooldownManager stack = groupedItems.grid.get(Pair.of(0, 0));
						groupedItemsBeforeCraft = new GroupedItems();

						for (int i = 0; i < 10; i++) {
							EntityHitResult randVec = VecHelper.offsetRandomly(EntityHitResult.a, d.t, .125f)
								.h(VecHelper.axisAlingedPlaneOf(facingVec))
								.d()
								.a(.25f);
							EntityHitResult offset2 = randVec.e(vec);
							randVec = randVec.a(.35f);
							d.addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), offset2.entity, offset2.c,
								offset2.d, randVec.entity, randVec.c, randVec.d);
						}
					}
				}
			}

			countDown -= getCountDownSpeed();
			if (countDown < 0) {
				countDown = 0;
				if (d.v)
					return;
				tryInsert();
				return;
			}
		}

		if (phase == Phase.INSERTING) {
			if (!d.v && isTargetingBelt())
				tryInsert();
			return;
		}
	}

	protected boolean isTargetingBelt() {
		DirectBeltInputBehaviour behaviour = getTargetingBelt();
		return behaviour != null && behaviour.canInsertFromSide(getTargetDirection());
	}

	protected DirectBeltInputBehaviour getTargetingBelt() {
		BlockPos targetPos = e.offset(getTargetDirection());
		return TileEntityBehaviour.get(d, targetPos, DirectBeltInputBehaviour.TYPE);
	}

	public void tryInsert() {
		if (!inserting.hasInventory() && !isTargetingBelt()) {
			ejectWholeGrid();
			return;
		}

		boolean chagedPhase = phase != Phase.INSERTING;
		final List<Pair<Integer, Integer>> inserted = new LinkedList<>();

		DirectBeltInputBehaviour behaviour = getTargetingBelt();
		for (Entry<Pair<Integer, Integer>, ItemCooldownManager> entry : groupedItems.grid.entrySet()) {
			Pair<Integer, Integer> pair = entry.getKey();
			ItemCooldownManager stack = entry.getValue();
			BlockFace face = getTargetFace(d, e, p());

			ItemCooldownManager remainder = behaviour == null ? inserting.insert(stack.i())
				: behaviour.handleInsertion(stack, face.getFace(), false);
			if (!remainder.a()) {
				stack.e(remainder.E());
				continue;
			}
			
			inserted.add(pair);
		}

		inserted.forEach(groupedItems.grid::remove);
		if (groupedItems.grid.isEmpty())
			ejectWholeGrid();
		else
			phase = Phase.INSERTING;
		if (!inserted.isEmpty() || chagedPhase)
			sendData();
	}

	public void ejectWholeGrid() {
		List<MechanicalCrafterTileEntity> chain = RecipeGridHandler.getAllCraftersOfChain(this);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterTileEntity::eject);
	}

	public void eject() {
		PistonHandler blockState = p();
		boolean present = AllBlocks.MECHANICAL_CRAFTER.has(blockState);
		EntityHitResult vec = present ? EntityHitResult.b(blockState.c(HORIZONTAL_FACING)
			.getVector()).a(.75f) : EntityHitResult.a;
		EntityHitResult ejectPos = VecHelper.getCenterOf(e)
			.e(vec);
		groupedItems.grid.forEach((pair, stack) -> dropItem(ejectPos, stack));
		if (!inventory.a(0)
			.a())
			dropItem(ejectPos, inventory.a(0));
		phase = Phase.IDLE;
		groupedItems = new GroupedItems();
		inventory.setStackInSlot(0, ItemCooldownManager.tick);
		sendData();
	}

	public void dropItem(EntityHitResult ejectPos, ItemCooldownManager stack) {
		PaintingEntity itemEntity = new PaintingEntity(d, ejectPos.entity, ejectPos.c, ejectPos.d, stack);
		itemEntity.m();
		d.c(itemEntity);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (d.v)
			return;
		if (phase == Phase.IDLE && craftingItemPresent())
			checkCompletedRecipe(false);
		if (phase == Phase.INSERTING)
			tryInsert();
	}

	public boolean craftingItemPresent() {
		return !inventory.a(0)
			.a();
	}

	public boolean craftingItemOrCoverPresent() {
		return !inventory.a(0)
			.a() || covered;
	}

	protected void checkCompletedRecipe(boolean poweredStart) {
		if (getSpeed() == 0)
			return;
		if (d.v)
			return;
		List<MechanicalCrafterTileEntity> chain = RecipeGridHandler.getAllCraftersOfChainIf(this,
			poweredStart ? MechanicalCrafterTileEntity::craftingItemPresent
				: MechanicalCrafterTileEntity::craftingItemOrCoverPresent,
			poweredStart);
		if (chain == null)
			return;
		chain.forEach(MechanicalCrafterTileEntity::begin);
	}

	protected void begin() {
		phase = Phase.ACCEPTING;
		groupedItems = new GroupedItems(inventory.a(0));
		inventory.setStackInSlot(0, ItemCooldownManager.tick);
		if (RecipeGridHandler.getPrecedingCrafters(this)
			.isEmpty()) {
			phase = Phase.ASSEMBLING;
			countDown = 500;
		}
		sendData();
	}

	protected void continueIfAllPrecedingFinished() {
		List<MechanicalCrafterTileEntity> preceding = RecipeGridHandler.getPrecedingCrafters(this);
		if (preceding == null) {
			ejectWholeGrid();
			return;
		}

		for (MechanicalCrafterTileEntity mechanicalCrafterTileEntity : preceding)
			if (mechanicalCrafterTileEntity.phase != Phase.WAITING)
				return;

		phase = Phase.ASSEMBLING;
		countDown = Math.max(100, getCountDownSpeed() + 1);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (p().c(HORIZONTAL_FACING) == side)
				return LazyOptional.empty();
			return invSupplier.cast();
		}
		return super.getCapability(cap, side);
	}

	public void connectivityChanged() {
		reRender = true;
		sendData();
		invSupplier.invalidate();
		invSupplier = LazyOptional.of(() -> input.getItemHandler(d, e));
	}

	public Inventory getInventory() {
		return inventory;
	}

}
