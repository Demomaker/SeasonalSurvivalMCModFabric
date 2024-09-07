package net.demomaker.seasonalsurvival;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class SeasonalSurvivalHelper {
  private static final int BLOCKS_IN_A_CHUNK = 16;
  private static final int MAXIMUM_AMOUNT_OF_CHUNKS_THAT_CAN_RENDER = 32;

  public static boolean isPlayerSheltered(ServerWorld world, ServerPlayerEntity player) {
    return isNearHeatOrLightSource(world, player);
  }

  private static boolean isNearHeatOrLightSource(ServerWorld world, PlayerEntity player) {
    BlockPos playerPos = player.getBlockPos();
    int blockLightLevel = world.getLightLevel(LightType.BLOCK, playerPos);
    return blockLightLevel > 0;
  }

  private static boolean isInsideBuilding(ServerWorld world, PlayerEntity player) {
    return isInShelter(player);
  }

  public static boolean isInShelter(PlayerEntity player) {
    BlockPos pos = player.getBlockPos();
    World world = player.getEntityWorld();
    int radius = BLOCKS_IN_A_CHUNK * MAXIMUM_AMOUNT_OF_CHUNKS_THAT_CAN_RENDER; // Adjust this as needed

    // Check blocks around the player
    for (int x = -radius; x <= radius; x++) {
      for (int y = -radius; y <= radius; y++) {
        for (int z = -radius; z <= radius; z++) {
          BlockPos offsetPos = pos.add(x, y, z);
          if (isShelterBlock(world, offsetPos)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean isShelterBlock(World world, BlockPos pos) {
    // Check for walls around the player
    boolean hasWallNorth = !world.getBlockState(pos.add(0, 0, -1)).isAir();
    boolean hasWallSouth = !world.getBlockState(pos.add(0, 0, 1)).isAir();
    boolean hasWallEast = !world.getBlockState(pos.add(1, 0, 0)).isAir();
    boolean hasWallWest = !world.getBlockState(pos.add(-1, 0, 0)).isAir();
    boolean hasRoof = !world.getBlockState(pos.add(0, 1, 0)).isAir();

    return hasWallNorth && hasWallSouth && hasWallEast && hasWallWest && hasRoof;
  }

}
