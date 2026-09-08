package chris.murderdrones.platform;

import chris.murderdrones.Constants;
import chris.murderdrones.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

/**
 * Fabric has no deferred/event-driven registration step like NeoForge's
 * DeferredRegister — Registry.register() runs immediately and returns the real
 * object. So unlike NeoForgeRegistryHelper, there's no registerAll() to call later;
 * each register*() call here does the whole job on the spot.
 */
public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        T registered = Registry.register(BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), block.get());
        return () -> registered;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        T registered = Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), item.get());
        return () -> registered;
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> event) {
        SoundEvent registered = Registry.register(BuiltInRegistries.SOUND_EVENT,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), event.get());
        return () -> registered;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        // A brand-new tab (not injecting into an existing vanilla one) is plain
        // vanilla registry API since the 1.19.3 creative-tab rework — no
        // Fabric-specific ItemGroupEvents needed here, unlike adding items to an
        // *existing* tab, which does need that API.
        CreativeModeTab registered = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), tab.get());
        return () -> registered;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, IRegistryHelper.Factory<T> factory, Supplier<? extends Block> blockEntity) {
        BlockEntityType<T> registered = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                BlockEntityType.Builder.of((pos, state) -> factory.create(pos, state), blockEntity.get()).build(null));
        return () -> registered;
    }
}

