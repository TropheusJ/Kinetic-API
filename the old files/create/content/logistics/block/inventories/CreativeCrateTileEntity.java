package com.simibubi.kinetic_api.content.logistics.block.inventories;

import java.util.List;
import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CreativeCrateTileEntity extends CrateTileEntity {

	public CreativeCrateTileEntity(BellBlockEntity<? extends CreativeCrateTileEntity> type) {
		super(type);
		inv = new BottomlessItemHandler(filtering::getFilter);
		itemHandler = LazyOptional.of(() -> inv);
	}

	FilteringBehaviour filtering;
	LazyOptional<IItemHandler> itemHandler;
	private BottomlessItemHandler inv;

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(filtering = createFilter().onlyActiveWhen(this::filterVisible)
			.withCallback(this::filterChanged));
	}

	private boolean filterVisible() {
		if (!n() || isDoubleCrate() && !isSecondaryCrate())
			return false;
		return true;
	}

	private void filterChanged(ItemCooldownManager filter) {
		if (!filterVisible())
			return;
		CreativeCrateTileEntity otherCrate = getOtherCrate();
		if (otherCrate == null)
			return;
		if (ItemCooldownManager.c(filter, otherCrate.filtering.getFilter()))
			return;
		otherCrate.filtering.setFilter(filter);
	}

	@Override
	public void al_() {
		super.al_();
		if (itemHandler != null)
			itemHandler.invalidate();
	}

	private CreativeCrateTileEntity getOtherCrate() {
		if (!AllBlocks.CREATIVE_CRATE.has(p()))
			return null;
		BeehiveBlockEntity tileEntity = d.c(e.offset(getFacing()));
		if (tileEntity instanceof CreativeCrateTileEntity)
			return (CreativeCrateTileEntity) tileEntity;
		return null;
	}

	public void onPlaced() {
		if (!isDoubleCrate())
			return;
		CreativeCrateTileEntity otherCrate = getOtherCrate();
		if (otherCrate == null)
			return;

		filtering.withCallback($ -> {
		});
		filtering.setFilter(otherCrate.filtering.getFilter());
		filtering.withCallback(this::filterChanged);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return itemHandler.cast();
		return super.getCapability(cap, side);
	}

	public FilteringBehaviour createFilter() {
		return new FilteringBehaviour(this, new ValueBoxTransform() {

			@Override
			protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
				MatrixStacker.of(ms)
					.rotateX(90);
			}

			@Override
			protected EntityHitResult getLocalOffset(PistonHandler state) {
				return new EntityHitResult(0.5, 13 / 16d, 0.5);
			}

			protected float getScale() {
				return super.getScale() * 1.5f;
			};

		});
	}

}