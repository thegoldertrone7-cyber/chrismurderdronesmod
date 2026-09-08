package chris.murderdrones.block;

import chris.murderdrones.Constants;
import chris.murderdrones.PlushieCharacters;
import chris.murderdrones.platform.Services;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Common (loader-agnostic) block/item/block-entity registration.
 * <p>
 * This replaces the original ModBlocks.java, which had a hand-written pair of static
 * fields (DeferredBlock + DeferredItem + BlockEntityType Supplier) for each of the 19
 * characters — 3 lines per character, every time a character was added. Here it's a
 * single loop over PlushieCharacters.ALL, so adding a character to that one list is
 * now the *only* place block/item/block-entity registration needs to change.
 * <p>
 * Actual registration happens via Services.REGISTRY (IRegistryHelper), which each
 * loader backs differently — NeoForge with DeferredRegister, Fabric with a direct
 * Registry.register call. See platform/services/IRegistryHelper.java.
 */
public final class ModBlocksCommon {
    private ModBlocksCommon() {}

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    // ── Model asset folder layout ─────────────────────────────────────────────
    private static final Set<String> MURDER_DRONES_CHARACTERS = Set.of(
            "uzi", "n", "v", "j", "cyn", "cynessa", "doll", "khan", "lizzie", "teacher", "tessa");
    private static final Set<String> DIGITAL_CIRCUS_CHARACTERS = Set.of(
            "pomni", "jax", "ragatha", "gangle", "zooble", "kinger", "caine", "npc", "bubble");

    public static String getModelFolder(String character) {
        if (MURDER_DRONES_CHARACTERS.contains(character)) return "murder_drones/" + character;
        if (DIGITAL_CIRCUS_CHARACTERS.contains(character)) return "digital_circus/" + character;
        return "";
    }

    public static String getModelPath(String character, String modelFileName) {
        String folder = getModelFolder(character);
        return folder.isEmpty() ? modelFileName : folder + "/" + modelFileName;
    }

    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Per-character registry maps ─────────────────────────────────────────────
    private static final Map<String, Supplier<Block>> BLOCKS_BY_ID = new LinkedHashMap<>();
    private static final Map<String, Supplier<BlockItem>> ITEMS_BY_ID = new LinkedHashMap<>();
    private static final Map<String, Supplier<BlockEntityType<PlushieBlockEntity>>> BLOCK_ENTITIES_BY_ID = new LinkedHashMap<>();

    public static Supplier<Block> blockFor(String id) {
        return BLOCKS_BY_ID.getOrDefault(id, BLOCKS_BY_ID.get("uzi"));
    }

    public static Supplier<BlockItem> itemFor(String id) {
        return ITEMS_BY_ID.getOrDefault(id, ITEMS_BY_ID.get("uzi"));
    }

    public static BlockEntityType<PlushieBlockEntity> blockEntityTypeFor(String id) {
        Supplier<BlockEntityType<PlushieBlockEntity>> sup = BLOCK_ENTITIES_BY_ID.getOrDefault(id, BLOCK_ENTITIES_BY_ID.get("uzi"));
        return sup.get();
    }

    public static SoundEvent[] getVoicelines(String character) {
        PlushieCharacters.Character entry = PlushieCharacters.get(character);
        if (entry == null) return new SoundEvent[0];
        SoundEvent[] voicelines = new SoundEvent[entry.voicelineCount()];
        for (int i = 0; i < entry.voicelineCount(); i++) {
            String name = character + "_voiceline" + (i + 1);
            voicelines[i] = chris.murderdrones.ModSoundsCommon.soundFor(name).get();
        }
        return voicelines;
    }

    public static void spawnClickParticles(Level level, BlockPos pos) {
        if (!level.isClientSide) return;
        double cx = pos.getX() + 0.5, cy = pos.getY() + 1.2, cz = pos.getZ() + 0.5;
        for (int i = 0; i < 5; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.6;
            double oz = (level.random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.NOTE, cx + ox, cy, cz + oz, level.random.nextDouble(), 0, 0);
        }
        for (int i = 0; i < 3; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.4;
            double oz = (level.random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ParticleTypes.HEART, cx + ox, cy + 0.1, cz + oz, 0, 0.05, 0);
        }
    }

    // ── Plushie Base (single block holding any character) ──────────────────────
    public static final Supplier<Block> PLUSHIE_BASE_BLOCK = Services.REGISTRY.registerBlock("plushie_base",
            () -> new PlushieBaseBlock(BlockBehaviour.Properties.of()
                    .destroyTime(0.5f)
                    .explosionResistance(0.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    public static final Supplier<Item> PLUSHIE_BASE_ITEM = Services.REGISTRY.registerItem("plushie_base",
            () -> new BlockItem(PLUSHIE_BASE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockEntityType<PlushieBaseBlockEntity>> PLUSHIE_BASE_BLOCK_ENTITY =
            Services.REGISTRY.registerBlockEntity("plushie_base_tile", PlushieBaseBlockEntity::new, PLUSHIE_BASE_BLOCK);

    // ── Screwdriver ──────────────────────────────────────────────────────────────
    // Not a simple `new ScrewdriverItem(...)` supplier like everything else here:
    // NeoForge needs its own subclass (NeoForgeScrewdriverItem) to add back a
    // NeoForge-only extension method — see ScrewdriverItem's doc comment. Each
    // loader's mod entry passes its own constructor reference into init() below.
    public static Supplier<Item> SCREWDRIVER;

    public static void registerScrewdriver(java.util.function.Function<Item.Properties, ScrewdriverItem> factory) {
        SCREWDRIVER = Services.REGISTRY.registerItem("screwdriver",
                () -> factory.apply(new Item.Properties().stacksTo(1)));
    }

    // ── Per-character plushie blocks/items/block-entities ───────────────────────
    // Was 19 copy-pasted DeferredBlock/DeferredItem/BlockEntityType field trios in
    // ModBlocks.java — now one loop over PlushieCharacters.ALL.
    static {
        for (PlushieCharacters.Character character : PlushieCharacters.ALL) {
            String id = character.id();

            Supplier<Block> block = Services.REGISTRY.registerBlock(id, () -> new PlushieBlock(id,
                    BlockBehaviour.Properties.of()
                            .destroyTime(0.8f)
                            .explosionResistance(0.8f)
                            .noOcclusion()
                            .sound(SoundType.WOOL)));
            BLOCKS_BY_ID.put(id, block);

            @SuppressWarnings("unchecked")
            Supplier<BlockItem> item = (Supplier<BlockItem>) (Supplier<?>) Services.REGISTRY.registerItem(id,
                    () -> new PlushieBlockItem(block.get(), new Item.Properties()));
            ITEMS_BY_ID.put(id, item);

            Supplier<BlockEntityType<PlushieBlockEntity>> blockEntity =
                    Services.REGISTRY.registerBlockEntity(id + "_tile", PlushieBlockEntity::new, block);
            BLOCK_ENTITIES_BY_ID.put(id, blockEntity);
        }
    }

    /** Called once from each loader's mod entry point (via ChrisMurderDronesMod.init) — triggers this class's static registration block, and registers the screwdriver with whichever ScrewdriverItem subclass that loader needs. */
    public static void init(java.util.function.Function<Item.Properties, ScrewdriverItem> screwdriverFactory) {
        registerScrewdriver(screwdriverFactory);
    }
}
