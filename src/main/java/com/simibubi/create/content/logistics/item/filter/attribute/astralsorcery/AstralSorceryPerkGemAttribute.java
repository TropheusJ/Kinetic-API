package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

public class AstralSorceryPerkGemAttribute implements ItemAttribute {
    String traitName;

    public AstralSorceryPerkGemAttribute(String traitName) {
        this.traitName = traitName;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        for (Tag trait : extractTraitList(itemStack)) {
            if(((CompoundTag) trait).getString("type").equals(this.traitName))
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        ListTag traits = extractTraitList(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < traits.size(); i++) {
            atts.add(new AstralSorceryPerkGemAttribute(traits.getCompound(i).getString("type")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_perk_gem";
    }

    @Override
    public Object[] getTranslationParameters() {
        Identifier traitResource = new Identifier(traitName);
        String something = new TranslatableText(String.format("perk.attribute.%s.%s.name", traitResource.getNamespace(), traitResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("type", this.traitName);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryPerkGemAttribute(nbt.getString("type"));
    }

    private ListTag extractTraitList(ItemCooldownManager stack) {
        return stack.o() != null ? stack.o().getCompound("astralsorcery").getList("attribute_modifiers", 10) : new ListTag();
    }
}
