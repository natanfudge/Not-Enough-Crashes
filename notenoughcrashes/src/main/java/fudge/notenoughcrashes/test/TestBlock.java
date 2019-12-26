//package fudge.notenoughcrashes.test;
//
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.Material;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemGroup;
//import net.minecraft.util.ActionResult;
//import net.minecraft.util.Hand;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.registry.Registry;
//import net.minecraft.world.World;
//
//
//public class TestBlock extends Block {
//    public static final Block EXAMPLE_BLOCK = new TestBlock();
//
//    public TestBlock() {
//        super(Settings.of(Material.ANVIL));
//    }
//
//    @Override
//    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
//        throw new NullPointerException();
//    }
//
//    public static void init(){
//        Registry.register(Registry.BLOCK, new Identifier("tutorial", "example_block"), EXAMPLE_BLOCK);
//        Registry.register(Registry.ITEM, new Identifier("tutorial", "example_block"),
//                        new BlockItem(EXAMPLE_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
//    }
//}