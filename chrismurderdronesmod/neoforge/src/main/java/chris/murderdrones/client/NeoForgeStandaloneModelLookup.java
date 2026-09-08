package chris.murderdrones.client;

import chris.murderdrones.client.services.IStandaloneModelLookup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge patches vanilla's ModelResourceLocation to add a real STANDALONE_VARIANT
 * ("standalone") specifically for models registered via
 * ModelEvent.RegisterAdditional (see ClientModEvents) — so lookup here just has to
 * use the matching key.
 */
public class NeoForgeStandaloneModelLookup implements IStandaloneModelLookup {
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(id));
    }
}
