package com.simibubi.create.content.contraptions.components.deployer;

import static com.simibubi.create.content.contraptions.base.DirectionalKineticBlock.FACING;

import afj;
import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import dcg;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.BlockView.a;
import net.minecraft.world.BlockView.b;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

public class DeployerTileEntity extends KineticTileEntity {

	protected State state;
	protected Mode mode;
	protected ItemCooldownManager heldItem = ItemCooldownManager.tick;
	protected DeployerFakePlayer player;
	protected int timer;
	protected float reach;
	protected boolean boop = false;
	protected List<ItemCooldownManager> overflowItems = new ArrayList<>();
	protected FilteringBehaviour filtering;
	protected boolean redstoneLocked;
	private LazyOptional<IItemHandlerModifiable> invHandler;
	private ListTag deferredInventoryList;

	enum State {
		WAITING, EXPANDING, RETRACTING, DUMPING;
	}

	enum Mode {
		PUNCH, USE
	}

	public DeployerTileEntity(BellBlockEntity<? extends DeployerTileEntity> type) {
		super(type);
		state = State.WAITING;
		mode = Mode.USE;
		heldItem = ItemCooldownManager.tick;
		redstoneLocked = false;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		filtering = new FilteringBehaviour(this, new DeployerFilterSlot());
		behaviours.add(filtering);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!d.v) {
			player = new DeployerFakePlayer((ServerWorld) d);
			if (deferredInventoryList != null) {
				player.bm.b(deferredInventoryList);
				deferredInventoryList = null;
				heldItem = player.dC();
				sendData();
			}
			EntityHitResult initialPos = VecHelper.getCenterOf(e.offset(p().c(FACING)));
			player.d(initialPos.entity, initialPos.c, initialPos.d);
		}
		invHandler = LazyOptional.of(this::createHandler);
	}

	protected void onExtract(ItemCooldownManager stack) {
		player.a(ItemScatterer.RANDOM, stack.i());
		sendData();
		X_();
	}

	protected int getTimerSpeed() {
		return (int) (getSpeed() == 0 ? 0 : afj.a(Math.abs(getSpeed() * 2), 8, 512));
	}

	@Override
	public void aj_() {
		super.aj_();

		if (getSpeed() == 0)
			return;
		if (!d.v && player != null && player.blockBreakingProgress != null) {
			if (d.w(player.blockBreakingProgress.getKey())) {
				d.a(player.X(), player.blockBreakingProgress.getKey(), -1);
				player.blockBreakingProgress = null;
			}
		}
		if (timer > 0) {
			timer -= getTimerSpeed();
			return;
		}
		if (d.v)
			return;

		ItemCooldownManager stack = player.dC();
		if (state == State.WAITING) {
			if (!overflowItems.isEmpty()) {
				timer = getTimerSpeed() * 10;
				return;
			}

			boolean changed = false;
			for (int i = 0; i < player.bm.Z_(); i++) {
				if (overflowItems.size() > 10)
					break;
				ItemCooldownManager item = player.bm.a(i);
				if (item.a())
					continue;
				if (item != stack || !filtering.test(item)) {
					overflowItems.add(item);
					player.bm.a(i, ItemCooldownManager.tick);
					changed = true;
				}
			}

			if (changed) {
				sendData();
				timer = getTimerSpeed() * 10;
				return;
			}

			Direction facing = p().c(FACING);
			if (mode == Mode.USE && !DeployerHandler.shouldActivate(stack, d, e.offset(facing, 2))) {
				timer = getTimerSpeed() * 10;
				return;
			}

			// Check for advancement conditions
			if (mode == Mode.PUNCH && !boop && startBoop(facing))
				return;

			if (redstoneLocked)
				return;

			state = State.EXPANDING;
			EntityHitResult movementVector = getMovementVector();
			EntityHitResult rayOrigin = VecHelper.getCenterOf(e)
				.e(movementVector.a(3 / 2f));
			EntityHitResult rayTarget = VecHelper.getCenterOf(e)
				.e(movementVector.a(5 / 2f));
			BlockView rayTraceContext =
				new BlockView(rayOrigin, rayTarget, a.b, b.a, player);
			dcg result = d.a(rayTraceContext);
			reach = (float) (.5f + Math.min(result.e()
				.d(rayOrigin)
				.f(), .75f));

			timer = 1000;
			sendData();
			return;
		}

		if (state == State.EXPANDING) {
			if (boop)
				triggerBoop();
			else
				activate();

			state = State.RETRACTING;
			timer = 1000;
			sendData();
			return;
		}

		if (state == State.RETRACTING) {
			state = State.WAITING;
			timer = 500;
			sendData();
			return;
		}

	}

	public boolean startBoop(Direction facing) {
		if (!d.w(e.offset(facing, 1)) || !d.w(e.offset(facing, 2)))
			return false;
		BlockPos otherDeployer = e.offset(facing, 4);
		if (!d.p(otherDeployer))
			return false;
		BeehiveBlockEntity otherTile = d.c(otherDeployer);
		if (!(otherTile instanceof DeployerTileEntity))
			return false;
		DeployerTileEntity deployerTile = (DeployerTileEntity) otherTile;
		if (d.d_(otherDeployer)
			.c(FACING)
			.getOpposite() != facing || deployerTile.mode != Mode.PUNCH)
			return false;

		boop = true;
		reach = 1f;
		timer = 1000;
		state = State.EXPANDING;
		sendData();
		return true;
	}

	public void triggerBoop() {
		BeehiveBlockEntity otherTile = d.c(e.offset(p().c(FACING), 4));
		if (!(otherTile instanceof DeployerTileEntity))
			return;

		DeployerTileEntity deployerTile = (DeployerTileEntity) otherTile;
		if (!deployerTile.boop || deployerTile.state != State.EXPANDING)
			return;
		if (deployerTile.timer > 0)
			return;

		// everything should be met
		boop = false;
		deployerTile.boop = false;
		deployerTile.state = State.RETRACTING;
		deployerTile.timer = 1000;
		deployerTile.sendData();

		// award nearby players
		List<ServerPlayerEntity> players =
			d.a(ServerPlayerEntity.class, new Timer(e).g(9));
		players.forEach(AllTriggers.DEPLOYER_BOOP::trigger);
	}

	protected void activate() {
		EntityHitResult movementVector = getMovementVector();
		Direction direction = p().c(FACING);
		EntityHitResult center = VecHelper.getCenterOf(e);
		BlockPos clickedPos = e.offset(direction, 2);
		player.p = direction.asRotation();
		player.q = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;

		DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
		if (player != null)
			heldItem = player.dC();
	}

	protected EntityHitResult getMovementVector() {
		if (!AllBlocks.DEPLOYER.has(p()))
			return EntityHitResult.a;
		return EntityHitResult.b(p().c(FACING)
			.getVector());
	}

	@Override
	protected void fromTag(PistonHandler blockState, CompoundTag compound, boolean clientPacket) {
		state = NBTHelper.readEnum(compound, "State", State.class);
		mode = NBTHelper.readEnum(compound, "Mode", Mode.class);
		timer = compound.getInt("Timer");
		redstoneLocked = compound.getBoolean("Powered");

		deferredInventoryList = compound.getList("Inventory", NBT.TAG_COMPOUND);
		overflowItems = NBTHelper.readItemList(compound.getList("Overflow", NBT.TAG_COMPOUND));
		if (compound.contains("HeldItem"))
			heldItem = ItemCooldownManager.a(compound.getCompound("HeldItem"));
		super.fromTag(blockState, compound, clientPacket);

		if (!clientPacket)
			return;
		reach = compound.getFloat("Reach");
		if (compound.contains("Particle")) {
			ItemCooldownManager particleStack = ItemCooldownManager.a(compound.getCompound("Particle"));
			SandPaperItem.spawnParticles(VecHelper.getCenterOf(e)
				.e(getMovementVector().a(2f)), particleStack, this.d);
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		NBTHelper.writeEnum(compound, "Mode", mode);
		NBTHelper.writeEnum(compound, "State", state);
		compound.putInt("Timer", timer);
		compound.putBoolean("Powered", redstoneLocked);

		if (player != null) {
			compound.put("HeldItem", player.dC()
				.serializeNBT());
			ListTag invNBT = new ListTag();
			player.bm.a(invNBT);
			compound.put("Inventory", invNBT);
			compound.put("Overflow", NBTHelper.writeItemList(overflowItems));
		}

		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		compound.putFloat("Reach", reach);
		if (player == null)
			return;
		compound.put("HeldItem", player.dC()
			.serializeNBT());
		if (player.spawnedItemEffects != null) {
			compound.put("Particle", player.spawnedItemEffects.serializeNBT());
			player.spawnedItemEffects = null;
		}
	}

	private IItemHandlerModifiable createHandler() {
		return new DeployerItemHandler(this);
	}
	
	public void redstoneUpdate() {
		if (d.v)
			return;
		boolean blockPowered = d.r(e);
		if (blockPowered == redstoneLocked)
			return;
		redstoneLocked = blockPowered;
		sendData();
	}

	public AllBlockPartials getHandPose() {
		return mode == Mode.PUNCH ? AllBlockPartials.DEPLOYER_HAND_PUNCHING
			: heldItem.a() ? AllBlockPartials.DEPLOYER_HAND_POINTING : AllBlockPartials.DEPLOYER_HAND_HOLDING;
	}

	@Override
	public Timer getRenderBoundingBox() {
		return super.getRenderBoundingBox().g(3);
	}

	@Override
	public void al_() {
		super.al_();
		if (invHandler != null)
			invHandler.invalidate();
	}

	public void changeMode() {
		mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
		X_();
		sendData();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (isItemHandlerCap(cap) && invHandler != null) 
			return invHandler.cast();
		return super.getCapability(cap, side);
	}
	
	@Override
	public boolean addToTooltip(List<Text> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;
		if (getSpeed() == 0)
			return false;
		if (overflowItems.isEmpty())
			return false;
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		return true;
	}

}
