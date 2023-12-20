package alephinfinity1.forgeblock.item.swords;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import alephinfinity1.forgeblock.item.IAbilityItem;
import alephinfinity1.forgeblock.item.IQualityItem;
import alephinfinity1.forgeblock.misc.TickHandler;
import alephinfinity1.forgeblock.misc.ability.AbilityResultType;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;

import alephinfinity1.forgeblock.ForgeBlock;
import alephinfinity1.forgeblock.attribute.FBAttributes;
import alephinfinity1.forgeblock.init.ModEnchantments;
import alephinfinity1.forgeblock.misc.capability.mana.IMana;
import alephinfinity1.forgeblock.misc.capability.mana.ManaProvider;
import alephinfinity1.forgeblock.misc.capability.stats_modifier.capability.IItemModifiers;
import alephinfinity1.forgeblock.misc.capability.stats_modifier.capability.ItemModifiersProvider;
import alephinfinity1.forgeblock.misc.event.FBEventHooks;
import alephinfinity1.forgeblock.misc.event.PlayerCastSpellEvent;
import alephinfinity1.forgeblock.misc.tier.FBTier;
import alephinfinity1.forgeblock.network.FBPacketHandler;
import alephinfinity1.forgeblock.network.ManaUpdatePacket;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class SilentDeathItem extends FBSwordItem implements IAbilityItem, IQualityItem {

    public SilentDeathItem(Properties props, FBTier tier, double attackDamageIn, double strengthIn, double critChanceIn,
                           double critDamageIn) {
        super(props, tier, attackDamageIn, strengthIn, critChanceIn, critDamageIn);
    }

    
    @Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(worldIn.isRemote) return ActionResult.resultPass(playerIn.getHeldItem(handIn));
		ItemStack stack = playerIn.getHeldItem(handIn);
		PlayerCastSpellEvent event = FBEventHooks.onPlayerCastSpell(playerIn, stack, this.getAbilityCost(stack, playerIn));
		if(playerIn.getCapability(ManaProvider.MANA_CAPABILITY).orElseThrow(() -> new NullPointerException()).consume(event.getManaConsumed()) && !event.isCanceled()) {
			AbilityResultType flag = activateAbility(worldIn, playerIn, stack);
			if(flag.equals(AbilityResultType.SUCCESS)) {
				IMana mana = playerIn.getCapability(ManaProvider.MANA_CAPABILITY).orElseThrow(NullPointerException::new);
				FBPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerIn), new ManaUpdatePacket(mana.getMana()));
				playerIn.sendStatusMessage(new StringTextComponent(new TranslationTextComponent(this.getUnlocalizedUseAbilityName()).getString() + TextFormatting.AQUA.toString() + " (" + new DecimalFormat("#").format(event.getManaConsumed()) + " " + new TranslationTextComponent("misc.forgeblock.mana").getString() + ")"), true);
				return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
			} else {
				playerIn.sendStatusMessage(new TranslationTextComponent("text.forgeblock.noTarget"), true);
				return ActionResult.resultFail(playerIn.getHeldItem(handIn));
			}
		}
		playerIn.sendStatusMessage(new StringTextComponent(new TranslationTextComponent("text.forgeblock.notEnoughMana").getString()), true);
		return ActionResult.resultFail(playerIn.getHeldItem(handIn));
	}

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot, ItemStack stack) {
        if (equipmentSlot != EquipmentSlotType.MAINHAND) return super.getAttributeModifiers(equipmentSlot);
        Builder<String, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(this.getAttributes(stack));
        builder.putAll(this.getReforgeModifiers(stack));

        int criticalLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.CRITICAL.get(), stack);
        if (criticalLevel != 0)
            builder.put(FBAttributes.CRIT_DAMAGE.getName(), new AttributeModifier(CRITICAL_ENCHANTMENT_MODIFIER, "Crit enchant modifier", 10.0D * criticalLevel, Operation.ADDITION));

        int oneForAllLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.ONE_FOR_ALL.get(), stack);
        if (oneForAllLevel != 0) {
            AtomicDouble weaponDamage = new AtomicDouble(0.0D);
            builder.build().forEach((attrName, modifier) -> {
                if (attrName.equals(SharedMonsterAttributes.ATTACK_DAMAGE.getName())
                        && modifier.getOperation().equals(Operation.ADDITION)) {
                    weaponDamage.getAndAdd(modifier.getAmount());
                }
            });
            builder.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ONE_FOR_ALL_MODIFIER, "1FA enchant modifier", weaponDamage.get() * 2.1, Operation.ADDITION));
        }

        IItemModifiers itemMod = stack.getCapability(ItemModifiersProvider.ITEM_MODIFIERS_CAPABILITY).orElse(null);
        if (itemMod != null) {
            builder.putAll(itemMod.getModifiers(stack));
        }

        return builder.build();
    }

    public Multimap<String, AttributeModifier> getAttributes(ItemStack stack) {
        ImmutableMultimap.Builder<String, AttributeModifier> newAttributes = ImmutableMultimap.builder();
        this.attributes.forEach((attrName, modifier) -> {
            if (modifier.getOperation().equals(Operation.ADDITION)) {
                newAttributes.put(attrName, new AttributeModifier(modifier.getID(),
                        modifier.getName(),
                        modifier.getAmount() * (1.0d + this.getQuality(stack) / 100.0d),
                        modifier.getOperation()));
            } else {
                newAttributes.put(attrName, modifier);
            }
        });
        return newAttributes.build();
    }

    @Override
    public int getQuality(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (Objects.isNull(tag)) {
            return 0;
        } else {
            return tag.getShort("Quality");
        }
    }

    @Override
    public void setQuality(ItemStack stack, int quality) {
        stack.getOrCreateTag().putShort("Quality", (short) quality);
    }

    @Override
    public int getFloor(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (Objects.isNull(tag)) {
            return 0;
        } else {
            return tag.getShort("Floor");
        }
    }

    @Override
    public void setFloor(ItemStack stack, int floor) {
        stack.getOrCreateTag().putShort("Floor", (short) floor);
    }

    @Override
	public List<ITextComponent> abilityDescription(ItemStack stack) {
		List<ITextComponent> list = new ArrayList<>(); 
		list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_0"));
		list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_1"));
		list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_2"));
		list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_3"));
		list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_4"));
        list.add(new TranslationTextComponent("text.forgeblock.sword_desc.silentdeath_5"));
		list.add(new StringTextComponent(new TranslationTextComponent("text.forgeblock.cooldown", new DecimalFormat("#.##").format(this.getCooldown(stack, ForgeBlock.MINECRAFT.player) / 20.0d)).getString()));
		return list;
    }

    @Override
    public AbilityResultType activateAbility(World world, PlayerEntity player, ItemStack stack) {
        AxisAlignedBB bound = new AxisAlignedBB(player.getPositionVector().add(player.getLookVec().rotateYaw(90.0f).rotatePitch(45.0f).scale(2).add(0, -5, 0)), player.getPositionVector().add(player.getLookVec().scale(8.0)).add(player.getLookVec().rotateYaw(90.0f).rotatePitch(45.0f).scale(-2).add(0, 5, 0)));

        List<Entity> list = world.getEntitiesInAABBexcluding(player, bound, EntityPredicates.NOT_SPECTATING);
		List<Entity> accepted = list.stream().filter((entity) -> entity.isAlive() && entity instanceof LivingEntity && entity.getDistanceSq(player) <= 144.0f).collect(Collectors.toList()); //Only alive living entities should be targetted by this ability.
        for(int i = 0; i < (accepted.size() > 0 ? 1 : accepted.size()); i++) {
			int ticksAfter = 10 * i + 5;
			Tuple<LivingEntity, LivingEntity> targets = new Tuple<>(player, (LivingEntity) accepted.get(i));
			TickHandler.shadowFuryTarget.put(targets, TickHandler.serverTicksElapsed + ticksAfter);
		}
		
		//If no entities are in acceptable range, the ability fails.
		if(accepted.isEmpty()) return AbilityResultType.NO_TARGET;


        return AbilityResultType.SUCCESS;
    }

    @Override
    public double getAbilityCost(ItemStack stack) {
        return 0;
    }

    @Override
    public double getAbilityCost(ItemStack stack, PlayerEntity player) {
        return 0;
    }

    @Override
    public int getCooldown(ItemStack stack) {
        return 300;
    }

    @Override
    public int getCooldown(ItemStack stack, PlayerEntity player) {
        if(Objects.isNull(player)) return this.getCooldown(stack);
		return 300;
    }

    @Override
    public String getUnlocalizedUseAbilityName() {
        return "hello";
    }

    @Override
    public FBTier getStackTier(ItemStack stack) {
        if (this.getQuality(stack) >= MAX_QUALITY) {
            return FBTier.changeTier(super.getStackTier(stack), 1);        /* If maximum quality, bump rarity by 1 */
        } else {
            return super.getStackTier(stack);
        }
    }

}
