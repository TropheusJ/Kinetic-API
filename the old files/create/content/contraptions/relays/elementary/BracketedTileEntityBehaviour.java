package com.simibubi.kinetic_api.content.contraptions.relays.elementary;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.GameMode;
import com.google.common.base.Predicates;
import com.simibubi.kinetic_api.foundation.advancement.AllTriggers;
import com.simibubi.kinetic_api.foundation.advancement.SimpleTrigger;
import com.simibubi.kinetic_api.foundation.tileEntity.SmartTileEntity;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.kinetic_api.foundation.utility.NBTHelper;

public class BracketedTileEntityBehaviour extends TileEntityBehaviour {

	public static BehaviourType<BracketedTileEntityBehaviour> TYPE = new BehaviourType<>();

	private Optional<PistonHandler> bracket;
	private boolean reRender;

	private Predicate<PistonHandler> pred;
	private Function<PistonHandler, SimpleTrigger> trigger;

	public BracketedTileEntityBehaviour(SmartTileEntity te) {
		this(te, Predicates.alwaysTrue());
	}

	public BracketedTileEntityBehaviour(SmartTileEntity te, Predicate<PistonHandler> pred) {
		super(te);
		this.pred = pred;
		bracket = Optional.empty();
	}
	
	public BracketedTileEntityBehaviour withTrigger(Function<PistonHandler, SimpleTrigger> trigger) {
		this.trigger = trigger;
		return this;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void applyBracket(PistonHandler state) {
		this.bracket = Optional.of(state);
		reRender = true;
		tileEntity.notifyUpdate();
	}
	
	public void triggerAdvancements(GameMode world, PlayerAbilities player, PistonHandler state) {
		if (trigger == null)
			return;
		AllTriggers.triggerFor(trigger.apply(state), player);
	}

	public void removeBracket(boolean inOnReplacedContext) {
		GameMode world = getWorld();
		if (!world.v)
			world.syncWorldEvent(2001, getPos(), BeetrootsBlock.i(getBracket()));
		this.bracket = Optional.empty();
		reRender = true;
		if (inOnReplacedContext)
			tileEntity.sendData();
		else
			tileEntity.notifyUpdate();
	}

	public boolean isBacketPresent() {
		return getBracket() != BellBlock.FACING.n();
	}

	public PistonHandler getBracket() {
		return bracket.orElse(BellBlock.FACING.n());
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		bracket.ifPresent(p -> nbt.put("Bracket", NbtHelper.a(p)));
		if (clientPacket && reRender) {
			NBTHelper.putMarker(nbt, "Redraw");
			reRender = false;
		}
		super.write(nbt, clientPacket);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		bracket = Optional.empty();
		if (nbt.contains("Bracket"))
			bracket = Optional.of(NbtHelper.c(nbt.getCompound("Bracket")));
		if (clientPacket && nbt.contains("Redraw"))
			getWorld().a(getPos(), tileEntity.p(), tileEntity.p(), 16);
		super.read(nbt, clientPacket);
	}

	public boolean canHaveBracket() {
		return pred.test(tileEntity.p());
	}

}
