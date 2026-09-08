package chris.murderdrones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Plushie Base is one single block type that can hold any character (unlike
 * the per-character PlushieBlock subclasses). Everything about what's currently
 * attached lives here in the block entity rather than in the blockstate:
 *   - character: registry id of the attached plushie ("" = nothing attached)
 *   - skinIndex: which skin of that character is showing
 *   - offsetX / offsetZ: pixel-perfect placement, 0-15 (sixteenths of a block),
 *     instead of always centering the plushie on the block
 *   - rotation: 0-15, same 22.5°-per-step convention as the regular plushie blocks
 *   - baseVisible: whether the base plate itself renders, independent of the
 *     attached plushie
 */
public class PlushieBaseBlockEntity extends BlockEntity {

    private String character = "";
    private int skinIndex = 0;
    private int offsetX = 8;
    private int offsetZ = 8;
    private int rotation = 0;
    private boolean baseVisible = true;
    private long voicelineCooldownUntil = 0L;
    private long jumpStartTime = -1L;

    public boolean isVoicelineOnCooldown() {
        return System.currentTimeMillis() < voicelineCooldownUntil;
    }

    public void startVoicelineCooldown(long durationMs) {
        voicelineCooldownUntil = System.currentTimeMillis() + durationMs;
    }

    public void triggerJump() {
        jumpStartTime = System.currentTimeMillis();
    }

    /** Same bounce curve as the regular plushie blocks — reuses PlushieBlockEntity's constants. */
    public PlushieBlockEntity.AnimationState getAnimationState() {
        if (jumpStartTime < 0) return PlushieBlockEntity.AnimationState.IDLE;

        long elapsed = System.currentTimeMillis() - jumpStartTime;
        if (elapsed >= PlushieBlockEntity.JUMP_DURATION_MS) {
            jumpStartTime = -1L;
            return PlushieBlockEntity.AnimationState.IDLE;
        }

        float p = (float) elapsed / (float) PlushieBlockEntity.JUMP_DURATION_MS;
        float yOffset;
        float scaleXZ;
        float scaleY;

        if (p < PlushieBlockEntity.PHASE_ANTICIPATION_END) {
            float t = p / PlushieBlockEntity.PHASE_ANTICIPATION_END;
            float squash = t * t * (3f - 2f * t);
            yOffset = -0.06F * squash;
            scaleXZ = 1.0F + 0.18F * squash;
            scaleY  = 1.0F - 0.15F * squash;
        } else if (p < PlushieBlockEntity.PHASE_PEAK) {
            float t = (p - PlushieBlockEntity.PHASE_ANTICIPATION_END) / (PlushieBlockEntity.PHASE_PEAK - PlushieBlockEntity.PHASE_ANTICIPATION_END);
            yOffset = (float) Math.sin(t * (Math.PI / 2.0)) * 0.40F;
            float stretchAmt = (float) Math.sin(t * Math.PI) * 0.22F;
            scaleXZ = 1.0F - stretchAmt * 0.55F;
            scaleY  = 1.0F + stretchAmt;
        } else if (p < PlushieBlockEntity.PHASE_LAND_START) {
            float t = (p - PlushieBlockEntity.PHASE_PEAK) / (PlushieBlockEntity.PHASE_LAND_START - PlushieBlockEntity.PHASE_PEAK);
            yOffset = (float) Math.cos(t * (Math.PI / 2.0)) * 0.40F;
            float easeInOut = t * t * (3f - 2f * t);
            float stretchAmt = (1.0F - easeInOut) * 0.10F;
            scaleXZ = 1.0F - stretchAmt * 0.5F;
            scaleY  = 1.0F + stretchAmt;
        } else {
            float t = (p - PlushieBlockEntity.PHASE_LAND_START) / (1.0F - PlushieBlockEntity.PHASE_LAND_START);
            yOffset = 0.0F;
            float squash = (float) Math.sin(t * Math.PI) * (1.0F - t * 0.6F);
            scaleXZ = 1.0F + 0.22F * squash;
            scaleY  = 1.0F - 0.18F * squash;
        }

        return new PlushieBlockEntity.AnimationState(yOffset, scaleXZ, scaleY);
    }

    public PlushieBaseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocksCommon.PLUSHIE_BASE_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasPlushie() {
        return character != null && !character.isEmpty();
    }

    public String getCharacter() { return character; }
    public int getSkinIndex() { return skinIndex; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetZ() { return offsetZ; }
    public int getRotation() { return rotation; }
    public boolean isBaseVisible() { return baseVisible; }

    /** Attaches a plushie at the given pixel-perfect offset and rotation. */
    public void attach(String character, int offsetX, int offsetZ, int rotation) {
        this.character = character;
        this.skinIndex = 0;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.rotation = rotation & 15;
        sync();
    }

    /** Clears the attached plushie (used when picking it back up). */
    public void clear() {
        this.character = "";
        this.skinIndex = 0;
        sync();
    }

    public void cycleSkin(boolean backwards) {
        if (!hasPlushie()) return;
        int maxSkins = PlushieBlockEntity.getSkinCount(character);
        if (backwards) {
            skinIndex = (skinIndex - 1 + maxSkins) % maxSkins;
        } else {
            skinIndex = (skinIndex + 1) % maxSkins;
        }
        sync();
    }

    /** Rotates the attached plushie by one 22.5° step (empty-hand shift-click on the plushie). */
    public void rotatePlushie() {
        rotation = (rotation + 1) & 15;
        sync();
    }

    public void toggleBaseVisible() {
        baseVisible = !baseVisible;
        sync();
    }

    public void setBaseVisible(boolean visible) {
        baseVisible = visible;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Character", character == null ? "" : character);
        tag.putInt("SkinIndex", skinIndex);
        tag.putInt("OffsetX", offsetX);
        tag.putInt("OffsetZ", offsetZ);
        tag.putInt("Rotation", rotation);
        tag.putBoolean("BaseVisible", baseVisible);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        character = tag.getString("Character");
        skinIndex = tag.getInt("SkinIndex");
        offsetX = tag.contains("OffsetX") ? tag.getInt("OffsetX") : 8;
        offsetZ = tag.contains("OffsetZ") ? tag.getInt("OffsetZ") : 8;
        rotation = tag.getInt("Rotation");
        baseVisible = !tag.contains("BaseVisible") || tag.getBoolean("BaseVisible");
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
}
