package fudge.notenoughcrashes.forge;

import fudge.notenoughcrashes.NotEnoughCrashes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(NotEnoughCrashes.MOD_ID)
public class NotEnoughCrashesForge {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NotEnoughCrashes.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NotEnoughCrashes.MOD_ID);
    private static final Block EXAMPLE_BLOCK_ITSELF = new Block(AbstractBlock.Settings.of(Material.METAL)){
        @Override
        public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
            throw new NullPointerException();
        }
    };
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("crash_block",() -> EXAMPLE_BLOCK_ITSELF);

    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("crash_block",() -> new BlockItem(EXAMPLE_BLOCK_ITSELF, new Item.Settings()));
    public NotEnoughCrashesForge() {
        NotEnoughCrashes.initialize();

//        new Thread(() -> {
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            throw new NullPointerException();
//        }).start();
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
