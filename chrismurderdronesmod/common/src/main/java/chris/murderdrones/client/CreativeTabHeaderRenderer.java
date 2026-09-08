package chris.murderdrones.client;

import chris.murderdrones.Constants;
import chris.murderdrones.ModCreativeTabCommon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Aeronautics-style section banners — see the original CreativeTabHeaderRenderer's
 * doc comment for the full "why". Only the actual rendering (renderHeaders) and
 * click-blocking (isBlockedClick) logic lives here; each loader hooks its own
 * screen-render / mouse-click event and calls straight into these two methods.
 * NeoForge: ScreenEvent.Render.Post / ScreenEvent.MouseButtonPressed.Pre.
 * Fabric: ScreenEvents.afterRender(screen) / ScreenMouseEvents.allowMouseClick(screen).
 */
public final class CreativeTabHeaderRenderer {
    private CreativeTabHeaderRenderer() {}

    private enum Mode { TILE, STRETCH }

    private record HeaderStyle(ResourceLocation texture, int srcWidth, int srcHeight, Mode mode) {}

    // Vanilla creative-tab item grid geometry (CreativeModeInventoryScreen): the
    // first slot sits at (leftPos + 9, topPos + 18) and slots are spaced 18px
    // apart, 9 columns wide. These match vanilla's own layout constants.
    private static final int GRID_X = 9;
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 9;

    // Built lazily (on first render) rather than in a static initializer — this
    // class can get loaded before the registry entries are bound, and calling
    // .get() on an unbound Supplier at class-init time can throw. By the time the
    // creative screen is actually being rendered, registration has long finished.
    private static Map<Item, HeaderStyle> headers;

    private static Map<Item, HeaderStyle> headers() {
        if (headers == null) {
            Map<Item, HeaderStyle> map = new LinkedHashMap<>();
            map.put(ModCreativeTabCommon.HEADER_MURDER_DRONES.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/header_murder_drones.png"),
                            16, 16, Mode.STRETCH));
            map.put(ModCreativeTabCommon.HEADER_DIGITAL_CIRCUS.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/header_digital_circus.png"),
                            16, 16, Mode.STRETCH));
            map.put(ModCreativeTabCommon.HEADER_MISC.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/item/header_misc.png"),
                            16, 16, Mode.STRETCH));
            headers = map;
        }
        return headers;
    }

    private static Set<Item> nonGrabbable;

    private static Set<Item> nonGrabbable() {
        if (nonGrabbable == null) {
            Set<Item> set = new HashSet<>(headers().keySet());
            set.add(ModCreativeTabCommon.ROW_SPACER.get());
            nonGrabbable = set;
        }
        return nonGrabbable;
    }

    /** True if the click at (mouseX, mouseY) hits a header/row-spacer slot and should be swallowed. */
    public static boolean isBlockedClick(CreativeModeInventoryScreen screen, double mouseX, double mouseY) {
        int left = screen.leftPos;
        int top = screen.topPos;

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem() || !nonGrabbable().contains(slot.getItem().getItem())) continue;

            int x = left + slot.x;
            int y = top + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                return true;
            }
        }
        return false;
    }

    /** Paints a full-width banner over every header item's row. Call after vanilla has drawn the slots. */
    public static void renderHeaders(CreativeModeInventoryScreen screen, GuiGraphics graphics) {
        int left = screen.leftPos;
        int top = screen.topPos;

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) continue;
            HeaderStyle style = headers().get(slot.getItem().getItem());
            if (style == null) continue;

            int bannerX = left + GRID_X - 1;
            int bannerY = top + slot.y - 1;
            int bannerWidth = COLUMNS * SLOT_SIZE;
            int bannerHeight = SLOT_SIZE;

            if (style.mode() == Mode.TILE) {
                drawTiledBackground(graphics, style.texture(), bannerX, bannerY, bannerWidth, bannerHeight, style.srcWidth(), style.srcHeight());
            } else {
                drawStretchedBackground(graphics, style.texture(), bannerX, bannerY, bannerWidth, bannerHeight, style.srcWidth(), style.srcHeight());
            }
            graphics.fill(bannerX, bannerY + bannerHeight - 1, bannerX + bannerWidth, bannerY + bannerHeight, 0x66000000);
        }
    }

    private static void drawTiledBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int srcW, int srcH) {
        graphics.enableScissor(x, y, x + width, y + height);
        for (int ty = 0; ty < height; ty += srcH) {
            for (int tx = 0; tx < width; tx += srcW) {
                graphics.blit(texture, x + tx, y + ty, 0, 0, srcW, srcH, srcW, srcH);
            }
        }
        graphics.disableScissor();
    }

    private static void drawStretchedBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int srcW, int srcH) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale((float) width / srcW, (float) height / srcH, 1f);
        graphics.blit(texture, 0, 0, 0, 0, srcW, srcH, srcW, srcH);
        graphics.pose().popPose();
    }
}
