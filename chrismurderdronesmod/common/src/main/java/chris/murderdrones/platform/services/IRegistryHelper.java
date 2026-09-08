package chris.murderdrones.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Abstraction over NeoForge's DeferredRegister vs Fabric's plain Registry.register,
 * so common code can register blocks/items/block entities without caring which
 * loader is actually backing the registry.
 * <p>
 * NeoForge's implementation queues these onto its own DeferredRegisters and only
 * actually registers them once the loader-specific mod entry point wires that
 * DeferredRegister to the mod event bus. Fabric's implementation registers
 * immediately, since Fabric has no equivalent deferred/event-driven registry step.
 * Either way, callers get back a Supplier they can call .get() on once startup has
 * finished — exactly like NeoForge's DeferredHolder already behaves.
 */
public interface IRegistryHelper {

    <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> block);

    <T extends Item> Supplier<T> registerItem(String name, Supplier<T> item);

    Supplier<SoundEvent> registerSoundEvent(String name, Supplier<SoundEvent> event);

    Supplier<CreativeModeTab> registerCreativeTab(String name, Supplier<CreativeModeTab> tab);

    /**
     * @param blockEntity block(s) this block entity type is valid for. Must already be
     *                    registered (via registerBlock) before this is called.
     */
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String name, Factory<T> factory, Supplier<? extends Block> blockEntity);

    /**
     * Same shape as BlockEntityType.BlockEntitySupplier, which we can't reference by
     * name here because it isn't declared public in 1.21.1's mappings — our own public
     * copy of it. PlushieBlockEntity::new / PlushieBaseBlockEntity::new satisfy this
     * exactly as-is since both take (BlockPos, BlockState).
     */
    @FunctionalInterface
    interface Factory<T extends BlockEntity> {
        T create(BlockPos pos, BlockState state);
    }
}

