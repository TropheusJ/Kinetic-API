package com.simibubi.create.content.curiosities.tools;

import java.util.function.Supplier;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.recipe.FireworkRocketRecipe;
import com.simibubi.create.AllItems;

public enum AllToolTiers implements SuspiciousStewItem {

	RADIANT(4, 1024, 16.0F, 3.5F, 10, () -> {
		return FireworkRocketRecipe.a(AllItems.REFINED_RADIANCE.get());
	}),

	;
	
	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final NetworkUtils<FireworkRocketRecipe> repairMaterial;

	private AllToolTiers(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn,
			int enchantabilityIn, Supplier<FireworkRocketRecipe> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.repairMaterial = new NetworkUtils<>(repairMaterialIn);
	}

	public int a() {
		return this.maxUses;
	}

	public float b() {
		return this.efficiency;
	}

	public float c() {
		return this.attackDamage;
	}

	public int d() {
		return this.harvestLevel;
	}

	public int e() {
		return this.enchantability;
	}

	public FireworkRocketRecipe f() {
		return this.repairMaterial.a();
	}
}
