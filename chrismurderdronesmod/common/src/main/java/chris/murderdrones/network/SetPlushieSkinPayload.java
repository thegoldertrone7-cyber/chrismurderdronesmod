package chris.murderdrones.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent client → server when a player picks a skin from {@link PlushieSkinPickerScreen},
 * the screwdriver hotkey's per-block skin picker (replacement for the old Assembly
 * Machine). {@code pos} is the placed plushie the player was looking at when they opened
 * the picker; the server re-validates reach, screwdriver-in-hand, and the skin index
 * bounds before touching anything — see ModNetworking#handleSetPlushieSkin.
 */
public record SetPlushieSkinPayload(BlockPos pos, int skinIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SetPlushieSkinPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(chris.murderdrones.Constants.MOD_ID, "set_plushie_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetPlushieSkinPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, SetPlushieSkinPayload::pos,
                    ByteBufCodecs.VAR_INT, SetPlushieSkinPayload::skinIndex,
                    SetPlushieSkinPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}