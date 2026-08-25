package chris.murderdrones;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ChrisMurderDronesMod.MODID, value = Dist.CLIENT)
public class PlushieBrowserOpener {

    public static final KeyMapping OPEN_BROWSER_KEY = new KeyMapping(
            "key.chrismurderdronesmod.open_browser",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            "key.categories.chrismurderdronesmod");

    // Built once and cached — PlushieSkinPickerOpener/Screen look entries up by
    // character key every time the skin picker opens, so this needs to be cheap
    // to call repeatedly rather than rebuilding the whole list each time.
    private static List<PlushieCategory> categories;

    private static List<PlushieCategory> getCategories() {
        if (categories == null) {
            categories = buildCategories();
        }
        return categories;
    }

    /** Public entry point for other classes (e.g. {@link CreativeCatalogButtonInjector}) that need the same cached category list. */
    public static List<PlushieCategory> getCategoriesForButton() {
        return getCategories();
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_BROWSER_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_BROWSER_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new PlushieBrowserScreen(getCategories()));
            }
        }
    }

    /** Looks up a category's {@link CharacterEntry} by its block/item registry path (e.g. "uzi", "jax"). */
    public static CharacterEntry findEntry(String character) {
        for (PlushieCategory category : getCategories()) {
            for (CharacterEntry entry : category.characters()) {
                if (pathOf(entry).equals(character)) return entry;
            }
        }
        return null;
    }

    /** Looks up which {@link PlushieCategory} a given character (by registry path) belongs to. */
    public static PlushieCategory findCategoryFor(String character) {
        for (PlushieCategory category : getCategories()) {
            for (CharacterEntry entry : category.characters()) {
                if (pathOf(entry).equals(character)) return category;
            }
        }
        return null;
    }

    private static String pathOf(CharacterEntry entry) {
        return BuiltInRegistries.ITEM.getKey(entry.item().get()).getPath();
    }

    // Category accent colors — the character list itself now comes straight from
    // PlushieCharacters, so adding a plushie to the catalog is just adding it there.
    private static final int MURDER_DRONES_COLOR = 0xFF7A1F2B;
    private static final int DIGITAL_CIRCUS_COLOR = 0xFF1F4F7A;

    private static List<PlushieCategory> buildCategories() {
        return List.of(
                buildCategory(PlushieCharacters.MURDER_DRONES, MURDER_DRONES_COLOR),
                buildCategory(PlushieCharacters.DIGITAL_CIRCUS, DIGITAL_CIRCUS_COLOR)
        );
    }

    private static PlushieCategory buildCategory(String categoryName, int accentColor) {
        List<CharacterEntry> entries = PlushieCharacters.inCategory(categoryName).stream()
                .map(character -> new CharacterEntry(
                        character.displayName(),
                        ModBlocks.itemFor(character.id())::get,
                        character.skinCount(),
                        character.skinNames()))
                .toList();
        return new PlushieCategory(categoryName, accentColor, entries);
    }
}