package chris.murderdrones.client;

import chris.murderdrones.client.services.IStandaloneModelLookup;

import java.util.ServiceLoader;

/**
 * Deliberately separate from platform.Services: that class's fields are static
 * finals loaded eagerly the moment ANY common code touches it, including on a
 * dedicated server (ModBlocksCommon's registration reads Services.REGISTRY /
 * Services.CONFIG there). This class lives in the client package and is only ever
 * touched by client-only classes (ModBlocksRenderer, PlushieBaseBlockEntityRenderer),
 * exactly like those classes are themselves never loaded on a dedicated server.
 */
public final class ClientServices {
    public static final IStandaloneModelLookup MODEL_LOOKUP = load(IStandaloneModelLookup.class);

    private ClientServices() {}

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load client service for " + clazz.getName()));
    }
}
