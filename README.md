# UltraMenu (Fabric, Minecraft Java 1.21.11)

Меню открывается на **Правый Shift** (меняется в Управлении). Зум — **C**.

## Сборка
Нужна Java 21.
1. Если есть gradle wrapper: `./gradlew build`.
2. Если нет — скачай шаблон на https://fabricmc.net/develop/template (1.21.11), скопируй из этого проекта
   папку `src/` и файл `gradle.properties` поверх шаблона. Потом `./gradlew build`.
3. Готовый мод: `build/libs/ultramenu-1.0.0.jar` (не *-sources). Положи в `mods` вместе с Fabric API.

Если Gradle ругается на версию — открой https://fabricmc.net/develop и подставь свежие значения
в `gradle.properties`.
