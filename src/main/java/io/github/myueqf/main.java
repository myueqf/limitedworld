package io.github.myueqf;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.registry.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Random;

public class main implements ModInitializer {
    public static ArrayList<ServerWorld> world = new ArrayList<>();
    public static boolean isExiteGateway = false;
    private static final Random random = new Random();
    private static int tickCounter = 0;

    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier("xwx", "old_world"), oldworld.CODEC);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ArrayList<ServerWorld> serverWorlds = new ArrayList<>();
            server.getWorlds().forEach(serverWorlds::add);
            setWorld(serverWorlds);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            // 每40tick检测一下
            if (tickCounter >= 40) {
                tickCounter = 0;
                checkAndDestroyNetherPortals(server);
            }
        });
    }

    public static void setWorld(ArrayList<ServerWorld> value) {
        world = value;
    }

    public static boolean getIsAntFarmInf() {
        return world.stream()
                .allMatch(
                        world -> world.getChunkManager().getChunkGenerator() instanceof oldworld
                );
    }

    public static void setIsExitGateway(boolean value) {
        isExiteGateway = value;
    }

    public static boolean getIsExitGateway() {
        return isExiteGateway;
    }

    // 拆除超过范围的传送门
    private static void checkAndDestroyNetherPortals(net.minecraft.server.MinecraftServer server) {
        for (ServerWorld serverWorld : world) {
            if (serverWorld.getRegistryKey() == RegistryKey.of(RegistryKeys.WORLD, new Identifier("the_nether"))) {
                server.getPlayerManager().getPlayerList().forEach(player -> {
                    if (player.getWorld() == serverWorld) {
                        BlockPos playerPos = player.getBlockPos();
                        int radius = 1; // 检测玩家身边的传送门
                        for (int x = -radius; x <= radius; x++) {
                            for (int y = -radius; y <= radius; y++) {
                                for (int z = -radius; z <= radius; z++) {
                                    BlockPos pos = playerPos.add(x, y, z);
                                    // 检查传送门
                                    if (serverWorld.getBlockState(pos).getBlock() instanceof NetherPortalBlock) {
                                        // 检查传送门位置
                                        if (Math.abs(pos.getX()) > 32 || Math.abs(pos.getZ()) > 32) {
                                            // 拆～掉～
                                            serverWorld.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}
