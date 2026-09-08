package chris.murderdrones.platform.services;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Abstraction over NeoForge's RegisterPayloadHandlersEvent/PayloadRegistrar vs
 * Fabric's PayloadTypeRegistry/ServerPlayNetworking. Handlers are plain
 * (payload, ServerPlayer) callbacks — both loaders' implementations take care of
 * resolving the actual player and scheduling the callback onto the server thread
 * internally (NeoForge via IPayloadContext#enqueueWork, Fabric via
 * ServerPlayNetworking.Context#server().execute(...)), so common handler code never
 * needs to see either loader's context type.
 * <p>
 * NeoForge's registration can only happen inside RegisterPayloadHandlersEvent, which
 * fires after mod construction — so NeoForgeNetworkHelper queues these calls and
 * flushes them when that event fires, exactly like NeoForgeRegistryHelper queues
 * DeferredRegister entries. Fabric registers immediately, same as everywhere else.
 */
public interface INetworkHelper {
    <T extends CustomPacketPayload> void registerC2SPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            C2SHandler<T> handler);

    void sendToServer(CustomPacketPayload payload);

    @FunctionalInterface
    interface C2SHandler<T extends CustomPacketPayload> {
        void handle(T payload, ServerPlayer player);
    }
}
