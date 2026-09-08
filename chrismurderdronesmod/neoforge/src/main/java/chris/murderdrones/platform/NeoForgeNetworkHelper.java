package chris.murderdrones.platform;

import chris.murderdrones.Constants;
import chris.murderdrones.platform.services.INetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Registration can only happen inside RegisterPayloadHandlersEvent (fires after mod
 * construction), so calls made during common init (which runs inside the mod
 * constructor) are queued here and flushed once that event fires — see
 * ChrisMurderDronesModNeoForge, which calls flush(event) from its own listener.
 */
public class NeoForgeNetworkHelper implements INetworkHelper {
    private static final List<Consumer<PayloadRegistrar>> PENDING = new ArrayList<>();

    @Override
    public <T extends CustomPacketPayload> void registerC2SPayload(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, C2SHandler<T> handler) {
        PENDING.add(registrar -> registrar.playToServer(type, codec, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        handler.handle(payload, serverPlayer);
                    }
                })));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    /** Called from ChrisMurderDronesModNeoForge's RegisterPayloadHandlersEvent listener. */
    public static void flush(RegisterPayloadHandlersEvent event) {
        // Bumped to "3": added SetPlushieSkinPayload (the screwdriver hotkey's skin
        // picker), so a client running an older payload set must not negotiate this
        // channel — same reasoning as the original mod's earlier "1" → "2" bump for
        // GivePlushiePayload.
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned("3");
        for (Consumer<PayloadRegistrar> queued : PENDING) {
            queued.accept(registrar);
        }
    }
}
