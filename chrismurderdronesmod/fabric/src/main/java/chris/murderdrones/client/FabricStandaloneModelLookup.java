package chris.murderdrones.client;

import chris.murderdrones.client.services.IStandaloneModelLookup;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric API stores models registered via ModelLoadingPlugin.Context#addModels
 * (see ChrisMurderDronesModFabricClient) under its own internal "fabric_resource"
 * ModelResourceLocation variant, and exposes them through this mixed-in interface
 * on the vanilla model manager instead of vanilla's own getModel(ModelResourceLocation).
 */
public class FabricStandaloneModelLookup implements IStandaloneModelLookup {
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(id);
    }
}
