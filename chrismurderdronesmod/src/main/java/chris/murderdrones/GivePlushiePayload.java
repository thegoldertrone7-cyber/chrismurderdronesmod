package chris.murderdrones;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client → server when a player uses the "Give (Creative)" button in the
 * Plushie Catalog (see PlushieBrowserScreen). Creative-only, enforced server-side
 * in ModNetworking#handleGivePlushie regardless of what the client sends.
 *
 * @param itemId    registry id of the base plushie item (e.g. "chrismurderdronesmod:npc")
 * @param skinIndex which skin to stamp onto the given stack (0 = default, no stamping needed)
 * @param label     the catalog's "Character – Skin" display label, echoed back in the
 *                  server's action-bar confirmation so it matches what the player clicked
 */
public record GivePlushiePayload(ResourceLocation itemId, int skinIndex, String label) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<GivePlushiePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ChrisMurderDronesMod.MODID, "give_plushie"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GivePlushiePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, GivePlushiePayload::itemId,
                    ByteBufCodecs.VAR_INT, GivePlushiePayload::skinIndex,
                    ByteBufCodecs.STRING_UTF8, GivePlushiePayload::label,
                    GivePlushiePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}