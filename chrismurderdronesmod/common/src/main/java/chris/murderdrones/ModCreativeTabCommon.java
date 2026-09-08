package chris.murderdrones;

import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.RowSpacerItem;
import chris.murderdrones.platform.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * Single creative tab holding everything the mod adds, grouped into Murder Drones /
 * Digital Circus / Misc sections by a header item at the start of its own row (see
 * addHeader/padToRowEnd — logic copied unchanged from the original ChrisMurderDronesMod.java).
 * <p>
 * The per-character item list itself is now a loop over PlushieCharacters.ALL grouped
 * by category, instead of the original's 19 hand-written addItem(...) calls.
 * <p>
 * NOT ported: CreativeTabHeaderRenderer's full-width banner painted over each header's
 * row. That renderer hooks NeoForge's ScreenEvent, which has no equivalent at the
 * Fabric API level (would need mixins into the creative screen to replicate there) —
 * left as a follow-up. Headers here just render as blank RowSpacerItem icons for now.
 */
public final class ModCreativeTabCommon {
    private ModCreativeTabCommon() {}

    private static final int GRID_COLUMNS = 9;

    public static final Supplier<Item> HEADER_MURDER_DRONES = Services.REGISTRY.registerItem("header_murder_drones",
            () -> new RowSpacerItem(new Item.Properties()));
    public static final Supplier<Item> HEADER_DIGITAL_CIRCUS = Services.REGISTRY.registerItem("header_digital_circus",
            () -> new RowSpacerItem(new Item.Properties()));
    public static final Supplier<Item> HEADER_MISC = Services.REGISTRY.registerItem("header_misc",
            () -> new RowSpacerItem(new Item.Properties()));
    public static final Supplier<Item> ROW_SPACER = Services.REGISTRY.registerItem("row_spacer",
            () -> new RowSpacerItem(new Item.Properties()));

    private static Supplier<Item> registerMusicDisc(String name) {
        ResourceKey<JukeboxSong> songKey = ResourceKey.create(Registries.JUKEBOX_SONG,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
        return Services.REGISTRY.registerItem(name, () -> new Item(new Item.Properties()
                .stacksTo(1)
                .jukeboxPlayable(songKey)));
    }

    public static final Supplier<Item> FTIGE_MUSIC_DISC = registerMusicDisc("ftige_music_disc");
    public static final Supplier<Item> BM_MUSIC_DISC = registerMusicDisc("bm_music_disc");
    public static final Supplier<Item> RTS_MUSIC_DISC = registerMusicDisc("rts_music_disc");
    public static final Supplier<Item> FOREVER_MUSIC_DISC = registerMusicDisc("forever_music_disc");

    public static final Supplier<CreativeModeTab> MAIN_TAB = Services.REGISTRY.registerCreativeTab("main_tab",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .title(Component.translatable("itemGroup.chrismurderdronesmod.main"))
                    // No .withTabsBefore(CreativeModeTabs.COMBAT) here — that's a
                    // NeoForge-only patch method on CreativeModeTab.Builder (not
                    // vanilla), so it can't live in common code. Tab defaults to
                    // appearing after the last vanilla tab on both loaders instead
                    // of specifically before Combat; purely cosmetic ordering.
                    .icon(() -> ModBlocksCommon.SCREWDRIVER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        int[] count = {0};

                        addHeader(output, count, HEADER_MURDER_DRONES.get());
                        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
                            if (character.category().equals(PlushieCharacters.MURDER_DRONES)) {
                                addItem(output, count, ModBlocksCommon.itemFor(character.id()).get());
                            }
                        }

                        addHeader(output, count, HEADER_DIGITAL_CIRCUS.get());
                        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
                            if (character.category().equals(PlushieCharacters.DIGITAL_CIRCUS)) {
                                addItem(output, count, ModBlocksCommon.itemFor(character.id()).get());
                            }
                        }

                        addHeader(output, count, HEADER_MISC.get());
                        addItem(output, count, ModBlocksCommon.SCREWDRIVER.get());
                        addItem(output, count, FTIGE_MUSIC_DISC.get());
                        addItem(output, count, BM_MUSIC_DISC.get());
                        addItem(output, count, RTS_MUSIC_DISC.get());
                        addItem(output, count, FOREVER_MUSIC_DISC.get());
                        addItem(output, count, ModBlocksCommon.PLUSHIE_BASE_ITEM.get());
                    }).build());

    /** Pads to the end of the current row with ROW_SPACER — see original's doc comment on why each pad needs distinct CUSTOM_MODEL_DATA. */
    private static void padToRowEnd(CreativeModeTab.Output output, int[] count) {
        while (count[0] % GRID_COLUMNS != 0) {
            ItemStack spacer = new ItemStack(ROW_SPACER.get());
            spacer.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(count[0]));
            output.accept(spacer);
            count[0]++;
        }
    }

    private static void addHeader(CreativeModeTab.Output output, int[] count, Item header) {
        padToRowEnd(output, count);
        output.accept(header);
        count[0]++;
        padToRowEnd(output, count);
    }

    private static void addItem(CreativeModeTab.Output output, int[] count, ItemLike item) {
        output.accept(item);
        count[0]++;
    }

    /** Called once from each loader's mod entry point (via ChrisMurderDronesMod.init) to force this class's static registration to run. */
    public static void init() {
        // no-op — the static field initializers above do the actual work when this class is first touched.
    }
}
