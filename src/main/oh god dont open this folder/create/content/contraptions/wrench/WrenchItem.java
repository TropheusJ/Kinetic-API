package com.simibubi.create.content.contraptions.wrench;

import javax.annotation.Nonnull;
import apx;
import bnx;
import com.simibubi.create.AllItems;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.HoeItem;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class WrenchItem extends HoeItem {

	public WrenchItem(a properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public Difficulty a(bnx context) {
		PlayerAbilities player = context.n();
		if (player == null || !player.eJ())
			return super.a(context);

		PistonHandler state = context.p()
			.d_(context.a());
		if (!(state.b() instanceof IWrenchable))
			return super.a(context);
		IWrenchable actor = (IWrenchable) state.b();

		if (player.bt())
			return actor.onSneakWrenched(state, context);
		return actor.onWrenched(state, context);
	}
	
	public static void wrenchInstaKillsMinecarts(AttackEntityEvent event) {
		apx target = event.getTarget();
		if (!(target instanceof ScheduleBuilder))
			return;
		PlayerAbilities player = event.getPlayer();
		ItemCooldownManager heldItem = player.dC();
		if (!AllItems.WRENCH.isIn(heldItem))
			return;
		if (player.b_())
			return;
		ScheduleBuilder minecart = (ScheduleBuilder) target;
		minecart.a(DamageRecord.a(player), 100);
	}
	
}
