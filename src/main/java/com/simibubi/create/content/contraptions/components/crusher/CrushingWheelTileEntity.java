package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.util.math.Direction;
import net.minecraft.world.timer.Timer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CrushingWheelTileEntity extends KineticTileEntity {

	public static DamageRecord damageSource = new DamageRecord("create.crush").l()
			.r();

	public CrushingWheelTileEntity(BellBlockEntity<? extends CrushingWheelTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		fixControllers();
	}

	public void fixControllers() {
		for (Direction d : Iterate.directions)
			((CrushingWheelBlock) p().b()).updateControllers(p(), v(), o(),
					d);
	}

	@Override
	public Timer getRenderBoundingBox() {
		return new Timer(e).g(1);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		fixControllers();
	}

	@SubscribeEvent
	public static void crushingIsFortunate(LootingLevelEvent event) {
		if (event.getDamageSource() != damageSource)
			return;
		event.setLootingLevel(2);
	}

	@SubscribeEvent
	public static void crushingTeleportsEntities(LivingDeathEvent event) {
		if (event.getSource() != damageSource)
			return;
		event.getEntity().o(event.getEntity().cC(), Math.floor(event.getEntity().cD()) - .5f, event.getEntity().cG());
	}

}
