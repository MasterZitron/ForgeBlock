package alephinfinity1.forgeblock.entity;

import alephinfinity1.forgeblock.attribute.FBAttributes;
import alephinfinity1.forgeblock.misc.FBCreatureAttributes;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class VoidlingRadicalEntity extends EndermanEntity implements IFBEntity {

	public VoidlingRadicalEntity(EntityType<? extends EndermanEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.registerFBAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25000000.0D);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7400.0D);
		this.getAttribute(FBAttributes.CRIT_CHANCE).setBaseValue(0.0D);
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new EndermanEntity.StareGoal(this));
		this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.0D, true));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp());
		this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
		this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(6, new SwimGoal(this));
		this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
		this.targetSelector.addGoal(1, new EndermanEntity.FindPlayerGoal(this));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<EndermiteEntity>(this, EndermiteEntity.class, 10, true, false, (entity) -> {return entity instanceof EndermiteEntity;}));
	}
	
	//Disable all teleportation methods.
	
	@Override
	public boolean teleportRandomly() {
		return false;
	}
	
	@Override
	public boolean teleportToEntity(Entity p_70816_1_) {
		return false;
	}
	
	@Override
	public boolean teleportTo(double x, double y, double z) {
		return false;
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
	public int getLevel() {
		return 2500;
	}

	@Override
	public double getCoins() {
		return 0D;
	}

	@Override
	public double getCombatXP() {
		return 2000.0D;
	}
	
	@Override
	public CreatureAttribute getCreatureAttribute() {
		return FBCreatureAttributes.ENDER;
	}

}
