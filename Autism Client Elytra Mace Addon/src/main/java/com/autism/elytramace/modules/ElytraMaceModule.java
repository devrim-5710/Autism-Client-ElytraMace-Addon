package com.autism.elytramace.modules;

import com.autism.elytramace.ElytraMaceAddon;
import autismclient.api.module.IntSetting;
import autismclient.modules.Module;
import autismclient.util.AutismInventoryHelper;
import autismclient.util.AutismRotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public final class ElytraMaceModule extends Module {
    private final IntSetting rocketDelay = add(new IntSetting("rocketDelay", "Rocket Delay (ticks)", 12, 5, 30, 1)
        .group("Flight"));
    private final IntSetting searchChunk = add(new IntSetting("searchChunk", "Chunk", 16, 1, 32, 1)
        .group("Flight"));

    private enum State { TAKEOFF, ROCKETS, TRACK, SWAP_WAIT, ATTACK, RE_ELYTRA }
    private State state = State.TAKEOFF;
    private int stateTicks = 0;
    private int rocketsUsed = 0;
    private int rocketsNeeded = 2;
    private int savedSlot = -1;
    private boolean swapped = false;
    private int attackTicks = 0;
    private Player target = null;

    public ElytraMaceModule() {
        super(ElytraMaceAddon.ID + ":elytra-mace", "Elytra Mace", "Auto rocket elytra + track nearest player.");
    }

    @Override
    public void onEnable() {
        state = State.TAKEOFF;
        stateTicks = 0;
        rocketsUsed = 0;
        savedSlot = -1;
        target = null;
    }

    @Override
    public void onDisable() {
        restoreSlot();
        state = State.TAKEOFF;
        target = null;
    }

    @Override
    public void tick() {
        Minecraft mc = MC;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        LocalPlayer player = mc.player;

        if (target == null || !target.isAlive() || target.isRemoved()) {
            target = findTarget(mc);
        }

        switch (state) {
            case TAKEOFF -> {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.getItem() != Items.ELYTRA || findRocketSlot(player) < 0) return;

                if (target != null) {
                    double dist = Math.sqrt(target.distanceToSqr(player));
                    rocketsNeeded = dist <= 5 * 16 ? 2 : 3;
                } else {
                    rocketsNeeded = 2;
                }

                AutismRotationUtil.apply(player,
                    new AutismRotationUtil.Rotation(player.getYRot(), -90.0f), true);

                if (!player.isFallFlying()) {
                    mc.getConnection().send(new ServerboundPlayerCommandPacket(
                        player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
                    player.tryToStartFallFlying();
                }

                stateTicks++;
                if (player.isFallFlying()) {
                    state = State.ROCKETS;
                    stateTicks = 0;
                    rocketsUsed = 0;
                } else if (stateTicks > 20) {
                    stateTicks = 0;
                }
            }
            case ROCKETS -> {
                if (!player.isFallFlying()) {
                    state = State.TAKEOFF;
                    stateTicks = 0;
                    restoreSlot();
                    return;
                }

                if (rocketsUsed >= rocketsNeeded) {
                    state = State.TRACK;
                    stateTicks = 0;
                    restoreSlot();
                    return;
                }

                stateTicks++;
                if (stateTicks >= rocketDelay.get()) {
                    useRocket(mc, player);
                    rocketsUsed++;
                    stateTicks = 0;
                }
            }
            case TRACK -> {
                if (!player.isFallFlying()) {
                    state = State.TAKEOFF;
                    stateTicks = 0;
                    swapped = false;
                    return;
                }

                if (target != null && target.isAlive() && !target.isRemoved()) {
                    Vec3 eyes = player.getEyePosition();
                    Vec3 targetPos = target.getEyePosition();
                    AutismRotationUtil.Rotation rot = AutismRotationUtil.lookingAt(targetPos, eyes);
                    AutismRotationUtil.apply(player, rot, false);

                    double dist = player.distanceTo(target);
                    if (dist <= 10 && !swapped) {
                        swapChestToArmor(mc, player);
                        swapped = true;
                        state = State.SWAP_WAIT;
                        stateTicks = 0;
                    }
                }
            }
            case SWAP_WAIT -> {
                stateTicks++;
                if (stateTicks >= 2) {
                    state = State.ATTACK;
                    stateTicks = 0;
                    attackTicks = 0;
                }
            }
            case ATTACK -> {
                if (target == null || !target.isAlive() || target.isRemoved()) {
                    state = State.RE_ELYTRA;
                    stateTicks = 0;
                    return;
                }

                int maceSlot = findMaceSlot(player);
                if (maceSlot >= 0) {
                    int current = player.getInventory().getSelectedSlot();
                    if (current != maceSlot) {
                        if (savedSlot < 0) savedSlot = current;
                        AutismInventoryHelper.selectHotbarSlot(mc, maceSlot);
                    }
                }

                Vec3 eyes = player.getEyePosition();
                Vec3 targetPos = target.getEyePosition();
                AutismRotationUtil.Rotation rot = AutismRotationUtil.lookingAt(targetPos, eyes);
                AutismRotationUtil.apply(player, rot, false);

                double dist = player.distanceTo(target);
                if (dist <= 6) {
                    mc.getConnection().send(new ServerboundAttackPacket(target.getId()));
                    player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                }

                attackTicks++;
                if (attackTicks >= 5 || player.onGround()) {
                    state = State.RE_ELYTRA;
                    stateTicks = 0;
                }
            }
            case RE_ELYTRA -> {
                for (int slot = 0; slot < 36; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (stack.getItem() == Items.ELYTRA) {
                        AutismInventoryHelper.swapInventorySlots(mc, slot, 38);
                        break;
                    }
                }
                state = State.TAKEOFF;
                stateTicks = 0;
                swapped = false;
                attackTicks = 0;
            }
        }
    }

    private void useRocket(Minecraft mc, LocalPlayer player) {
        int slot = findRocketSlot(player);
        if (slot < 0) return;

        int current = player.getInventory().getSelectedSlot();
        if (current != slot) {
            if (savedSlot < 0) savedSlot = current;
            AutismInventoryHelper.selectHotbarSlot(mc, slot);
        }

        mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND,
            player.tickCount, player.getXRot(), player.getYRot()));
    }

    private Player findTarget(Minecraft mc) {
        if (mc.player == null || mc.level == null) return null;

        double maxDist = searchChunk.get() * 16.0;
        double maxDistSq = maxDist * maxDist;
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player && player != mc.player
                    && player.isAlive() && !player.isRemoved()) {
                double dist = player.distanceToSqr(mc.player);
                if (dist <= maxDistSq && dist < nearestDist) {
                    nearestDist = dist;
                    nearest = player;
                }
            }
        }
        return nearest;
    }

    private int findRocketSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() == Items.FIREWORK_ROCKET) {
                return slot;
            }
        }
        return -1;
    }

    private int findMaceSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() == Items.MACE) {
                return slot;
            }
        }
        return -1;
    }

    private void restoreSlot() {
        Minecraft mc = MC;
        if (mc == null || mc.player == null || savedSlot < 0) return;
        AutismInventoryHelper.selectHotbarSlot(mc, savedSlot);
        savedSlot = -1;
    }

    private void swapChestToArmor(Minecraft mc, LocalPlayer player) {
        for (int invSlot = 0; invSlot < 36; invSlot++) {
            ItemStack stack = player.getInventory().getItem(invSlot);
            if (stack.isEmpty()) continue;
            boolean isChestplate = stack.getItem() == Items.NETHERITE_CHESTPLATE
                    || stack.getItem() == Items.DIAMOND_CHESTPLATE
                    || stack.getItem() == Items.IRON_CHESTPLATE
                    || stack.getItem() == Items.GOLDEN_CHESTPLATE
                    || stack.getItem() == Items.LEATHER_CHESTPLATE
                    || stack.getItem() == Items.CHAINMAIL_CHESTPLATE;
            if (isChestplate) {
                AutismInventoryHelper.swapInventorySlots(mc, invSlot, 38);
                return;
            }
        }
    }
}
