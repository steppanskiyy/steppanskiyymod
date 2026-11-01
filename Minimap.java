package com.ultramenu;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;

public final class Minimap {
    private static int[] colors = new int[0];
    private static int[] heights = new int[0];
    private static int n = 0, ox, oz, lastUpdate = -999;
    private static ClientWorld lastWorld;

    private static void rebuild(MinecraftClient mc, int size) {
        ClientWorld w = mc.world;
        ClientPlayerEntity p = mc.player;
        colors = new int[size * size];
        heights = new int[size * size];
        n = size;
        int r = n / 2;
        ox = p.getBlockX();
        oz = p.getBlockZ();
        int py = p.getBlockY();
        boolean ceil = w.getDimension().hasCeiling();
        int bottom = w.getBottomY();
        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                int wx = ox - r + i, wz = oz - r + j, idx = j * n + i;
                if (w.getChunk(wx >> 4, wz >> 4, ChunkStatus.FULL, false) == null) {
                    colors[idx] = 0xFF0C0C0C;
                    heights[idx] = py;
                    continue;
                }
                int y = ceil ? py + 2 : w.getTopY(Heightmap.Type.WORLD_SURFACE, wx, wz) - 1;
                while (y > bottom && w.getBlockState(m.set(wx, y, wz)).isAir()) y--;
                BlockState s = w.getBlockState(m.set(wx, y, wz));
                int rgb = s.isAir() ? 0 : s.getMapColor(w, m).color;
                colors[idx] = 0xFF000000 | rgb;
                heights[idx] = y;
            }
        }
        // Рельеф: светлее/темнее относительно соседа
        for (int j = 1; j < n; j++) {
            for (int i = 1; i < n; i++) {
                int idx = j * n + i;
                int d = heights[idx] - heights[idx - n - 1];
                d = Math.max(-3, Math.min(3, d));
                if (d == 0) continue;
                float f = 1f + d * 0.07f;
                int c = colors[idx];
                int rr = Math.min(255, (int) (((c >> 16) & 255) * f));
                int gg = Math.min(255, (int) (((c >> 8) & 255) * f));
                int bb = Math.min(255, (int) ((c & 255) * f));
                colors[idx] = 0xFF000000 | (rr << 16) | (gg << 8) | bb;
            }
        }
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        Config c = Config.I;
        ClientPlayerEntity p = mc.player;
        int cells = Math.max(16, c.mmRange * 2);
        if (n != cells || mc.world != lastWorld || p.age - lastUpdate >= 4 || p.age < lastUpdate) {
            rebuild(mc, cells);
            lastWorld = mc.world;
            lastUpdate = p.age;
        }

        int size = c.mmSize;
        int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        int left = (c.mmCorner % 2 == 0) ? 6 : sw - size - 6;
        int top = (c.mmCorner < 2) ? 6 : sh - size - 40;

        ctx.fill(left - 1, top - 1, left + size + 1, top + size + 1, 0xFF000000);

        for (int j = 0; j < n; j++) {
            int y0 = top + j * size / n, y1 = top + (j + 1) * size / n;
            if (y1 == y0) continue;
            int i = 0;
            while (i < n) {
                int col = colors[j * n + i];
                int k = i + 1;
                while (k < n && colors[j * n + k] == col) k++;
                ctx.fill(left + i * size / n, y0, left + k * size / n, y1, col);
                i = k;
            }
        }

        double r = n / 2.0;
        double baseX = ox - r, baseZ = oz - r;

        if (c.mmEntities) {
            for (Entity e : mc.world.getEntities()) {
                if (e == p || !(e instanceof LivingEntity) || e instanceof ArmorStandEntity) continue;
                double fx = (e.getX() - baseX) / n, fz = (e.getZ() - baseZ) / n;
                if (fx < 0 || fx >= 1 || fz < 0 || fz >= 1) continue;
                int ex = left + (int) (fx * size), ez = top + (int) (fz * size);
                int col = e instanceof PlayerEntity ? 0xFFFFFF55 : (e instanceof Monster ? 0xFFFF4040 : 0xFF55FF55);
                ctx.fill(ex - 1, ez - 1, ex + 2, ez + 2, col);
            }
        }

        double fx = (p.getX() - baseX) / n, fz = (p.getZ() - baseZ) / n;
        int px = left + (int) (fx * size), pz = top + (int) (fz * size);
        double yaw = Math.toRadians(p.getYaw());
        double dx = -Math.sin(yaw), dz = Math.cos(yaw);
        for (int t = 3; t <= 7; t += 2) {
            int ax = px + (int) Math.round(dx * t), az = pz + (int) Math.round(dz * t);
            ctx.fill(ax, az, ax + 1, az + 1, 0xFFFFFF00);
        }
        ctx.fill(px - 1, pz - 1, px + 2, pz + 2, 0xFFFFFFFF);

        ctx.drawText(mc.textRenderer, "N", left + size / 2 - 2, top + 2, 0xFFFF5555, true);

        if (c.mmCoords) {
            String s = p.getBlockX() + " " + p.getBlockY() + " " + p.getBlockZ();
            int ty = (c.mmCorner < 2) ? top + size + 3 : top - 11;
            ctx.drawText(mc.textRenderer, s, left + (size - mc.textRenderer.getWidth(s)) / 2, ty, 0xFFFFFFFF, true);
        }
    }
}
