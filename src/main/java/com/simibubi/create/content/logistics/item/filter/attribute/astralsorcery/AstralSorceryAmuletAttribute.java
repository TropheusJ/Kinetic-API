package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;
import net.minecraft.enchantment.DamageEnchantment;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraftforge.registries.ForgeRegistries;

public class AstralSorceryAmuletAttribute implements ItemAttribute {
    String enchName;
    int enchType;

    public AstralSorceryAmuletAttribute(String enchName, int enchType) {
        this.enchName = enchName;
        this.enchType = enchType;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        for (Tag trait : extractTraitList(itemStack)) {
            if(((CompoundTag) trait).getString("ench").equals(this.enchName)
                    && ((CompoundTag)trait).getInt("type") == this.enchType)
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        ListTag traits = extractTraitList(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < traits.size(); i++) {
            atts.add(new AstralSorceryAmuletAttribute(
                    traits.getCompound(i).getString("ench"),
                    traits.getCompound(i).getInt("type")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_amulet";
    }

    @Override
    public Object[] getTranslationParameters() {
        Identifier traitResource = new Identifier(enchName);
        String something = "";

        DamageEnchantment enchant = ForgeRegistries.ENCHANTMENTS.getValue(Identifier.tryParse(enchName));
        if(enchant != null) {
            something = new TranslatableText(enchant.g()).getString();
        }

        if(enchType == 1) something = "existing " + something;

        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("enchName", this.enchName);
        nbt.putInt("enchType", this.enchType);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryAmuletAttribute(nbt.getString("enchName"), nbt.getInt("enchType"));
    }

    private ListTag extractTraitList(ItemCooldownManager stack) {
        return stack.o() != null ? stack.o().getCompound("astralsorcery").getList("amuletEnchantments", 10) : new ListTag();
    }
}
