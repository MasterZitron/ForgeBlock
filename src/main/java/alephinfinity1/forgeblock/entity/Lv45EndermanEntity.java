package alephinfinity1.forgeblock.entity;

import alephinfinity1.forgeblock.attribute.FBAttributes;
import alephinfinity1.forgeblock.misc.FBCreatureAttributes;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class Lv45EndermanEntity extends Lv1EndermanEntity {

	public Lv45EndermanEntity(EntityType<? extends EndermanEntity> type, World worldIn) {
		super(type, worldIn);
	}

	
	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.registerFBAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6000.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(600.0D);
		this.getAttribute(FBAttributes.CRIT_CHANCE).setBaseValue(0.0D);
	}
	
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if(this.isInvulnerableTo(source)) return false;
		else if(source.isMagicDamage()) { //If magic damage, bypass enderman teleport immunity.
			if(source.getTrueSource() != null) {
				if(source.getTrueSource() instanceof PlayerEntity) //If player entity
					return super.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) source.getTrueSource()), amount);
				else if(source.getTrueSource() instanceof LivingEntity) //If mob entity
					return super.attackEntityFrom(DamageSource.causeMobDamage((LivingEntity) source.getTrueSource()), amount);
				else return super.attackEntityFrom(source, amount); //If unknown magic damage
			} else return super.attackEntityFrom(source, amount);
		} else { //If arrow etc., block damage.
			return super.attackEntityFrom(source, amount);
		}
	}
	
	@Override
	protected int getExperiencePoints(PlayerEntity player) {
		return 9;
	}

	@Override
	public int getLevel() {
		return 45;
	}

	@Override
	public double getCoins() {
		return 12.0D;
	}

	@Override
	public double getCombatXP() {
		return 32.0D;
	}
	
	@Override
	public CreatureAttribute getCreatureAttribute() {
		return FBCreatureAttributes.ENDER;
	}

}
