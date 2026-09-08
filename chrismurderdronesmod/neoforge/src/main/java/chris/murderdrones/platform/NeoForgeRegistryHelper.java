package chris.murderdrones.platform;

import chris.murderdrones.Constants;
import chris.murderdrones.platform.services.IRegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Backs IRegistryHelper with DeferredRegisters, exactly like the original
 * ModBlocks.java/ChrisMurderDronesMod.java/ModSounds.java did directly. The only
 * real change is that ChrisMurderDronesModNeoForge (the mod entry point) now has
 * to explicitly call registerAll(modEventBus) once during construction, since
 * common code can't see the mod event bus itself.
 */
public class NeoForgeRegistryHelper implements IRegistryHelper {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    @Override
    public <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> holder = BLOCKS.register(name, block);
        return holder::get;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item) {
        DeferredItem<T> holder = ITEMS.register(name, item);
        return holder::get;
    }

    @Override
    public Supplier<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> event) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUND_EVENTS.register(name, event);
        return holder::get;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> holder = CREATIVE_MODE_TABS.register(name, tab);
        return holder::get;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, IRegistryHelper.Factory<T> factory, Supplier<? extends Block> blockEntity) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder =
                (DeferredHolder<BlockEntityType<?>, BlockEntityType<T>>) (DeferredHolder<?, ?>)
                        BLOCK_ENTITIES.register(name, () -> BlockEntityType.Builder.of(
                                (pos, state) -> factory.create(pos, state), blockEntity.get()).build(null));
        return holder::get;
    }

    /** Called once from ChrisMurderDronesModNeoForge's constructor to flush all queued registrations. */
    public static void registerAll(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

