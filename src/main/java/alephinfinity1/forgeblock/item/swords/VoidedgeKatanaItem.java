package alephinfinity1.forgeblock.item.swords;

import com.google.common.collect.Multimap;

import alephinfinity1.forgeblock.misc.tier.FBTier;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class VoidedgeKatanaItem extends FBSwordItem{

    public VoidedgeKatanaItem(Properties props, FBTier tier, double attackDamageIn, double strengthIn, double critChanceIn,
			double critDamageIn) {
		super(props, tier, attackDamageIn, strengthIn, critChanceIn, critDamageIn);
	}

	public VoidedgeKatanaItem(Properties props, FBTier tier, Multimap<String, AttributeModifier> modifiers) {
		super(props, tier, modifiers);
	}
}

