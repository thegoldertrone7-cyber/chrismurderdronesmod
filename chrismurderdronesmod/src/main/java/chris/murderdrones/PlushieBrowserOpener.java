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

    private static List<PlushieCategory> buildCategories() {
        return List.of(
                new PlushieCategory("Murder Drones", 0xFF7A1F2B, List.of(
                        new CharacterEntry("Uzi", ModBlocks.UZI_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("uzi"),
                                List.of("", "Prom", "PJ")),
                        new CharacterEntry("N", ModBlocks.N_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("n"),
                                List.of("", "Camper", "Dapper", "Manor", "PJ")),
                        new CharacterEntry("V", ModBlocks.V_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("v"),
                                List.of("", "Camper", "Prom", "Manor", "PJ")),
                        new CharacterEntry("J", ModBlocks.J_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("j"),
                                List.of("", "Manor", "Emotion Spilled Out", "Camper", "Prom", "PJ")),
                        new CharacterEntry("Cyn", ModBlocks.CYN_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("cyn"),
                                List.of("", "Disassembly Drone", "Freddy FazSuit")),
                        new CharacterEntry("Cynessa", ModBlocks.CYNESSA_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("cynessa"),
                                List.of("", "T-Rex")),
                        new CharacterEntry("Doll", ModBlocks.DOLL_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("doll"),
                                List.of("", "Prom", "Bandage", "Broken Eye")),
                        new CharacterEntry("Khan", ModBlocks.KHAN_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("khan"),
                                List.of("", "BALD", "Bowtie")),
                        new CharacterEntry("Lizzie", ModBlocks.LIZZIE_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("lizzie"),
                                List.of("", "Prom", "Camper")),
                        new CharacterEntry("Teacher", ModBlocks.TEACHER_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("teacher"),
                                List.of("", "Camper")),
                        new CharacterEntry("Tessa", ModBlocks.TESSA_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("tessa"),
                                List.of("", "Manor"))
                )),
                new PlushieCategory("Digital Circus", 0xFF1F4F7A, List.of(
                        new CharacterEntry("Pomni", ModBlocks.POMNI_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("pomni"),
                                List.of("", "Spudsy's", "Noir", "Running the Show", "Replacement Code", "Big Tops", "Big Tops Finale", "Possessed", "President", "Swimsuit", "Evil", "Anime", "Abstracted")),
                        new CharacterEntry("Jax", ModBlocks.JAX_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("jax"),
                                List.of("", "Spudsy's", "Noir", "Maid",
                                        "Running the Show", "Shadow", "Trans", "Peeled", "Evil", "Pacher's Paradise", "Spring", "Anime", "Abstracted", "Blank", "Casual 1", "Casual 2", "Casual Hoodie", "Casual Jacket", "Swimsuit", "Military", "Static", "Bow")),
                        new CharacterEntry("Ragatha", ModBlocks.RAGATHA_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("ragatha"),
                                List.of("", "Spudsy's", "Noir", "Running the Show",
                                        "Shadow", "Kitty Cat", "Full Bigtops", "Evil", "Bigtops", "Anime", "Beach", "Cowgirl", "Abstracted")),
                        new CharacterEntry("Gangle", ModBlocks.GANGLE_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("gangle"),
                                List.of("", "Spudsy's", "Noir", "Running the Show",
                                        "Happy Mask", "New Zeland Extremist", "Anime", "Evil", "Beach", "EP 4 Mask", "Bigtops", "Kitty Cat", "Abstracted")),
                        new CharacterEntry("Zooble", ModBlocks.ZOOBLE_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("zooble"),
                                List.of("", "Spudsy's", "Noir", "Running the Show", "Bodyguard", "Anime", "Evil", "YIKES!", "Abstracted")),
                        new CharacterEntry("Kinger", ModBlocks.KINGER_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("kinger"),
                                List.of("", "Noir", "Bucket Hat", "Running the Show", "Coach Dictatorer", "Bigtops", "Beach", "Anime", "Queenie", "Kitty Cat", "Abstracted")),
                        new CharacterEntry("Caine", ModBlocks.CAINE_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("caine"),
                                List.of("", "Running the Show", "Shadow", "Eniac",
                                        "Christmas", "Fisher")),
                        new CharacterEntry("NPC", ModBlocks.NPC_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("npc"),
                                List.of("", "Abel", "Red", "Orange",
                                        "Yellow", "Lime", "Green", "Cyan", "Light Blue", "Blue", "Magenta", "Purple", "Pink", "Disapearing Guy", "Ming")),
                        new CharacterEntry("Bubble", ModBlocks.BUBBLE_BLOCK_ITEM::get, PlushieBlockEntity.getSkinCount("bubble"),
                                List.of("", "Floor", "Chef", "Video Cam"))
                ))
        );
    }
}