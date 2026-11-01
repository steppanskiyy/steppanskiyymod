package com.ultramenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

public class UltraMenuClient implements ClientModInitializer {
    public static KeyBinding OPEN, ZOOM;
    public static boolean zooming = false;

    public static int combo = 0;
    private static int lastHitAge = 0;
    private static int lastHurt = 0;

    private static final ArrayDeque<Long> CLICKS = new ArrayDeque<>();
    private static boolean lastAtk = false;
    private static boolean autoSprintOn = false;
    private static boolean fbApplied = false;

    public static int cps() {
        return CLICKS.size();
    }

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("ultramenu", "main"));
        OPEN = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.ultramenu.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        ZOOM = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.ultramenu.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));

        ClientTickEvents.END_CLIENT_TICK.register(UltraMenuClient::tick);
        HudElementRegistry.addLast(Identifier.of("ultramenu", "hud"), Hud::render);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && entity instanceof LivingEntity) {
                combo++;
                lastHitAge = player.age;
            }
            return ActionResult.PASS;
        });
    }

    private static void tick(MinecraftClient mc) {
        while (OPEN.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new MenuScreen());
        }

        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) {
            zooming = false;
            return;
        }
        Config c = Config.I;
        boolean noScreen = mc.currentScreen == null;

        zooming = c.zoomEnabled && noScreen && ZOOM.isPressed();

        // Авто-спринт
        if (c.autoSprint && noScreen) {
            boolean want = mc.options.forwardKey.isPressed() && !p.isSneaking();
            if (want) {
                mc.options.sprintKey.setPressed(true);
                autoSprintOn = true;
            } else if (autoSprintOn) {
                mc.options.sprintKey.setPressed(false);
                autoSprintOn = false;
            }
        }

        // Фуллбрайт через клиентский ночной эффект
        if (c.fullbright) {
            StatusEffectInstance cur = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 400) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
            }
            fbApplied = true;
        } else if (fbApplied) {
            p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            fbApplied = false;
        }

        // CPS (по тикам, приблизительно)
        boolean atk = mc.options.attackKey.isPressed();
        long now = System.currentTimeMillis();
        if (atk && !lastAtk) CLICKS.addLast(now);
        lastAtk = atk;
        while (!CLICKS.isEmpty() && now - CLICKS.peekFirst() > 1000) CLICKS.pollFirst();

        // Комбо: сбрасывается когда нас ударили или 3 сек без хитов
        if (p.hurtTime > 0 && lastHurt == 0) combo = 0;
        lastHurt = p.hurtTime;
        if (combo > 0 && p.age - lastHitAge > 60) combo = 0;
    }
}
