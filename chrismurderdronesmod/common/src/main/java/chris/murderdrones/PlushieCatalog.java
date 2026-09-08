package chris.murderdrones;

import chris.murderdrones.block.ModBlocksCommon;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * Renamed from the original PlushieBrowserOpener — that name suggested keybind/event
 * registration, which is now loader-specific (see ModKeyBindings + each loader's own
 * key-mapping registration). This class just holds the shared category-building and
 * lookup logic that both the browser and skin-picker screens need.
 */
public final class PlushieCatalog {
    private PlushieCatalog() {}

    // Built once and cached — PlushieSkinPickerScreen looks entries up by character
    // key every time it opens, so this needs to be cheap to call repeatedly rather
    // than rebuilding the whole list each time.
    private static List<PlushieCategory> categories;

    public static List<PlushieCategory> getCategories() {
        if (categories == null) {
            categories = buildCategories();
        }
        return categories;
    }

    /** Looks up a category's CharacterEntry by its block/item registry path (e.g. "uzi", "jax"). */
    public static CharacterEntry findEntry(String character) {
        for (PlushieCategory category : getCategories()) {
            for (CharacterEntry entry : category.characters()) {
                if (pathOf(entry).equals(character)) return entry;
            }
        }
        return null;
    }

    /** Looks up which PlushieCategory a given character (by registry path) belongs to. */
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
                        ModBlocksCommon.itemFor(character.id())::get,
                        character.skinCount(),
                        character.skinNames()))
                .toList();
        return new PlushieCategory(categoryName, accentColor, entries);
    }
}
