package me.daniil148852.gravitygauntlet.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

// ВАЖНО: Интерфейс для вызова кастомных методов снаружи (Duck Typing)
// В ваших предметах кастуйте сущность к этому интерфейсу: ((GravityGauntletExtension) entity).setOrbiting(...)
interface GravityGauntletExtension {
    void gravityGauntlet$setOrbiting(UUID owner, double angle);
    void gravityGauntlet$setLaunched(boolean launched);
}

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity implements GravityGauntletExtension {
    
    @Unique
    private UUID gravityGauntlet$owner;
    @Unique
    private double gravityGauntlet$angle;
    @Unique
    private boolean gravityGauntlet$isOrbiting;
    @Unique
    private boolean gravityGauntlet$isLaunched;

    public FallingBlockEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void gravityGauntlet$onTick(CallbackInfo ci) {
        World world = this.getWorld();

        // 1. Логика вращения вокруг игрока
        if (gravityGauntlet$isOrbiting && gravityGauntlet$owner != null) {
            if (!world.isClient) {
                PlayerEntity owner = world.getPlayerByUuid(gravityGauntlet$owner);
                if (owner == null || owner.isRemoved()) {
                    this.discard();
                    return;
                }

                gravityGauntlet$angle += 0.05;

                double radius = 3.0;
                double x = owner.getX() + Math.cos(gravityGauntlet$angle) * radius;
                double y = owner.getY() + 1.5;
                double z = owner.getZ() + Math.sin(gravityGauntlet$angle) * radius;

                this.setPosition(x, y, z);
                this.setVelocity(0, 0, 0);
                this.setNoGravity(true);
                this.velocityModified = true; // Важно для плавной синхронизации с клиентом
            }
            ci.cancel(); // Отменяем обычный tick падения блока
            return;
        }

        // 2. Логика полета и нанесения урона (заменяет несуществующий onEntityCollision)
        if (gravityGauntlet$isLaunched && !world.isClient) {
            // Ищем существ (кроме владельца) в хитбоксе падающего блока
            Box box = this.getBoundingBox().expand(0.2);
            List<Entity> targets = world.getOtherEntities(this, box, entity -> 
                entity instanceof LivingEntity && !entity.getUuid().equals(gravityGauntlet$owner)
            );

            for (Entity entity : targets) {
                LivingEntity target = (LivingEntity) entity;
                target.damage(this.getDamageSources().fallingBlock((FallingBlockEntity) (Object) this), 10.0f);
                
                // Можно раскомментировать, чтобы блок пропадал после удара 1 цели:
                // this.discard();
                // break;
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void gravityGauntlet$writeCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        if (gravityGauntlet$owner != null) {
            // Уникальные ключи защитят от поломки NBT других модов
            nbt.putUuid("GravityGauntlet_Owner", gravityGauntlet$owner);
            nbt.putDouble("GravityGauntlet_Angle", gravityGauntlet$angle);
            nbt.putBoolean("GravityGauntlet_IsOrbiting", gravityGauntlet$isOrbiting);
            nbt.putBoolean("GravityGauntlet_IsLaunched", gravityGauntlet$isLaunched);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void gravityGauntlet$readCustomNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.containsUuid("GravityGauntlet_Owner")) {
            gravityGauntlet$owner = nbt.getUuid("GravityGauntlet_Owner");
            gravityGauntlet$angle = nbt.getDouble("GravityGauntlet_Angle");
            gravityGauntlet$isOrbiting = nbt.getBoolean("GravityGauntlet_IsOrbiting");
            gravityGauntlet$isLaunched = nbt.getBoolean("GravityGauntlet_IsLaunched");
        }
    }

    // Реализация методов из интерфейса Duck Typing
    @Override
    public void gravityGauntlet$setOrbiting(UUID owner, double angle) {
        this.gravityGauntlet$owner = owner;
        this.gravityGauntlet$angle = angle;
        this.gravityGauntlet$isOrbiting = true;
        this.gravityGauntlet$isLaunched = false;
        this.setNoGravity(true);
    }

    @Override
    public void gravityGauntlet$setLaunched(boolean launched) {
        this.gravityGauntlet$isLaunched = launched;
        this.gravityGauntlet$isOrbiting = false;
        this.setNoGravity(!launched); // Возвращаем гравитацию при запуске (по желанию)
    }
}
