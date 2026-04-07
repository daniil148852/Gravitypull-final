package me.daniil148852.gravitygauntlet;

import me.daniil148852.gravitygauntlet.extension.GravityGauntletExtension;
import me.daniil148852.gravitygauntlet.mixin.FallingBlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class GravityGauntletItem extends Item {
    public static final List<OrbitingBlock> ORBITING_BLOCKS = new ArrayList<>();

    public GravityGauntletItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user.isSneaking()) {
            shootBlocks(user);
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();
        if (user == null || user.isSneaking()) {
            return ActionResult.PASS;
        }

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (!world.isClient && !state.isAir() && state.getBlock() != Blocks.BEDROCK) {
            world.removeBlock(pos, false);

            FallingBlockEntity fallingBlock = FallingBlockEntityAccessor.createFallingBlockEntity(
                world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, state
            );
            fallingBlock.setNoGravity(true);
            fallingBlock.dropItem = false;
            
            FallingBlockEntityAccessor accessor = (FallingBlockEntityAccessor) fallingBlock;
            accessor.setHurtEntities(true);
            fallingBlock.fallDistance = 8.0f;

            // Спавним сущность в мир
            world.spawnEntity(fallingBlock);

            // ВАЖНО: Вместо NBT используем наш интерфейс для передачи данных в Mixin
            double randomAngle = Math.random() * Math.PI * 2;
            ((GravityGauntletExtension) fallingBlock).gravityGauntlet$setOrbiting(user.getUuid(), randomAngle);

            // Добавляем в локальный список для отслеживания
            OrbitingBlock orbitingBlock = new OrbitingBlock();
            orbitingBlock.entity = fallingBlock;
            orbitingBlock.owner = user.getUuid();
            orbitingBlock.angle = randomAngle;
            ORBITING_BLOCKS.add(orbitingBlock);

            return ActionResult.SUCCESS;
        }

        return ActionResult.SUCCESS;
    }

    private void shootBlocks(PlayerEntity user) {
        Iterator<OrbitingBlock> iterator = ORBITING_BLOCKS.iterator();
        Vec3d look = user.getRotationVec(1.0f);

        while (iterator.hasNext()) {
            OrbitingBlock orbitingBlock = iterator.next();
            if (orbitingBlock.owner.equals(user.getUuid())) {
                FallingBlockEntity entity = orbitingBlock.entity;
                if (entity != null && !entity.isRemoved()) {
                    entity.setNoGravity(false);
                    entity.setVelocity(
                        look.x * 2.0,
                        look.y * 2.0 + 0.3,
                        look.z * 2.0
                    );
                    entity.velocityModified = true;
                    
                    FallingBlockEntityAccessor accessor = (FallingBlockEntityAccessor) entity;
                    accessor.setHurtEntities(true);
                    entity.fallDistance = 15.0f;

                    // ВАЖНО: Сообщаем миксину, что блок запущен!
                    ((GravityGauntletExtension) entity).gravityGauntlet$setLaunched(true);
                }
                iterator.remove();
            }
        }
    }

    public static class OrbitingBlock {
        public FallingBlockEntity entity;
        public UUID owner;
        public double angle;
    }
}
