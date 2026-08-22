package chris.murderdrones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = ChrisMurderDronesMod.MODID)
public class ModNetworking {

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        // Bumped to "3": added SetPlushieSkinPayload (the screwdriver hotkey's skin
        // picker), so a client running an older payload set must not negotiate this
        // channel — same reasoning as the earlier "1" → "2" bump for GivePlushiePayload.
        PayloadRegistrar registrar = event.registrar(ChrisMurderDronesMod.MODID).versioned("3");
        registrar.playToServer(GivePlushiePayload.TYPE, GivePlushiePayload.STREAM_CODEC,
                ModNetworking::handleGivePlushie);
        registrar.playToServer(SetPlushieSkinPayload.TYPE, SetPlushieSkinPayload.STREAM_CODEC,
                ModNetworking::handleSetPlushieSkin);
    }

    private static void handleGivePlushie(GivePlushiePayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            if (!serverPlayer.isCreative()) return; // safety: only usable in creative, regardless of client state
            if (!ServerConfig.ENABLE_CREATIVE_QUICK_GIVE.getAsBoolean()) return; // server owners can turn the shortcut off

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
        });
    }

    /**
     * Handles a skin pick from {@link PlushieSkinPickerScreen} (the screwdriver hotkey's
     * per-block skin picker). Re-validates everything server-side rather than trusting
     * the client — reach distance, screwdriver actually in hand, the target really being
     * a plushie block, and the skin index being in range for that character.
     */
    private static void handleSetPlushieSkin(SetPlushieSkinPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.level() instanceof ServerLevel level)) return;

            BlockPos pos = payload.pos();

            double maxReach = serverPlayer.blockInteractionRange() + 1.0;
            double distSq = serverPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distSq > maxReach * maxReach) return;

            boolean holdingScrewdriver = serverPlayer.getMainHandItem().is(ChrisMurderDronesMod.SCREWDRIVER.get())
                    || serverPlayer.getOffhandItem().is(ChrisMurderDronesMod.SCREWDRIVER.get());
            if (!holdingScrewdriver) return;

            if (!(level.getBlockEntity(pos) instanceof PlushieBlockEntity plushie)) return;
            if (!(level.getBlockState(pos).getBlock() instanceof ModBlocks.PlushieBlock plushieBlock)) return;

            String character = plushieBlock.getCharacter();
            int maxSkins = PlushieBlockEntity.getSkinCount(character);
            int skin = payload.skinIndex();
            if (skin < 0 || skin >= maxSkins) return;

            plushie.setSkinIndex(skin);

            if (ServerConfig.ENABLE_SKIN_CHANGE_FEEDBACK.getAsBoolean()) {
                serverPlayer.displayClientMessage(
                        Component.literal(ModBlocks.capitalize(character) + " — Skin " + (skin + 1) + " / " + maxSkins)
                                .withStyle(style -> style.withColor(0xFFD700)),
                        true);
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.3F);
            }
        });
    }
}