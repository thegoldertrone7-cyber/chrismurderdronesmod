package chris.murderdrones.network;

import chris.murderdrones.Constants;
import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBlock;
import chris.murderdrones.block.PlushieBlockEntity;
import chris.murderdrones.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Ported from the original ModNetworking.java. The handler bodies are unchanged
 * vanilla logic — only the registration mechanism (loader-specific) was factored
 * out into Services.NETWORK / IRegistryHelper's NeoForge and Fabric implementations.
 */
public final class ModNetworkingCommon {
    private ModNetworkingCommon() {}

    public static void init() {
        Services.NETWORK.registerC2SPayload(GivePlushiePayload.TYPE, GivePlushiePayload.STREAM_CODEC,
                ModNetworkingCommon::handleGivePlushie);
        Services.NETWORK.registerC2SPayload(SetPlushieSkinPayload.TYPE, SetPlushieSkinPayload.STREAM_CODEC,
                ModNetworkingCommon::handleSetPlushieSkin);
    }

    private static void handleGivePlushie(GivePlushiePayload payload, ServerPlayer serverPlayer) {
        if (!serverPlayer.isCreative()) return; // safety: only usable in creative, regardless of client state
        if (!Services.CONFIG.enableCreativeQuickGive()) return; // server owners can turn the shortcut off

        Item item = BuiltInRegistries.ITEM.get(payload.itemId());
        if (item == null || item == Items.AIR) return;

        ItemStack base = new ItemStack(item);
        // PlushieBlockEntity#withSkin correctly stamps the block-entity "id" tag
        // alongside SkinIndex — see that method's comment for why a bare
        // {SkinIndex:N} tag crashes the game the next time something saves.
        ItemStack stack = payload.skinIndex() > 0 ? PlushieBlockEntity.withSkin(base, payload.skinIndex()) : base;

        if (!serverPlayer.getInventory().add(stack)) {
            serverPlayer.drop(stack, false);
        }

        serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 1.4F);

        String label = payload.label() == null || payload.label().isBlank()
                ? stack.getHoverName().getString()
                : payload.label();
        serverPlayer.displayClientMessage(
                Component.literal("Given: " + label).withStyle(style -> style.withColor(0xFFD700)), true);
    }

    /**
     * Handles a skin pick from the screwdriver hotkey's per-block skin picker.
     * Re-validates everything server-side rather than trusting the client — reach
     * distance, screwdriver actually in hand, the target really being a plushie
     * block, and the skin index being in range for that character.
     */
    private static void handleSetPlushieSkin(SetPlushieSkinPayload payload, ServerPlayer serverPlayer) {
        if (!(serverPlayer.level() instanceof ServerLevel level)) return;

        var pos = payload.pos();

        double maxReach = serverPlayer.blockInteractionRange() + 1.0;
        double distSq = serverPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        if (distSq > maxReach * maxReach) return;

        boolean holdingScrewdriver = serverPlayer.getMainHandItem().is(ModBlocksCommon.SCREWDRIVER.get())
                || serverPlayer.getOffhandItem().is(ModBlocksCommon.SCREWDRIVER.get());
        if (!holdingScrewdriver) return;

        if (!(level.getBlockEntity(pos) instanceof PlushieBlockEntity plushie)) return;
        if (!(level.getBlockState(pos).getBlock() instanceof PlushieBlock plushieBlock)) return;

        String character = plushieBlock.getCharacter();
        int maxSkins = PlushieBlockEntity.getSkinCount(character);
        int skin = payload.skinIndex();
        if (skin < 0 || skin >= maxSkins) return;

        plushie.setSkinIndex(skin);

        if (Services.CONFIG.enableSkinChangeFeedback()) {
            serverPlayer.displayClientMessage(
                    Component.literal(ModBlocksCommon.capitalize(character) + " — Skin " + (skin + 1) + " / " + maxSkins)
                            .withStyle(style -> style.withColor(0xFFD700)),
                    true);
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.3F);
        }
    }
}
