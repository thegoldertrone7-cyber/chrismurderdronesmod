package chris.murderdrones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ChrisMurderDronesMod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, ChrisMurderDronesMod.MODID);

    private static final VoxelShape DOLL_SHAPE = Block.box(5.5, 0.0, 5.5, 11.0, 13.0, 11.0);

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;

    // ── Model asset folder layout ─────────────────────────────────────────────
    private static final Set<String> MURDER_DRONES_CHARACTERS = Set.of(
            "uzi", "n", "v", "j", "cyn", "cynessa", "doll", "khan", "lizzie", "teacher", "tessa");
    private static final Set<String> DIGITAL_CIRCUS_CHARACTERS = Set.of(
            "pomni", "jax", "ragatha", "gangle", "zooble", "kinger", "caine", "npc", "bubble");

    public static String getModelFolder(String character) {
        if (MURDER_DRONES_CHARACTERS.contains(character)) {
            return "murder_drones/" + character;
        }
        if (DIGITAL_CIRCUS_CHARACTERS.contains(character)) {
            return "digital_circus/" + character;
        }
        return "";
    }

    public static String getModelPath(String character, String modelFileName) {
        String folder = getModelFolder(character);
        return folder.isEmpty() ? modelFileName : folder + "/" + modelFileName;
    }

    /** Shared by PlushieBlock's screwdriver feedback and PlushieBlockItem's tooltip. */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Block registrations ───────────────────────────────────────────────────
    public static final DeferredBlock<Block> UZI_BLOCK     = registerPlushie("uzi");
    public static final DeferredBlock<Block> N_BLOCK       = registerPlushie("n");
    public static final DeferredBlock<Block> V_BLOCK       = registerPlushie("v");
    public static final DeferredBlock<Block> J_BLOCK       = registerPlushie("j");
    public static final DeferredBlock<Block> CYN_BLOCK     = registerPlushie("cyn");
    public static final DeferredBlock<Block> CYNESSA_BLOCK = registerPlushie("cynessa");
    public static final DeferredBlock<Block> DOLL_BLOCK    = registerPlushie("doll");
    public static final DeferredBlock<Block> KHAN_BLOCK    = registerPlushie("khan");
    public static final DeferredBlock<Block> LIZZIE_BLOCK  = registerPlushie("lizzie");
    public static final DeferredBlock<Block> TEACHER_BLOCK = registerPlushie("teacher");
    public static final DeferredBlock<Block> TESSA_BLOCK   = registerPlushie("tessa");
    public static final DeferredBlock<Block> POMNI_BLOCK   = registerPlushie("pomni");
    public static final DeferredBlock<Block> JAX_BLOCK     = registerPlushie("jax");
    public static final DeferredBlock<Block> RAGATHA_BLOCK = registerPlushie("ragatha");
    public static final DeferredBlock<Block> GANGLE_BLOCK  = registerPlushie("gangle");
    public static final DeferredBlock<Block> ZOOBLE_BLOCK  = registerPlushie("zooble");
    public static final DeferredBlock<Block> KINGER_BLOCK  = registerPlushie("kinger");
    public static final DeferredBlock<Block> CAINE_BLOCK   = registerPlushie("caine");
    public static final DeferredBlock<Block> NPC_BLOCK     = registerPlushie("npc");
    public static final DeferredBlock<Block> BUBBLE_BLOCK  = registerPlushie("bubble");

    // ── Plushie Base ─────────────────────────────────────────────────────────
    // One single block that can hold ANY character (see PlushieBaseBlockEntity),
    // placed pixel-perfect instead of the always-centered per-character blocks above.
    public static final DeferredBlock<Block> PLUSHIE_BASE_BLOCK = BLOCKS.register("plushie_base",
            () -> new PlushieBaseBlock(BlockBehaviour.Properties.of()
                    .destroyTime(0.5f)
                    .explosionResistance(0.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    public static final DeferredItem<net.minecraft.world.item.Item> PLUSHIE_BASE_ITEM =
            ChrisMurderDronesMod.ITEMS.register("plushie_base",
                    () -> new BlockItem(PLUSHIE_BASE_BLOCK.get(), new net.minecraft.world.item.Item.Properties()));
    public static final Supplier<BlockEntityType<PlushieBaseBlockEntity>> PLUSHIE_BASE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("plushie_base_tile",
                    () -> BlockEntityType.Builder.of(PlushieBaseBlockEntity::new, PLUSHIE_BASE_BLOCK.get()).build(null));

    // ── Item registrations ────────────────────────────────────────────────────
    public static final DeferredItem<BlockItem> UZI_BLOCK_ITEM     = registerPlushieItem("uzi",     UZI_BLOCK);
    public static final DeferredItem<BlockItem> N_BLOCK_ITEM       = registerPlushieItem("n",       N_BLOCK);
    public static final DeferredItem<BlockItem> V_BLOCK_ITEM       = registerPlushieItem("v",       V_BLOCK);
    public static final DeferredItem<BlockItem> J_BLOCK_ITEM       = registerPlushieItem("j",       J_BLOCK);
    public static final DeferredItem<BlockItem> CYN_BLOCK_ITEM     = registerPlushieItem("cyn",     CYN_BLOCK);
    public static final DeferredItem<BlockItem> CYNESSA_BLOCK_ITEM = registerPlushieItem("cynessa", CYNESSA_BLOCK);
    public static final DeferredItem<BlockItem> DOLL_BLOCK_ITEM    = registerPlushieItem("doll",    DOLL_BLOCK);
    public static final DeferredItem<BlockItem> KHAN_BLOCK_ITEM    = registerPlushieItem("khan",    KHAN_BLOCK);
    public static final DeferredItem<BlockItem> LIZZIE_BLOCK_ITEM  = registerPlushieItem("lizzie",  LIZZIE_BLOCK);
    public static final DeferredItem<BlockItem> TEACHER_BLOCK_ITEM = registerPlushieItem("teacher", TEACHER_BLOCK);
    public static final DeferredItem<BlockItem> TESSA_BLOCK_ITEM   = registerPlushieItem("tessa",   TESSA_BLOCK);
    public static final DeferredItem<BlockItem> POMNI_BLOCK_ITEM   = registerPlushieItem("pomni",   POMNI_BLOCK);
    public static final DeferredItem<BlockItem> JAX_BLOCK_ITEM     = registerPlushieItem("jax",     JAX_BLOCK);
    public static final DeferredItem<BlockItem> RAGATHA_BLOCK_ITEM = registerPlushieItem("ragatha", RAGATHA_BLOCK);
    public static final DeferredItem<BlockItem> GANGLE_BLOCK_ITEM  = registerPlushieItem("gangle",  GANGLE_BLOCK);
    public static final DeferredItem<BlockItem> ZOOBLE_BLOCK_ITEM  = registerPlushieItem("zooble",  ZOOBLE_BLOCK);
    public static final DeferredItem<BlockItem> KINGER_BLOCK_ITEM  = registerPlushieItem("kinger",  KINGER_BLOCK);
    public static final DeferredItem<BlockItem> CAINE_BLOCK_ITEM   = registerPlushieItem("caine",   CAINE_BLOCK);
    public static final DeferredItem<BlockItem> NPC_BLOCK_ITEM     = registerPlushieItem("npc",     NPC_BLOCK);
    public static final DeferredItem<BlockItem> BUBBLE_BLOCK_ITEM  = registerPlushieItem("bubble",  BUBBLE_BLOCK);

    // Single id -> item lookup, replacing the two separate hand-written switches
    // that used to live in getDrops() and PlushieBrowserOpener. Add a line here
    // (next to its registerPlushieItem call above) whenever a new character is added.
    private static final Map<String, DeferredItem<BlockItem>> ITEMS_BY_ID = new LinkedHashMap<>();
    static {
        ITEMS_BY_ID.put("uzi", UZI_BLOCK_ITEM);
        ITEMS_BY_ID.put("n", N_BLOCK_ITEM);
        ITEMS_BY_ID.put("v", V_BLOCK_ITEM);
        ITEMS_BY_ID.put("j", J_BLOCK_ITEM);
        ITEMS_BY_ID.put("cyn", CYN_BLOCK_ITEM);
        ITEMS_BY_ID.put("cynessa", CYNESSA_BLOCK_ITEM);
        ITEMS_BY_ID.put("doll", DOLL_BLOCK_ITEM);
        ITEMS_BY_ID.put("khan", KHAN_BLOCK_ITEM);
        ITEMS_BY_ID.put("lizzie", LIZZIE_BLOCK_ITEM);
        ITEMS_BY_ID.put("teacher", TEACHER_BLOCK_ITEM);
        ITEMS_BY_ID.put("tessa", TESSA_BLOCK_ITEM);
        ITEMS_BY_ID.put("pomni", POMNI_BLOCK_ITEM);
        ITEMS_BY_ID.put("jax", JAX_BLOCK_ITEM);
        ITEMS_BY_ID.put("ragatha", RAGATHA_BLOCK_ITEM);
        ITEMS_BY_ID.put("gangle", GANGLE_BLOCK_ITEM);
        ITEMS_BY_ID.put("zooble", ZOOBLE_BLOCK_ITEM);
        ITEMS_BY_ID.put("kinger", KINGER_BLOCK_ITEM);
        ITEMS_BY_ID.put("caine", CAINE_BLOCK_ITEM);
        ITEMS_BY_ID.put("npc", NPC_BLOCK_ITEM);
        ITEMS_BY_ID.put("bubble", BUBBLE_BLOCK_ITEM);
    }

    /** Looks up a character's plushie item by its registry id (e.g. "uzi"). Falls back to Uzi if unknown. */
    public static DeferredItem<BlockItem> itemFor(String id) {
        return ITEMS_BY_ID.getOrDefault(id, UZI_BLOCK_ITEM);
    }

    // ── Block entity registrations ────────────────────────────────────────────
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> UZI_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("uzi_tile",     () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, UZI_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> N_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("n_tile",       () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, N_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> V_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("v_tile",       () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, V_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> J_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("j_tile",       () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, J_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> CYN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cyn_tile",     () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, CYN_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> CYNESSA_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("cynessa_tile", () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, CYNESSA_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> DOLL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("doll_tile",    () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, DOLL_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> KHAN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("khan_tile",    () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, KHAN_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> LIZZIE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("lizzie_tile",  () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, LIZZIE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> TEACHER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("teacher_tile", () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, TEACHER_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> TESSA_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("tessa_tile",   () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, TESSA_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> POMNI_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pomni_tile",   () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, POMNI_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> JAX_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("jax_tile",     () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, JAX_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> RAGATHA_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ragatha_tile", () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, RAGATHA_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> GANGLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("gangle_tile",  () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, GANGLE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> ZOOBLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("zooble_tile",  () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, ZOOBLE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> KINGER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("kinger_tile",  () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, KINGER_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> CAINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("caine_tile",   () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, CAINE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> NPC_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("npc_tile",     () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, NPC_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<PlushieBlockEntity>> BUBBLE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("bubble_tile",  () -> BlockEntityType.Builder.of(PlushieBlockEntity::new, BUBBLE_BLOCK.get()).build(null));

    // ── Voiceline arrays per character ────────────────────────────────────────
    // Looked up by naming convention ("<id>_voicelineN") against PlushieCharacters'
    // voicelineCount, instead of a hand-written switch — see PlushieCharacters.java
    // for how to add/adjust a character's voiceline count.
    public static SoundEvent[] getVoicelines(String character) {
        PlushieCharacters.Character entry = PlushieCharacters.get(character);
        if (entry == null) return new SoundEvent[0];
        SoundEvent[] voicelines = new SoundEvent[entry.voicelineCount()];
        for (int i = 0; i < entry.voicelineCount(); i++) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    ChrisMurderDronesMod.MODID, character + "_voiceline" + (i + 1));
            voicelines[i] = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(id);
        }
        return voicelines;
    }

    public static void spawnClickParticles(Level level, BlockPos pos) {
        if (!level.isClientSide) return;
        double cx = pos.getX() + 0.5, cy = pos.getY() + 1.2, cz = pos.getZ() + 0.5;
        for (int i = 0; i < 5; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.6;
            double oz = (level.random.nextDouble() - 0.5) * 0.6;
            level.addParticle(ParticleTypes.NOTE,  cx + ox, cy, cz + oz, level.random.nextDouble(), 0, 0);
        }
        for (int i = 0; i < 3; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.4;
            double oz = (level.random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ParticleTypes.HEART, cx + ox, cy + 0.1, cz + oz, 0, 0.05, 0);
        }
    }

    private static DeferredBlock<Block> registerPlushie(String name) {
        return BLOCKS.register(name, () -> new PlushieBlock(name,
                BlockBehaviour.Properties.of()
                        .destroyTime(0.8f)
                        .explosionResistance(0.8f)
                        .noOcclusion()
                        .sound(SoundType.WOOL)
        ));
    }

    public static class PlushieBlock extends Block implements EntityBlock {
        private final String character;

        public PlushieBlock(String character, Properties properties) {
            super(properties);
            this.character = character;
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(ROTATION,
                    net.minecraft.util.Mth.floor((double)(context.getRotation() * 16.0F / 360.0F) + 0.5D) & 15);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(ROTATION);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new PlushieBlockEntity(pos, state);
        }

        @Override
        public RenderShape getRenderShape(BlockState state) {
            return RenderShape.INVISIBLE;
        }

        @Override
        public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
            return true;
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return DOLL_SHAPE;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return Shapes.empty();
        }

        @Override
        public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
            if (player.getMainHandItem().is(Items.SHEARS)) {
                return super.getDestroyProgress(state, player, level, pos) * 5.0F;
            }
            return super.getDestroyProgress(state, player, level, pos);
        }

        @Override
        public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            DeferredItem<BlockItem> itemReg = itemFor(character);
            ItemStack stack = new ItemStack(itemReg.get());

            BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (be instanceof PlushieBlockEntity plushie && plushie.getSkinIndex() > 0) {
                int skin = plushie.getSkinIndex();

                CompoundTag beTag = new CompoundTag();
                // BLOCK_ENTITY_DATA is validated like real block-entity NBT the moment the game
                // tries to save this stack (player inventory save, world save, etc.) — NeoForge's
                // DataComponentUtil.wrapEncodingExceptions re-encodes it through the block entity
                // codec, which requires an "id" tag naming the block entity type. Without it you
                // get "Missing id for entity in: {SkinIndex:N}" the next time anything saves while
                // a re-skinned plushie drop is sitting in an inventory. Same fix as
                // AssemblyMachineMenu#withSkin, applied here to the block-break drop path.
                ResourceLocation beId = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(
                        PlushieBlockEntity.resolveType(itemReg.get().getBlock()));
                beTag.putString("id", beId.toString());
                beTag.putInt("SkinIndex", skin);
                stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(beTag));

                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(skin));
            }

            return List.of(stack);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (PlushieBlockEntity.hasSkins(character) &&
                    player.getMainHandItem().is(ChrisMurderDronesMod.SCREWDRIVER.get())) {
                if (!level.isClientSide) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof PlushieBlockEntity plushie) {
                        // Shift + right-click shuffles backwards; plain right-click shuffles forwards.
                        plushie.cycleSkin(PlushieBlockEntity.getSkinCount(character), player.isSecondaryUseActive());

                        if (ServerConfig.ENABLE_SKIN_CHANGE_FEEDBACK.getAsBoolean()) {
                            int skinCount = PlushieBlockEntity.getSkinCount(character);
                            int shown = plushie.getSkinIndex() + 1;
                            player.displayClientMessage(
                                    Component.literal(capitalize(character) + " — Skin " + shown + " / " + skinCount)
                                            .withStyle(Style.EMPTY.withColor(0xFFD700)),
                                    true);
                            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                                    net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 1.3F);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (player.isSecondaryUseActive()) {
                if (!level.isClientSide) {
                    int current = state.getValue(ROTATION);
                    level.setBlock(pos, state.setValue(ROTATION, (current + 1) % 16), Block.UPDATE_ALL);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            BlockEntity beForCooldown = level.getBlockEntity(pos);
            if (beForCooldown instanceof PlushieBlockEntity plushieForCooldown
                    && plushieForCooldown.isVoicelineOnCooldown()) {
                return InteractionResult.CONSUME;
            }

            SoundEvent[] lines = getVoicelines(character);
            if (lines.length > 0) {
                int lineIndex = level.random.nextInt(lines.length);
                if (beForCooldown instanceof PlushieBlockEntity plushieForCooldown) {
                    plushieForCooldown.startVoicelineCooldown(
                            VoicelineDurations.getDurationMs(character, lineIndex));
                }

                if (!level.isClientSide) {
                    SoundEvent chosen = lines[lineIndex];
                    // Genuine per-player distance fade instead of the old "volume as range
                    // multiplier" trick — see ModSoundUtil for why that trick clipped abruptly.
                    ModSoundUtil.playFadingSound((ServerLevel) level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            chosen, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F);
                }
            }

            if (level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PlushieBlockEntity plushie) {
                    plushie.triggerJump();
                }
                spawnClickParticles(level, pos);
            }
            return InteractionResult.SUCCESS;
        }

        public String getCharacter() {
            return character;
        }
    }

    @SuppressWarnings("unchecked")
    private static DeferredItem<BlockItem> registerPlushieItem(String name, DeferredBlock<Block> block) {
        return (DeferredItem<BlockItem>) (DeferredItem<?>) ChrisMurderDronesMod.ITEMS.register(name,
                () -> new PlushieBlockItem(block.get(), new net.minecraft.world.item.Item.Properties()));
    }

    public static class PlushieBlockItem extends BlockItem {

        public PlushieBlockItem(Block block, net.minecraft.world.item.Item.Properties properties) {
            super(block, properties);
        }

        private String character() {
            return ((PlushieBlock) getBlock()).getCharacter();
        }

        /** Public accessor used by PlushieBaseBlock to identify which character this item attaches. */
        public String getCharacter() {
            return character();
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (player.isSecondaryUseActive()) {
                return InteractionResultHolder.pass(stack);
            }

            SoundEvent[] lines = getVoicelines(character());
            if (ServerConfig.ENABLE_HAND_VOICELINES.getAsBoolean() && lines.length > 0) {
                // Picked on both sides identically-shaped (not networked), matching how
                // ItemCooldowns already needs to run on both sides for the UI overlay to
                // reflect what the server enforces underneath.
                int lineIndex = level.random.nextInt(lines.length);
                int durationMs = VoicelineDurations.getDurationMs(character(), lineIndex);
                int durationTicks = Math.max(1, durationMs / 50);
                player.getCooldowns().addCooldown(this, durationTicks);

                if (!level.isClientSide) {
                    SoundEvent chosen = lines[lineIndex];
                    ModSoundUtil.playFadingSound((ServerLevel) level,
                            player.getX(), player.getY(), player.getZ(),
                            chosen, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F);
                }
            } else {
                player.getCooldowns().addCooldown(this, ServerConfig.VOICELINE_COOLDOWN_TICKS.get());
            }

            return InteractionResultHolder.success(stack);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
                return super.useOn(context);
            }
            return InteractionResult.PASS;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context,
                                    List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            String ch = character();
            tooltip.add(Component.literal(capitalize(ch))
                    .withStyle(Style.EMPTY.withColor(0xFFD700).withItalic(false)));

            int skinCount = PlushieBlockEntity.getSkinCount(ch);
            if (skinCount > 1) {
                CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
                int skin = cmd != null ? cmd.value() : 0;
                tooltip.add(Component.literal("Skin " + (skin + 1) + " / " + skinCount)
                        .withStyle(Style.EMPTY.withColor(0x77DD77).withItalic(false)));
            }

            PlushieCharacters.Character charEntry = PlushieCharacters.get(ch);
            String flavour = charEntry != null ? charEntry.flavourText() : "A Murder Drones plushie.";
            tooltip.add(Component.literal(flavour)
                    .withStyle(Style.EMPTY.withColor(0x55FFFF).withItalic(true)));
            tooltip.add(Component.literal("Right-click to squeeze  |  Sneak+place to put down")
                    .withStyle(Style.EMPTY.withColor(0x888888).withItalic(true)));
        }

    }
}