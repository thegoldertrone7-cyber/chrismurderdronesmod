package chris.murderdrones.block;

import chris.murderdrones.ModSoundUtil;
import chris.murderdrones.VoicelineDurations;
import chris.murderdrones.config.ICommonConfig;
import chris.murderdrones.platform.Services;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;

/**
 * One single block that can hold any character's plushie (see PlushieBaseBlockEntity),
 * placed pixel-perfect instead of always centered like the regular plushie blocks.
 *
 * Interaction model — clicks are split into "base" vs "plushie" by hit height, since
 * both live in the same block space:
 *   - Right-click with a plushie item, base empty            -> attach it here
 *   - Right-click with the screwdriver, hit on the plushie   -> next skin
 *   - Shift+right-click screwdriver, hit on the plushie      -> previous skin
 *   - Shift+right-click screwdriver, hit on the base plate   -> toggle base visibility
 *   - Shift+right-click empty hand, hit on the plushie       -> rotate the plushie
 *   - Shift+right-click empty hand, hit on the base plate    -> pick up the plushie
 *     (and make the base visible again)
 */
public class PlushieBaseBlock extends Block implements EntityBlock {

    /** Plate is a thin slab near the bottom of the block; the plushie renders above it. */
    private static final VoxelShape PLATE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0);

    /** Hit height (in block-local Y, 0-1) below which a click counts as "on the base plate". */
    private static final double BASE_HIT_HEIGHT = 0.14;

    public PlushieBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlushieBaseBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof PlushieBaseBlockEntity base && base.hasPlushie()) {
            // A single tall box makes every downward-angled ray register near its TOP face
            // regardless of where you actually aim — so instead of one giant box, this is
            // the plate PLUS a smaller box positioned at the plushie's real pixel-perfect
            // offset. Aim at the character, you hit the character's box; aim at the plate
            // around it, you hit the plate — matching where things actually visually are.
            double ox = base.getOffsetX();
            double oz = base.getOffsetZ();
            double half = 4.5;
            double minX = Mth.clamp(ox - half, 0.0, 16.0);
            double maxX = Mth.clamp(ox + half, 0.0, 16.0);
            double minZ = Mth.clamp(oz - half, 0.0, 16.0);
            double maxZ = Mth.clamp(oz + half, 0.0, 16.0);
            VoxelShape characterShape = Block.box(minX, 2.0, minZ, maxX, 15.0, maxZ);
            return Shapes.or(PLATE_SHAPE, characterShape);
        }
        return PLATE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static boolean hitIsOnBase(BlockPos pos, BlockHitResult hit) {
        double localY = hit.getLocation().y - pos.getY();
        return localY <= BASE_HIT_HEIGHT;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PlushieBaseBlockEntity base)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Attach a plushie item to an empty base.
        if (!base.hasPlushie() && stack.getItem() instanceof PlushieBlockItem plushieItem) {
            if (!level.isClientSide) {
                double fx = hit.getLocation().x - pos.getX();
                double fz = hit.getLocation().z - pos.getZ();
                int offX = Mth.clamp((int) Math.floor(fx * 16.0), 0, 15);
                int offZ = Mth.clamp((int) Math.floor(fz * 16.0), 0, 15);
                int rot = Mth.floor((double) (player.getYRot() * 16.0F / 360.0F) + 0.5D) & 15;

                base.attach(plushieItem.getCharacter(), offX, offZ, rot);

                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.WOOL_PLACE,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.1F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Screwdriver: cycle skins (plushie hit) or toggle base visibility (base hit).
        if (base.hasPlushie() && stack.is(ModBlocksCommon.SCREWDRIVER.get())) {
            boolean onBase = hitIsOnBase(pos, hit);
            boolean shift = player.isSecondaryUseActive();

            if (!level.isClientSide) {
                if (onBase && shift) {
                    base.toggleBaseVisible();
                } else if (!onBase && PlushieBlockEntity.hasSkins(base.getCharacter())) {
                    base.cycleSkin(shift);
                    if (Services.CONFIG.enableSkinChangeFeedback()) {
                        int skinCount = PlushieBlockEntity.getSkinCount(base.getCharacter());
                        int shown = base.getSkinIndex() + 1;
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal(
                                        ModBlocksCommon.capitalize(base.getCharacter()) + " — Skin " + shown + " / " + skinCount)
                                        .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xFFD700)),
                                true);
                        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                                net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 1.3F);
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PlushieBaseBlockEntity base) || !base.hasPlushie()) {
            return InteractionResult.PASS;
        }

        boolean onBase = hitIsOnBase(pos, hit);

        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                if (onBase) {
                    // Pick up the plushie and reveal the base again.
                    ItemStack drop = PlushieBlockEntity.withSkin(
                            new ItemStack(ModBlocksCommon.itemFor(base.getCharacter()).get()), base.getSkinIndex());
                    if (!player.getInventory().add(drop)) {
                        player.drop(drop, false);
                    }
                    base.clear();
                    base.setBaseVisible(true);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                            net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, 1.2F);
                } else {
                    // Turn the plushie in place.
                    base.rotatePlushie();
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Plain right-click with an empty hand on the plushie: voiceline, same as the regular blocks.
        if (!onBase) {
            if (base.isVoicelineOnCooldown()) {
                return InteractionResult.CONSUME;
            }

            SoundEvent[] lines = ModBlocksCommon.getVoicelines(base.getCharacter());
            if (lines.length > 0) {
                int lineIndex = level.random.nextInt(lines.length);
                base.startVoicelineCooldown(VoicelineDurations.getDurationMs(base.getCharacter(), lineIndex));

                if (!level.isClientSide) {
                    SoundEvent chosen = lines[lineIndex];
                    ModSoundUtil.playFadingSound((ServerLevel) level,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            chosen, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F);
                }
            }

            if (level.isClientSide) {
                base.triggerJump();
                ModBlocksCommon.spawnClickParticles(level, pos);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
