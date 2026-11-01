package com.ultramenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class MenuScreen extends Screen {
    private static final String[] TABS = {"Вид", "Руки", "Ресурспаки", "Миникарта", "Облегчалки", "PvP"};
    private static int lastTab = 0;

    private int tab = lastTab;
    private int packPage = 0;
    private int slot, left, top, colW;
    private boolean noPacks = false;

    public MenuScreen() {
        super(Text.literal("UltraMenu"));
    }

    @Override
    protected void init() {
        build();
    }

    // ---------- helpers ----------
    private int cx() { return left + (slot % 2) * (colW + 6); }
    private int cy() { return top + (slot / 2) * 24; }
    private int fullW() { return colW * 2 + 6; }

    private Text onOff(String name, boolean on) {
        return Text.literal(name + ": ").append(Text.literal(on ? "ВКЛ" : "ВЫКЛ").formatted(on ? Formatting.GREEN : Formatting.RED));
    }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        ButtonWidget b = ButtonWidget.builder(onOff(name, get.getAsBoolean()), btn -> {
            boolean v = !get.getAsBoolean();
            set.accept(v);
            btn.setMessage(onOff(name, v));
        }).dimensions(cx(), cy(), colW, 20).build();
        addDrawableChild(b);
        slot++;
    }

    private void slider(String name, double min, double max, double cur, String fmt, DoubleConsumer set) {
        addDrawableChild(new Slider(cx(), cy(), colW, name, min, max, cur, fmt, set));
        slot++;
    }

    private void wide(Text text, Runnable action) {
        if (slot % 2 == 1) slot++;
        addDrawableChild(ButtonWidget.builder(text, b -> action.run()).dimensions(left, cy(), fullW(), 20).build());
        slot += 2;
    }

    private void cycle(String name, String[] values, java.util.function.IntSupplier get, java.util.function.IntConsumer set) {
        ButtonWidget b = ButtonWidget.builder(Text.literal(name + ": " + values[get.getAsInt()]), btn -> {
            int v = (get.getAsInt() + 1) % values.length;
            set.accept(v);
            btn.setMessage(Text.literal(name + ": " + values[v]));
        }).dimensions(cx(), cy(), colW, 20).build();
        addDrawableChild(b);
        slot++;
    }

    // ---------- build ----------
    private void build() {
        clearChildren();
        slot = 0;
        left = 136;
        top = 36;
        colW = Math.max(100, Math.min(160, (width - left - 16 - 6) / 2));
        noPacks = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        Config c = Config.I;

        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(Text.literal((i == tab ? "> " : "") + TABS[i]), b -> {
                tab = idx;
                lastTab = idx;
                packPage = 0;
                build();
            }).dimensions(10, top + i * 24, 116, 20).build());
        }

        switch (tab) {
            case 0 -> {
                String[] names = {"Авто", "4:3", "5:4", "16:9", "21:9", "1:1"};
                float[] vals = {0f, 4f / 3f, 5f / 4f, 16f / 9f, 21f / 9f, 1f};
                int pw = (fullW() - 8) / 3;
                for (int k = 0; k < 6; k++) {
                    final float v = vals[k];
                    boolean sel = Math.abs(c.aspect - v) < 0.01f;
                    addDrawableChild(ButtonWidget.builder(Text.literal((sel ? "* " : "") + names[k]), b -> {
                        Config.I.aspect = v;
                        build();
                    }).dimensions(left + (k % 3) * (pw + 4), top + (k / 3) * 24, pw, 20).build());
                }
                slot = 4;
                double curAspect = c.aspect > 0 ? c.aspect
                        : (double) mc.getWindow().getFramebufferWidth() / Math.max(1, mc.getWindow().getFramebufferHeight());
                slider("Аспект (своё)", 0.5, 3.0, curAspect, "%.2f", v -> Config.I.aspect = (float) v);
                slider("FOV", 30, 110, mc.options.getFov().getValue(), "%.0f", v -> mc.options.getFov().setValue((int) Math.round(v)));
                slider("Дальность", 2, 32, mc.options.getViewDistance().getValue(), "%.0f", v -> mc.options.getViewDistance().setValue((int) Math.round(v)));
                toggle("Фуллбрайт", () -> Config.I.fullbright, v -> Config.I.fullbright = v);
                toggle("Без тряски камеры", () -> Config.I.noBob, v -> {
                    Config.I.noBob = v;
                    mc.options.getBobView().setValue(!v);
                });
                toggle("Зум (клавиша C)", () -> Config.I.zoomEnabled, v -> Config.I.zoomEnabled = v);
                slider("Сила зума", 1.5, 10, c.zoomLevel, "x%.1f", v -> Config.I.zoomLevel = (float) v);
            }
            case 1 -> {
                toggle("Свои настройки рук", () -> Config.I.handsEnabled, v -> Config.I.handsEnabled = v);
                slot++;
                slider("Сдвиг X", -1, 1, c.handX, "%.2f", v -> Config.I.handX = (float) v);
                slider("Сдвиг Y", -1, 1, c.handY, "%.2f", v -> Config.I.handY = (float) v);
                slider("Сдвиг Z", -1, 1, c.handZ, "%.2f", v -> Config.I.handZ = (float) v);
                slider("Наклон X", -90, 90, c.handRotX, "%.0f°", v -> Config.I.handRotX = (float) v);
                slider("Поворот Y", -90, 90, c.handRotY, "%.0f°", v -> Config.I.handRotY = (float) v);
                slider("Крен Z", -90, 90, c.handRotZ, "%.0f°", v -> Config.I.handRotZ = (float) v);
                slider("Размер", 0.3, 1.5, c.handScale, "%.2f", v -> Config.I.handScale = (float) v);
                slot++;
                wide(Text.literal("Сбросить руки"), () -> {
                    Config.I.handX = Config.I.handY = Config.I.handZ = 0;
                    Config.I.handRotX = Config.I.handRotY = Config.I.handRotZ = 0;
                    Config.I.handScale = 1;
                    build();
                });
            }
            case 2 -> buildPacks(mc);
            case 3 -> {
                toggle("Миникарта", () -> Config.I.minimap, v -> Config.I.minimap = v);
                toggle("Мобы и игроки", () -> Config.I.mmEntities, v -> Config.I.mmEntities = v);
                toggle("Координаты", () -> Config.I.mmCoords, v -> Config.I.mmCoords = v);
                cycle("Угол", new String[]{"Лев-верх", "Прав-верх", "Лев-низ", "Прав-низ"}, () -> Config.I.mmCorner, v -> Config.I.mmCorner = v);
                slider("Размер", 64, 180, c.mmSize, "%.0f px", v -> Config.I.mmSize = (int) Math.round(v));
                slider("Радиус", 16, 64, c.mmRange, "%.0f блоков", v -> Config.I.mmRange = (int) Math.round(v));
            }
            case 4 -> {
                toggle("Авто-спринт", () -> Config.I.autoSprint, v -> Config.I.autoSprint = v);
                toggle("Автопрыжок", () -> mc.options.getAutoJump().getValue(), v -> mc.options.getAutoJump().setValue(v));
                toggle("Инфо-панель", () -> Config.I.infoHud, v -> Config.I.infoHud = v);
                toggle("FPS", () -> Config.I.showFps, v -> Config.I.showFps = v);
                toggle("Пинг", () -> Config.I.showPing, v -> Config.I.showPing = v);
                toggle("Координаты", () -> Config.I.showCoords, v -> Config.I.showCoords = v);
                toggle("Направление", () -> Config.I.showDir, v -> Config.I.showDir = v);
                toggle("Биом", () -> Config.I.showBiome, v -> Config.I.showBiome = v);
            }
            case 5 -> {
                toggle("CPS", () -> Config.I.pvpCps, v -> Config.I.pvpCps = v);
                toggle("Счётчик комбо", () -> Config.I.pvpCombo, v -> Config.I.pvpCombo = v);
                toggle("HUD брони", () -> Config.I.armorHud, v -> Config.I.armorHud = v);
                toggle("Запасы (тотемы...)", () -> Config.I.supplyHud, v -> Config.I.supplyHud = v);
                toggle("Инфо о цели", () -> Config.I.targetHud, v -> Config.I.targetHud = v);
                toggle("Зум (клавиша C)", () -> Config.I.zoomEnabled, v -> Config.I.zoomEnabled = v);
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), b -> close())
                .dimensions(width / 2 - 50, height - 28, 100, 20).build());
    }

    private void buildPacks(MinecraftClient mc) {
        ResourcePackManager rpm = mc.getResourcePackManager();
        List<ResourcePackProfile> list = new ArrayList<>();
        for (ResourcePackProfile p : rpm.getProfiles()) {
            if (!p.isRequired()) list.add(p);
        }
        if (list.isEmpty()) {
            noPacks = true;
            return;
        }
        int perPage = 6;
        int pages = (list.size() + perPage - 1) / perPage;
        packPage = Math.max(0, Math.min(packPage, pages - 1));

        for (int k = packPage * perPage; k < Math.min(list.size(), (packPage + 1) * perPage); k++) {
            ResourcePackProfile p = list.get(k);
            boolean on = rpm.getEnabledProfiles().contains(p);
            String name = p.getDisplayName().getString();
            if (name.length() > 34) name = name.substring(0, 34) + "…";
            Text label = Text.literal(on ? "[ВКЛ] " : "[ВЫКЛ] ").formatted(on ? Formatting.GREEN : Formatting.GRAY)
                    .append(Text.literal(name).formatted(Formatting.WHITE));
            wide(label, () -> {
                if (on) rpm.disable(p.getId());
                else rpm.enable(p.getId());
                mc.options.refreshResourcePacks(rpm);
                build();
            });
        }
        if (pages > 1) {
            int y = top + 6 * 24;
            addDrawableChild(ButtonWidget.builder(Text.literal("< Назад"), b -> {
                packPage = (packPage - 1 + pages) % pages;
                build();
            }).dimensions(left, y, colW, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Вперёд >"), b -> {
                packPage = (packPage + 1) % pages;
                build();
            }).dimensions(left + colW + 6, y, colW, 20).build());
        }
    }

    // ---------- render / close ----------
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("UltraMenu"), width / 2, 12, 0xFFFFFFFF);
        if (noPacks) {
            ctx.drawText(textRenderer, "Ресурспаков нет. Закинь их в папку resourcepacks.", left, top + 4, 0xFFAAAAAA, true);
        }
    }

    @Override
    public void close() {
        Config.save();
        if (client != null) client.options.write();
        super.close();
    }

    // ---------- slider ----------
    private static class Slider extends SliderWidget {
        private final String label, fmt;
        private final double min, max;
        private final DoubleConsumer set;

        Slider(int x, int y, int w, String label, double min, double max, double cur, String fmt, DoubleConsumer set) {
            super(x, y, w, 20, Text.empty(), Math.max(0, Math.min(1, (cur - min) / (max - min))));
            this.label = label;
            this.fmt = fmt;
            this.min = min;
            this.max = max;
            this.set = set;
            updateMessage();
        }

        private double real() {
            return min + (max - min) * this.value;
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            setMessage(Text.literal(label + ": " + String.format(Locale.ROOT, fmt, real())));
        }

        @Override
        protected void applyValue() {
            set.accept(real());
        }
    }
}
