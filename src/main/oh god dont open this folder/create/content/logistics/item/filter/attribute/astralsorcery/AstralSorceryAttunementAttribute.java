package com.simibubi.create.content.logistics.item.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

public class AstralSorceryAttunementAttribute implements ItemAttribute {
    String constellationName;

    public AstralSorceryAttunementAttribute(String constellationName) {
        this.constellationName = constellationName;
    }

    @Override
    public boolean appliesTo(ItemCooldownManager itemStack) {
        CompoundTag nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        Identifier itemResource = itemStack.b().getRegistryName();
        if(itemResource != null && itemResource.toString().contains("shifting_star_")) {
            constellation = itemResource.toString().replace("shifting_star_", "");
        }

        return constellation.equals(constellationName);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemCooldownManager itemStack) {
        CompoundTag nbt = extractAstralNBT(itemStack);
        String constellation = nbt.contains("constellation") ? nbt.getString("constellation") : nbt.getString("constellationName");

        // Special handling for shifting stars
        Identifier itemResource = itemStack.b().getRegistryName();
        if(itemResource != null && itemResource.toString().contains("shifting_star_")) {
            constellation = itemResource.toString().replace("shifting_star_", "");
        }

        List<ItemAttribute> atts = new ArrayList<>();
        if(constellation.length() > 0) {
            atts.add(new AstralSorceryAttunementAttribute(constellation));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_constellation";
    }

    @Override
    public Object[] getTranslationParameters() {
        Identifier constResource = new Identifier(constellationName);
        String something = new TranslatableText(String.format("%s.constellation.%s", constResource.getNamespace(), constResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("constellation", this.constellationName);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryAttunementAttribute(nbt.getString("constellation"));
    }

    private CompoundTag extractAstralNBT(ItemCooldownManager stack) {
        return stack.o() != null ? stack.o().getCompound("astralsorcery") : new CompoundTag();
    }
}
