package chris.murderdrones.block;

import chris.murderdrones.ModSoundUtil;
import chris.murderdrones.VoicelineDurations;
import chris.murderdrones.config.ICommonConfig;
import chris.murderdrones.platform.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** The BlockItem for every PlushieBlock. Extracted verbatim from ModBlocks.java. */
public class PlushieBlockItem extends BlockItem {

    public PlushieBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    private String character() {
        return ((PlushieBlock) getBlock()).getCharacter();
    }

    /** Public accessor used by PlushieBaseBlock to identify which character this item attaches. */
    public String getCharacter() {
        return character();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isSecondaryUseActive()) {
            return InteractionResultHolder.pass(stack);
        }

        ICommonConfig config = Services.CONFIG;
        SoundEvent[] lines = ModBlocksCommon.getVoicelines(character());
        if (config.enableHandVoicelines() && lines.length > 0) {
            int lineIndex = level.random.nextInt(lines.length);
            int durationMs = VoicelineDurations.getDurationMs(character(), lineIndex);
            int durationTicks = Math.max(1, durationMs / 50);
            player.getCooldowns().addCooldown(this, durationTicks);

            if (!level.isClientSide) {
                SoundEvent chosen = lines[lineIndex];
                ModSoundUtil.playFadingSound((ServerLevel) level,
                        player.getX(), player.getY(), player.getZ(),
                        chosen, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F);
            }
        } else {
            player.getCooldowns().addCooldown(this, config.voicelineCooldownTicks());
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isSecondaryUseActive()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                 List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String ch = character();
        tooltip.add(Component.literal(ModBlocksCommon.capitalize(ch))
                .withStyle(Style.EMPTY.withColor(0xFFD700).withItalic(false)));

        int skinCount = PlushieBlockEntity.getSkinCount(ch);
        if (skinCount > 1) {
            CustomModelData cmd = stack.get(DataComponents.CUSTOM_MODEL_DATA);
            int skin = cmd != null ? cmd.value() : 0;
            tooltip.add(Component.literal("Skin " + (skin + 1) + " / " + skinCount)
                    .withStyle(Style.EMPTY.withColor(0x77DD77).withItalic(false)));
        }

        chris.murderdrones.PlushieCharacters.Character charEntry = chris.murderdrones.PlushieCharacters.get(ch);
        String flavour = charEntry != null ? charEntry.flavourText() : "A Murder Drones plushie.";
        tooltip.add(Component.literal(flavour)
                .withStyle(Style.EMPTY.withColor(0x55FFFF).withItalic(true)));
        tooltip.add(Component.literal("Right-click to squeeze  |  Sneak+place to put down")
                .withStyle(Style.EMPTY.withColor(0x888888).withItalic(true)));
    }
}
