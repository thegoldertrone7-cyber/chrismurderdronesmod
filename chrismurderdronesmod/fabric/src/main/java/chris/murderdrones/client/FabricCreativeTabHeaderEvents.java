package chris.murderdrones.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;

/**
 * Fabric has no global "any screen rendered/clicked" event bus like NeoForge's
 * ScreenEvent — instead you register per-screen-instance callbacks, typically from
 * a BEFORE_INIT listener that checks the screen type. Registered once from
 * ChrisMurderDronesModFabricClient.
 */
public final class FabricCreativeTabHeaderEvents {
    private FabricCreativeTabHeaderEvents() {}

    public static void register() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreativeModeInventoryScreen creativeScreen)) return;

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
                    CreativeTabHeaderRenderer.renderHeaders(creativeScreen, graphics));

            ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) ->
                    !CreativeTabHeaderRenderer.isBlockedClick(creativeScreen, mouseX, mouseY));
        });
    }
}
