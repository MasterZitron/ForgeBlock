package alephinfinity1.forgeblock.item.swords;

import com.google.common.collect.Multimap;

import alephinfinity1.forgeblock.misc.tier.FBTier;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


public class RevenantFalchionItem extends FBSwordItem{


    public RevenantFalchionItem(Properties props, FBTier tier, double attackDamageIn, double strengthIn, double critChanceIn,
			double critDamageIn) {
		super(props, tier, attackDamageIn, strengthIn, critChanceIn, critDamageIn);
	}

	public RevenantFalchionItem(Properties props, FBTier tier, Multimap<String, AttributeModifier> modifiers) {
		super(props, tier, modifiers);
	}
}
