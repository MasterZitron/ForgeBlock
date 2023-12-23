package alephinfinity1.forgeblock.item.swords;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import alephinfinity1.forgeblock.misc.tier.FBTier;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class HunterKnifeItem extends FBSwordItem{

    public HunterKnifeItem(Properties props, FBTier tier, double attackDamageIn, double strengthIn, double critChanceIn,
			double critDamageIn) {
		super(props, tier, attackDamageIn, strengthIn, critChanceIn, critDamageIn);
	}

	public HunterKnifeItem(Properties props, FBTier tier, Multimap<String, AttributeModifier> modifiers) {
		super(props, tier, modifiers);
	}
}
