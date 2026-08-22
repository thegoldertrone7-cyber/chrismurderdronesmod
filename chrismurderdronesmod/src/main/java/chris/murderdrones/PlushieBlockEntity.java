package chris.murderdrones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlushieBlockEntity extends BlockEntity {

    private long jumpStartTime = -1L;
    private long voicelineCooldownUntil = 0L;

    public boolean isVoicelineOnCooldown() {
        return System.currentTimeMillis() < voicelineCooldownUntil;
    }

    public void startVoicelineCooldown(long durationMs) {
        voicelineCooldownUntil = System.currentTimeMillis() + durationMs;
    }

    private int skinIndex = 0;

    // ── Murder Drones ────────────────────────────────────────────────────────
    public static final int UZI_SKIN_COUNT     = 3;
    public static final int N_SKIN_COUNT       = 5;
    public static final int V_SKIN_COUNT       = 5;
    public static final int J_SKIN_COUNT       = 6;
    public static final int CYN_SKIN_COUNT     = 3;
    public static final int CYNESSA_SKIN_COUNT = 2;
    public static final int DOLL_SKIN_COUNT    = 4;
    public static final int KHAN_SKIN_COUNT    = 3;
    public static final int LIZZIE_SKIN_COUNT  = 3;
    public static final int TEACHER_SKIN_COUNT = 2;
    public static final int TESSA_SKIN_COUNT   = 2;

    // ── Digital Circus ───────────────────────────────────────────────────────
    public static final int POMNI_SKIN_COUNT   = 13;
    public static final int JAX_SKIN_COUNT     = 22;
    public static final int RAGATHA_SKIN_COUNT = 13;
    public static final int GANGLE_SKIN_COUNT  = 13;
    public static final int ZOOBLE_SKIN_COUNT  = 9;
    public static final int KINGER_SKIN_COUNT  = 11;
    public static final int CAINE_SKIN_COUNT   = 6;
    public static final int NPC_SKIN_COUNT     = 15;
    public static final int BUBBLE_SKIN_COUNT  = 4;

    /**
     * Every character in the mod has an explicit entry here — even the ones that
     * currently only have a single (default) skin. That way "how many skins does
     * X have" is always a one-line answer to look up and bump, instead of relying
     * on characters silently falling through to a default of 1. If you add a new
     * character block, add its constant above and its case here; until then it'll
     * fall through to the default like everyone else used to.
     */
    public static int getSkinCount(String character) {
        return switch (character) {
            // Murder Drones
            case "uzi"     -> UZI_SKIN_COUNT;
            case "n"       -> N_SKIN_COUNT;
            case "v"       -> V_SKIN_COUNT;
            case "j"       -> J_SKIN_COUNT;
            case "cyn"     -> CYN_SKIN_COUNT;
            case "cynessa" -> CYNESSA_SKIN_COUNT;
            case "doll"    -> DOLL_SKIN_COUNT;
            case "khan"    -> KHAN_SKIN_COUNT;
            case "lizzie"  -> LIZZIE_SKIN_COUNT;
            case "teacher" -> TEACHER_SKIN_COUNT;
            case "tessa"   -> TESSA_SKIN_COUNT;
            // Digital Circus
            case "pomni"   -> POMNI_SKIN_COUNT;
            case "jax"     -> JAX_SKIN_COUNT;
            case "ragatha" -> RAGATHA_SKIN_COUNT;
            case "gangle"  -> GANGLE_SKIN_COUNT;
            case "zooble"  -> ZOOBLE_SKIN_COUNT;
            case "kinger"  -> KINGER_SKIN_COUNT;
            case "caine"   -> CAINE_SKIN_COUNT;
            case "npc"     -> NPC_SKIN_COUNT;
            case "bubble"  -> BUBBLE_SKIN_COUNT;
            default        -> 1;
        };
    }

    public static boolean hasSkins(String character) {
        return getSkinCount(character) > 1;
    }

    public static final long JUMP_DURATION_MS = 600L;
    public static final float PHASE_ANTICIPATION_END = 0.10F;
    public static final float PHASE_PEAK             = 0.45F;
    public static final float PHASE_LAND_START       = 0.85F;

    public PlushieBlockEntity(BlockPos pos, BlockState state) {
        super(resolveType(state), pos, state);
    }

    private static BlockEntityType<PlushieBlockEntity> resolveType(BlockState state) {
        return resolveType(state.getBlock());
    }

    public static BlockEntityType<PlushieBlockEntity> resolveType(net.minecraft.world.level.block.Block block) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(block).getPath();
        return switch (path) {
            case "n"       -> ModBlocks.N_BLOCK_ENTITY.get();
            case "v"       -> ModBlocks.V_BLOCK_ENTITY.get();
            case "j"       -> ModBlocks.J_BLOCK_ENTITY.get();
            case "cyn"     -> ModBlocks.CYN_BLOCK_ENTITY.get();
            case "cynessa" -> ModBlocks.CYNESSA_BLOCK_ENTITY.get();
            case "doll"    -> ModBlocks.DOLL_BLOCK_ENTITY.get();
            case "khan"    -> ModBlocks.KHAN_BLOCK_ENTITY.get();
            case "lizzie"  -> ModBlocks.LIZZIE_BLOCK_ENTITY.get();
            case "teacher" -> ModBlocks.TEACHER_BLOCK_ENTITY.get();
            case "tessa"   -> ModBlocks.TESSA_BLOCK_ENTITY.get();
            case "pomni"   -> ModBlocks.POMNI_BLOCK_ENTITY.get();
            case "jax"     -> ModBlocks.JAX_BLOCK_ENTITY.get();
            case "ragatha" -> ModBlocks.RAGATHA_BLOCK_ENTITY.get();
            case "gangle"  -> ModBlocks.GANGLE_BLOCK_ENTITY.get();
            case "zooble"  -> ModBlocks.ZOOBLE_BLOCK_ENTITY.get();
            case "kinger"  -> ModBlocks.KINGER_BLOCK_ENTITY.get();
            case "caine"   -> ModBlocks.CAINE_BLOCK_ENTITY.get();
            case "npc"     -> ModBlocks.NPC_BLOCK_ENTITY.get();
            case "bubble"  -> ModBlocks.BUBBLE_BLOCK_ENTITY.get();
            default        -> ModBlocks.UZI_BLOCK_ENTITY.get();
        };
    }

    public int getSkinIndex() { return skinIndex; }

    /**
     * Directly sets (rather than cycles) the skin index and syncs the change to nearby
     * clients. Used by the screwdriver's per-block skin picker hotkey (see
     * ModNetworking#handleSetPlushieSkin) so a specific skin can be chosen straight from
     * the picker's grid instead of stepping through them one at a time.
     */
    public void setSkinIndex(int skin) {
        this.skinIndex = skin;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Returns a copy of the given plushie item stack stamped with the given skin index,
     * via DataComponents.BLOCK_ENTITY_DATA + CUSTOM_MODEL_DATA. Used by the Creative
     * "Give" networking path and the skin picker screen's preview cards to stamp a skin
     * onto a plain ItemStack.
     * <p>
     * BLOCK_ENTITY_DATA is validated like real block-entity NBT the moment the game tries
     * to save this stack (player inventory save, world save, etc.) — NeoForge's
     * DataComponentUtil.wrapEncodingExceptions re-encodes it through the block entity
     * codec, which requires an "id" tag naming the block entity type. Without it you get
     * "Missing id for entity in: {SkinIndex:N}" the next time anything saves while a
     * re-skinned plushie is sitting in an inventory. Vanilla always includes this tag (see
     * BlockEntity#saveWithId) — we resolve it straight from the item's Block instead.
     */
    public static ItemStack withSkin(ItemStack base, int skin) {
        ItemStack result = base.copy();
        if (skin <= 0) {
            result.remove(DataComponents.BLOCK_ENTITY_DATA);
            result.remove(DataComponents.CUSTOM_MODEL_DATA);
            return result;
        }
        CompoundTag beTag = new CompoundTag();
        if (base.getItem() instanceof BlockItem blockItem) {
            ResourceLocation beId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(resolveType(blockItem.getBlock()));
            beTag.putString("id", beId.toString());
        }
        beTag.putInt("SkinIndex", skin);
        result.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(beTag));
        result.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(skin));
        return result;
    }

    public void cycleSkin(int maxSkins) {
        cycleSkin(maxSkins, false);
    }

    /** Advances (or reverses, if {@code backwards}) the skin index, wrapping around. */
    public void cycleSkin(int maxSkins, boolean backwards) {
        if (backwards) {
            skinIndex = (skinIndex - 1 + maxSkins) % maxSkins;
        } else {
            skinIndex = (skinIndex + 1) % maxSkins;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SkinIndex", skinIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        skinIndex = tag.getInt("SkinIndex");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public record AnimationState(float yOffset, float scaleXZ, float scaleY) {
        public static final AnimationState IDLE = new AnimationState(0f, 1f, 1f);
    }

    public void triggerJump() {
        jumpStartTime = System.currentTimeMillis();
    }

    public AnimationState getAnimationState() {
        if (jumpStartTime < 0) return AnimationState.IDLE;

        long elapsed = System.currentTimeMillis() - jumpStartTime;
        if (elapsed >= JUMP_DURATION_MS) {
            jumpStartTime = -1L;
            return AnimationState.IDLE;
        }

        float p = (float) elapsed / (float) JUMP_DURATION_MS;

        float yOffset;
        float scaleXZ;
        float scaleY;

        if (p < PHASE_ANTICIPATION_END) {
            float t = p / PHASE_ANTICIPATION_END;
            float squash = easeInOut(t);
            yOffset = -0.06F * squash;
            scaleXZ = 1.0F + 0.18F * squash;
            scaleY  = 1.0F - 0.15F * squash;

        } else if (p < PHASE_PEAK) {
            float t = (p - PHASE_ANTICIPATION_END) / (PHASE_PEAK - PHASE_ANTICIPATION_END);
            yOffset = (float) Math.sin(t * (Math.PI / 2.0)) * 0.40F;
            float stretchAmt = (float) Math.sin(t * Math.PI) * 0.22F;
            scaleXZ = 1.0F - stretchAmt * 0.55F;
            scaleY  = 1.0F + stretchAmt;

        } else if (p < PHASE_LAND_START) {
            float t = (p - PHASE_PEAK) / (PHASE_LAND_START - PHASE_PEAK);
            yOffset = (float) Math.cos(t * (Math.PI / 2.0)) * 0.40F;
            float stretchAmt = (1.0F - easeInOut(t)) * 0.10F;
            scaleXZ = 1.0F - stretchAmt * 0.5F;
            scaleY  = 1.0F + stretchAmt;

        } else {
            float t = (p - PHASE_LAND_START) / (1.0F - PHASE_LAND_START);
            yOffset = 0.0F;
            float squash = (float) Math.sin(t * Math.PI) * (1.0F - t * 0.6F);
            scaleXZ = 1.0F + 0.22F * squash;
            scaleY  = 1.0F - 0.18F * squash;
        }

        return new AnimationState(yOffset, scaleXZ, scaleY);
    }

    private static float easeInOut(float t) {
        return t * t * (3f - 2f * t);
    }

    @Deprecated
    public float getJumpOffset() {
        return getAnimationState().yOffset();
    }
}