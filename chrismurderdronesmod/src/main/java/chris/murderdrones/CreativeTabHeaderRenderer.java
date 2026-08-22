package chris.murderdrones;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// ── Aeronautics-style section banners ──────────────────────────────────────
// Create Simulated's "Aeronautics"/"Offroad" tabs draw a full-width coloured
// banner across a whole row instead of a single inventory-slot icon. Vanilla
// has no built-in concept of that, so we fake it: the header items registered
// in ChrisMurderDronesMod still occupy one real slot each (that's what lets
// them sit at a specific point in display order), but on the client we listen
// for the creative screen being rendered, find whichever slot currently holds
// one of those header items, and paint a full-width banner texture over that
// entire row. No mixins required — ScreenEvent.Render.Post fires after
// vanilla has drawn the slot contents, so the banner simply covers the single
// icon that would otherwise show there. A second listener below
// (onMouseButtonPressedPre) makes sure that covered-up icon — and the blank
// ROW_SPACER padding around it — can't actually be clicked or dragged out.
@EventBusSubscriber(modid = ChrisMurderDronesMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class CreativeTabHeaderRenderer {

    private enum Mode { TILE, STRETCH }

    private record HeaderStyle(ResourceLocation texture, int srcWidth, int srcHeight, Mode mode) {}

    // Item -> banner texture. Add an entry here whenever a new header item is
    // added to ChrisMurderDronesMod's creative tab.
    //
    // Two ways to supply the image:
    //  - TILE:    a small texture (e.g. 16x9) is repeated edge-to-edge to fill
    //             the row. Good for seamless patterns/scenery.
    //  - STRETCH: a single texture (any size — e.g. a normal 16x16 item icon)
    //             is scaled up to fill the entire 162x18 row as one image.
    //             Good for reusing an existing icon as the banner art without
    //             needing a separate seamless-tile asset.
    //
    // Built lazily (on first render) rather than in a static initializer:
    // this class can get loaded before the DeferredItem registry entries are
    // bound, and calling .get() on an unbound DeferredHolder at class-init
    // time throws "Trying to access unbound value". By the time the creative
    // screen is actually being rendered, registration has long since finished.
    private static Map<Item, HeaderStyle> headers;

    private static Map<Item, HeaderStyle> headers() {
        if (headers == null) {
            Map<Item, HeaderStyle> map = new LinkedHashMap<>();
            map.put(ChrisMurderDronesMod.HEADER_MURDER_DRONES.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "textures/item/header_murder_drones.png"),
                            16, 16, Mode.STRETCH));
            // Stretches the header item's own 16x16 icon texture across the
            // whole row instead of using a separate seamless-tile asset.
            map.put(ChrisMurderDronesMod.HEADER_DIGITAL_CIRCUS.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "textures/item/header_digital_circus.png"),
                            16, 16, Mode.STRETCH));
            map.put(ChrisMurderDronesMod.HEADER_MISC.get(),
                    new HeaderStyle(ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "textures/item/header_misc.png"),
                            16, 16, Mode.STRETCH));
            headers = map;
        }
        return headers;
    }

    // Vanilla creative-tab item grid geometry (CreativeModeInventoryScreen):
    // the first slot sits at (leftPos + 9, topPos + 18) and slots are spaced
    // 18px apart, 9 columns wide. These match vanilla's own layout constants.
    private static final int GRID_X = 9;
    private static final int SLOT_SIZE = 18;
    private static final int COLUMNS = 9;

    // Items that should be visible-but-untouchable in the creative tab: the
    // three headers plus ROW_SPACER. Lazily built for the same "unbound
    // DeferredHolder" reason as headers() above.
    private static Set<Item> nonGrabbable;

    private static Set<Item> nonGrabbable() {
        if (nonGrabbable == null) {
            Set<Item> set = new HashSet<>(headers().keySet());
            set.add(ChrisMurderDronesMod.ROW_SPACER.get());
            nonGrabbable = set;
        }
        return nonGrabbable;
    }

    /**
     * Stops headers/row-spacers being clicked, shift-clicked, dragged, or
     * middle-click-picked out of the creative tab. Slot.x/y are already
     * screen-local (same coordinates {@link #onScreenRenderPost} uses for the
     * banner), so a slot is "hovered" whenever the mouse falls within its
     * 16x16 icon box at (left + slot.x, top + slot.y).
     */
    @SubscribeEvent
    public static void onMouseButtonPressedPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen)) return;

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem() || !nonGrabbable().contains(slot.getItem().getItem())) continue;

            int x = left + slot.x;
            int y = top + slot.y;
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();

        for (Slot slot : screen.getMenu().slots) {
            if (!slot.hasItem()) continue;
            HeaderStyle style = headers().get(slot.getItem().getItem());
            if (style == null) continue;

            int bannerX = left + GRID_X - 1;
            int bannerY = top + slot.y - 1;
            int bannerWidth = COLUMNS * SLOT_SIZE;
            int bannerHeight = SLOT_SIZE;

            // The header item's own row is guaranteed empty (ChrisMurderDronesMod
            // pads it with ROW_SPACER on both sides), so the banner is free to
            // cover the whole row — either tiled or stretched to fill it. Same
            // visual language as Create Simulated's "Aeronautics"/"Offroad"
            // section headers, minus the text label.
            if (style.mode() == Mode.TILE) {
                drawTiledBackground(graphics, style.texture(), bannerX, bannerY, bannerWidth, bannerHeight, style.srcWidth(), style.srcHeight());
            } else {
                drawStretchedBackground(graphics, style.texture(), bannerX, bannerY, bannerWidth, bannerHeight, style.srcWidth(), style.srcHeight());
            }
            graphics.fill(bannerX, bannerY + bannerHeight - 1, bannerX + bannerWidth, bannerY + bannerHeight, 0x66000000);
        }
    }

    /** Repeats (not stretches) a texture across the given area, clipped exactly to it. */
    private static void drawTiledBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int srcW, int srcH) {
        graphics.enableScissor(x, y, x + width, y + height);
        for (int ty = 0; ty < height; ty += srcH) {
            for (int tx = 0; tx < width; tx += srcW) {
                graphics.blit(texture, x + tx, y + ty, 0, 0, srcW, srcH, srcW, srcH);
            }
        }
        graphics.disableScissor();
    }

    /** Scales a single texture up (or down) to exactly fill the given area — full 9-wide row from one image. */
    private static void drawStretchedBackground(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int srcW, int srcH) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale((float) width / srcW, (float) height / srcH, 1f);
        graphics.blit(texture, 0, 0, 0, 0, srcW, srcH, srcW, srcH);
        graphics.pose().popPose();
    }
}