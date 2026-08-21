package com.mango.betterlead.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;

public class CreatureCatcherItem extends Item {
    public CreatureCatcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

            ItemStack stack = player.getItemInHand(context.getHand());
            Level level = context.getLevel();

            if (!isFilled(stack)) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            BlockPos spawnPos = context.getClickedPos()
                    .relative(context.getClickedFace());

            CompoundTag catcherData = stack
                    .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                    .copyTag();

            if (!(catcherData.get("CapturedEntity") instanceof CompoundTag capturedEntity)) {
                return InteractionResult.PASS;
            }

            Entity releasedEntity = EntityType.loadEntityRecursive(capturedEntity, level, entity -> {
                entity.moveTo(
                        spawnPos.getX() + 0.5,
                        spawnPos.getY(),
                        spawnPos.getZ() + 0.5,
                        entity.getYRot(),
                        entity.getXRot()
                );
                return entity;
            });

            level.addFreshEntity(releasedEntity);

            catcherData.remove("CapturedEntity");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(catcherData));
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            player.getInventory().setChanged();

            return InteractionResult.SUCCESS;
        }






    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand)
    {
        return tryCapture(player, target, hand);
    }

    public InteractionResult tryCapture(Player player, LivingEntity target, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (target instanceof Player) {
            return InteractionResult.PASS;
        }
        else if (target instanceof EnderDragon || target instanceof WitherBoss) {
            return InteractionResult.PASS;
        }
        else if (isFilled(heldStack)) {
            player.displayClientMessage(Component.literal("Already contains a creature!"), true);
            return InteractionResult.PASS;
        }


        if (player.level().isClientSide) {
            player.displayClientMessage(Component.literal("Yoiked!"), true);
            return InteractionResult.SUCCESS;
        }

        CompoundTag capturedEntity = new CompoundTag();
        target.saveWithoutId(capturedEntity);
        capturedEntity.putString(
                "id",
                BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString()
        );

        CompoundTag catcherData = heldStack
                .getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag();

        catcherData.put("CapturedEntity", capturedEntity);
        heldStack.set(DataComponents.CUSTOM_DATA, CustomData.of(catcherData));
        player.getInventory().setChanged();


        target.discard();

        return InteractionResult.SUCCESS;


    }

    private boolean isFilled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag()
                .contains("CapturedEntity", Tag.TAG_COMPOUND);
    }
}
