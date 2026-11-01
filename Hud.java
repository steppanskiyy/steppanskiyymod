package com.ultramenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Hud {
    private static ItemStack[] icons;
    private static Item[] supplyItems;

    public static void render(DrawContext ctx, RenderTickCounter rt) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        Config c = Config.I;

        if (c.minimap) Minimap.render(ctx, mc);
        textBlock(ctx, mc, c);
        if (c.armorHud) armor(ctx, mc);
        if (c.supplyHud) supplies(ctx, mc);
        if (c.targetHud) target(ctx, mc);
    }

    private static String dir(Direction d) {
        return switch (d) {
            case NORTH -> "С (-Z)";
            case SOUTH -> "Ю (+Z)";
            case EAST -> "В (+X)";
            case WEST -> "З (-X)";
            default -> "";
        };
    }

    private static void textBlock(DrawContext ctx, MinecraftClient mc, Config c) {
        ClientPlayerEntity p = mc.player;
        List<String> lines = new ArrayList<>();

        if (c.infoHud) {
            if (c.showFps) lines.add("FPS: " + mc.getCurrentFps());
            if (c.showPing && mc.getNetworkHandler() != null) {
                PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
                if (e != null) lines.add("Пинг: " + e.getLatency() + " мс");
            }
            if (c.showCoords) lines.add("XYZ: " + p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ());
            if (c.showDir) lines.add("Смотрит: " + dir(p.getHorizontalFacing()));
            if (c.showBiome) {
                String b = mc.world.getBiome(p.getBlockPos()).getKey().map(k -> k.getValue().getPath()).orElse("?");
                lines.add("Биом: " + b);
            }
        }
        if (c.pvpCps) lines.add("CPS: " + UltraMenuClient.cps());
        if (c.pvpCombo) lines.add("Комбо: " + UltraMenuClient.combo);

        int y = 4;
        for (String s : lines) {
            ctx.drawText(mc.textRenderer, s, 4, y, 0xFFFFFFFF, true);
            y += 10;
        }
    }

    private static void armor(DrawContext ctx, MinecraftClient mc) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        TextRenderer tr = mc.textRenderer;
        int x = mc.getWindow().getScaledWidth() - 22;
        int y = mc.getWindow().getScaledHeight() / 2 - 36;
        for (int i = 0; i < 4; i++) {
            ItemStack st = mc.player.getEquippedStack(slots[i]);
            if (st.isEmpty()) continue;
            int yy = y + i * 18;
            ctx.drawItem(st, x, yy);
            ctx.drawStackOverlay(tr, st, x, yy);
            if (st.isDamageable()) {
                String t = String.valueOf(st.getMaxDamage() - st.getDamage());
                ctx.drawText(tr, t, x - tr.getWidth(t) - 2, yy + 5, 0xFFFFFFFF, true);
            }
        }
    }

    private static void supplies(DrawContext ctx, MinecraftClient mc) {
        if (icons == null) {
            supplyItems = new Item[]{Items.TOTEM_OF_UNDYING, Items.ENDER_PEARL, Items.GOLDEN_APPLE, Items.END_CRYSTAL};
            icons = new ItemStack[supplyItems.length];
            for (int i = 0; i < icons.length; i++) icons[i] = new ItemStack(supplyItems[i]);
        }
        PlayerInventory inv = mc.player.getInventory();
        int[] counts = new int[supplyItems.length];
        for (int s = 0; s < inv.size(); s++) {
            ItemStack st = inv.getStack(s);
            if (st.isEmpty()) continue;
            for (int k = 0; k < supplyItems.length; k++) {
                if (st.isOf(supplyItems[k])) counts[k] += st.getCount();
            }
        }
        int x = 4;
        int y = mc.getWindow().getScaledHeight() / 2 - 36;
        for (int k = 0; k < icons.length; k++) {
            int yy = y + k * 18;
            ctx.drawItem(icons[k], x, yy);
            ctx.drawText(mc.textRenderer, "x" + counts[k], x + 20, yy + 5, counts[k] == 0 ? 0xFFFF5555 : 0xFFFFFFFF, true);
        }
    }

    private static void target(DrawContext ctx, MinecraftClient mc) {
        Entity t = mc.targetedEntity;
        if (!(t instanceof LivingEntity le) || le == mc.player) return;
        float hp = le.getHealth(), max = Math.max(1f, le.getMaxHealth());
        int x = mc.getWindow().getScaledWidth() / 2 + 14;
        int y = mc.getWindow().getScaledHeight() / 2 + 12;
        String s = le.getName().getString() + "  " + String.format(Locale.ROOT, "%.1f/%.1f", hp, max);
        ctx.drawText(mc.textRenderer, s, x, y, 0xFFFFFFFF, true);
        ctx.fill(x, y + 11, x + 70, y + 14, 0xFF330000);
        ctx.fill(x, y + 11, x + (int) (70 * Math.min(1f, hp / max)), y + 14, 0xFFFF3030);
    }
}
