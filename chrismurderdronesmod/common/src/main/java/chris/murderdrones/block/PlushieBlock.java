package chris.murderdrones.block;

import chris.murderdrones.ModSoundUtil;
import chris.murderdrones.VoicelineDurations;
import chris.murderdrones.config.ICommonConfig;
import chris.murderdrones.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * One per-character plushie block (Uzi, N, V, ...). Unchanged gameplay logic from
 * the original mod — moved out of ModBlocks.java into its own file, and switched
 * from direct ModBlocks/ChrisMurderDronesMod/ServerConfig statics to the common
 * ModBlocksCommon registry map + Services.CONFIG so it compiles identically on
 * both loaders.
 */
public class PlushieBlock extends Block implements EntityBlock {
    private static final VoxelShape DOLL_SHAPE = Block.box(5.5, 0.0, 5.5, 11.0, 13.0, 11.0);

    private final String character;

    public PlushieBlock(String character, Properties properties) {
        super(properties);
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ModBlocksCommon.ROTATION,
                net.minecraft.util.Mth.floor((double) (context.getRotation() * 16.0F / 360.0F) + 0.5D) & 15);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ModBlocksCommon.ROTATION);
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
        ItemStack stack = new ItemStack(ModBlocksCommon.itemFor(character).get());

        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof PlushieBlockEntity plushie && plushie.getSkinIndex() > 0) {
            int skin = plushie.getSkinIndex();

            // See PlushieBlockEntity#withSkin for why the "id" tag is required here.
            CompoundTag beTag = new CompoundTag();
            ResourceLocation beId = net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(
                    PlushieBlockEntity.resolveType(ModBlocksCommon.itemFor(character).get().getBlock()));
            beTag.putString("id", beId.toString());
            beTag.putInt("SkinIndex", skin);
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(beTag));

            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(skin));
        }

        return List.of(stack);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ICommonConfig config = Services.CONFIG;

        if (PlushieBlockEntity.hasSkins(character) &&
                player.getMainHandItem().is(ModBlocksCommon.SCREWDRIVER.get())) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof PlushieBlockEntity plushie) {
                    plushie.cycleSkin(PlushieBlockEntity.getSkinCount(character), player.isSecondaryUseActive());

                    if (config.enableSkinChangeFeedback()) {
                        int skinCount = PlushieBlockEntity.getSkinCount(character);
                        int shown = plushie.getSkinIndex() + 1;
                        player.displayClientMessage(
                                Component.literal(ModBlocksCommon.capitalize(character) + " — Skin " + shown + " / " + skinCount)
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
                int current = state.getValue(ModBlocksCommon.ROTATION);
                level.setBlock(pos, state.setValue(ModBlocksCommon.ROTATION, (current + 1) % 16), Block.UPDATE_ALL);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockEntity beForCooldown = level.getBlockEntity(pos);
        if (beForCooldown instanceof PlushieBlockEntity plushieForCooldown
                && plushieForCooldown.isVoicelineOnCooldown()) {
            return InteractionResult.CONSUME;
        }

        SoundEvent[] lines = ModBlocksCommon.getVoicelines(character);
        if (lines.length > 0) {
            int lineIndex = level.random.nextInt(lines.length);
            if (beForCooldown instanceof PlushieBlockEntity plushieForCooldown) {
                plushieForCooldown.startVoicelineCooldown(
                        VoicelineDurations.getDurationMs(character, lineIndex));
            }

            if (!level.isClientSide) {
                SoundEvent chosen = lines[lineIndex];
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
            ModBlocksCommon.spawnClickParticles(level, pos);
        }
        return InteractionResult.SUCCESS;
    }
}
