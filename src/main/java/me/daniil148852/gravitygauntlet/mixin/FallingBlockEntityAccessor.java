package me.daniil148852.gravitygauntlet.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
    
    // Сигнатура должна строго совпадать с приватным конструктором FallingBlockEntity
    @Invoker("<init>")
    static FallingBlockEntity createFallingBlockEntity(World world, double x, double y, double z, BlockState state) {
        throw new AssertionError();
    }

    @Accessor("hurtEntities")
    void setHurtEntities(boolean hurtEntities);

    // У FallingBlockEntity нет поля "nbt", правильное поле называется "blockEntityData"
    @Accessor("blockEntityData")
    NbtCompound getBlockEntityData();

    @Accessor("blockEntityData")
    void setBlockEntityData(NbtCompound nbt);
}
