package chris.murderdrones;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

@Mod(ChrisMurderDronesMod.MODID)
public class ChrisMurderDronesMod {
    public static final String MODID = "chrismurderdronesmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    // ── Deferred Registers ─────────────────────────────────────────────────────
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS   = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ── Screwdriver Registration ───────────────────────────────────────────────
    // Custom item class (not a plain Item) so it can opt out of vanilla's
    // sneak-bypasses-block-use default — see ScrewdriverItem for why that matters.
    public static final DeferredItem<Item> SCREWDRIVER = ITEMS.register("screwdriver",
            () -> new Screwdriveritem(new Item.Properties().stacksTo(1)));

    // ── Music Discs ─────────────────────────────────────────────────────────
    // As of 1.21, RecordItem is gone — a disc's playability is just a data
    // component (Item.Properties#jukeboxPlayable) pointing at a JukeboxSong
    // registry key. The JukeboxSong itself is a DATAPACK entry, not code, so
    // each key below must have a matching JSON at
    // data/chrismurderdronesmod/jukebox_song/<name>.json (provided separately)
    // that wires it to the SoundEvent registered in ModSounds. Until you supply
    // real audio for those sound events, these discs will register and show up
    // in the creative tab fine, they just won't make noise in a jukebox yet.
    private static DeferredItem<Item> registerMusicDisc(String name) {
        ResourceKey<JukeboxSong> songKey = ResourceKey.create(Registries.JUKEBOX_SONG,
                ResourceLocation.fromNamespaceAndPath(MODID, name));
        return ITEMS.register(name, () -> new Item(new Item.Properties()
                .stacksTo(1)
                .jukeboxPlayable(songKey)));
    }

    public static final DeferredItem<Item> FTIGE_MUSIC_DISC = registerMusicDisc("ftige_music_disc");
    public static final DeferredItem<Item> BM_MUSIC_DISC    = registerMusicDisc("bm_music_disc");
    public static final DeferredItem<Item> RTS_MUSIC_DISC   = registerMusicDisc("rts_music_disc");
    public static final DeferredItem<Item> FOREVER_MUSIC_DISC   = registerMusicDisc("forever_music_disc");

    // ── Section Header Icons ────────────────────────────────────────────────────
    // Non-functional items that act as anchor points for a full-width banner
    // drawn by CreativeTabHeaderRenderer (see that class). Their item models
    // point at the same blank/transparent texture as ROW_SPACER — the real
    // banner artwork (header_murder_drones.png etc.) is drawn directly by the
    // renderer, not via the item's own icon, so there's nothing left over to
    // visually peek out from behind/around the banner.
    //
    // Registered as RowSpacerItem (not plain Item) so they have no display
    // name for a tooltip/search bar to show, on top of the
    // hidden_from_recipe_viewers tag that keeps them out of JEI/EMI/REI's
    // ingredient lists and the click-blocking in CreativeTabHeaderRenderer
    // that stops them being dragged out of the creative tab.
    public static final DeferredItem<Item> HEADER_MURDER_DRONES  = ITEMS.register("header_murder_drones",
            () -> new RowSpacerItem(new Item.Properties()));
    public static final DeferredItem<Item> HEADER_DIGITAL_CIRCUS = ITEMS.register("header_digital_circus",
            () -> new RowSpacerItem(new Item.Properties()));
    public static final DeferredItem<Item> HEADER_MISC = ITEMS.register("header_misc",
            () -> new RowSpacerItem(new Item.Properties()));

    // Fills out the rest of a header's row (and, if needed, the tail of the row
    // before it) so the section banner drawn by CreativeTabHeaderRenderer always
    // owns a full, empty row and real items always start fresh on the row below.
    // Invisible/unnamed/unsearchable/ungrabbable — see RowSpacerItem.
    public static final DeferredItem<Item> ROW_SPACER = ITEMS.register("row_spacer",
            () -> new RowSpacerItem(new Item.Properties()));

    // Vanilla's creative tab grid is 9 columns wide.
    private static final int GRID_COLUMNS = 9;

    // ── Creative Tab ─────────────────────────────────────────────────────────────
    // Single tab holding everything the mod adds. Items are grouped into sections
    // (Murder Drones / Digital Circus / Misc) by inserting a header item as the
    // start of its own dedicated row, padded with ROW_SPACER on both sides so
    // nothing else shares that row — see CreativeTabHeaderRenderer, which paints
    // a full-width banner over the row that header lands on.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.chrismurderdronesmod.main"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> SCREWDRIVER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        int[] count = {0};

                        addHeader(output, count, HEADER_MURDER_DRONES.get());
                        addItem(output, count, ModBlocks.UZI_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.N_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.V_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.J_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.CYN_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.CYNESSA_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.DOLL_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.KHAN_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.LIZZIE_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.TEACHER_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.TESSA_BLOCK_ITEM.get());

                        addHeader(output, count, HEADER_DIGITAL_CIRCUS.get());
                        addItem(output, count, ModBlocks.POMNI_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.JAX_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.RAGATHA_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.GANGLE_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.ZOOBLE_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.KINGER_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.CAINE_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.NPC_BLOCK_ITEM.get());
                        addItem(output, count, ModBlocks.BUBBLE_BLOCK_ITEM.get());

                        addHeader(output, count, HEADER_MISC.get());
                        addItem(output, count, SCREWDRIVER.get());
                        addItem(output, count, FTIGE_MUSIC_DISC.get());
                        addItem(output, count, BM_MUSIC_DISC.get());
                        addItem(output, count, RTS_MUSIC_DISC.get());
                        addItem(output, count, FOREVER_MUSIC_DISC.get());
                    }).build());

    /**
     * Pads to the end of the current row with ROW_SPACER, if it's partway through one.
     * <p>
     * Creative-tab display lists dedupe identical ItemStacks (same item + same data
     * components collapse to a single entry), so a plain {@code ROW_SPACER.get()}
     * stack would only ever show up once per row no matter how many times it's
     * added — every copy after the first silently vanishes and real items slide
     * back up next to the header. Tagging each copy with a distinct
     * CUSTOM_MODEL_DATA value (unused by the item's model, purely here to make
     * the stacks compare unequal) keeps every padding slot from being merged away.
     */
    private static void padToRowEnd(CreativeModeTab.Output output, int[] count) {
        while (count[0] % GRID_COLUMNS != 0) {
            ItemStack spacer = new ItemStack(ROW_SPACER.get());
            spacer.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(count[0]));
            output.accept(spacer);
            count[0]++;
        }
    }

    /** Places a header at the start of a fresh row, then pads the rest of that row so it's alone. */
    private static void addHeader(CreativeModeTab.Output output, int[] count, Item header) {
        padToRowEnd(output, count); // finish whatever row came before, if any
        output.accept(header);
        count[0]++;
        padToRowEnd(output, count); // claim the rest of the header's own row
    }

    private static void addItem(CreativeModeTab.Output output, int[] count, net.minecraft.world.level.ItemLike item) {
        output.accept(item);
        count[0]++;
    }

    public ChrisMurderDronesMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // Core Registries
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Block & Sound Registries
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}