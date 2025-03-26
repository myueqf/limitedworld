package io.github.myueqf;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class oldworld extends NoiseChunkGenerator  {

    public static final Codec<oldworld> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(oldworld::getBiomeSource),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(oldworld::getSettings))
            .apply(instance, instance.stable(oldworld::new)));

    private static final int maxChunkDistFromAxis = 8; // 区块范围半径
    private static final int structureChunkDistanceFlexibility = 2;

    public oldworld(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource, settings);
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    private boolean isChunkInBounds(ChunkPos pos) {
        return Math.abs(pos.x) <= maxChunkDistFromAxis && Math.abs(pos.z) <= maxChunkDistFromAxis;
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {
        if (isChunkInBounds(chunk.getPos()))
            super.buildSurface(region, structures, noiseConfig, chunk);
    }

    private static void fillChunkWithBARRIER(Chunk chunk) {
        final BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = chunk.getBottomY(); y < chunk.getHeight() ; y++) {
                    chunk.setBlockState(mutable.set(x, y, z), Blocks.BARRIER.getDefaultState(), false);
                }
            }
        }
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
        if (isChunkInBounds(chunk.getPos()))
            super.carve(chunkRegion, seed, noiseConfig, biomeAccess, structureAccessor, chunk, carverStep);
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        if (isChunkInBounds(chunk.getPos()))
            return super.populateNoise(executor, blender, noiseConfig, structureAccessor, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        if (isChunkInBounds(chunk.getPos()))
            super.generateFeatures(world, chunk, structureAccessor);
        else if (Math.abs(chunk.getPos().x) <= maxChunkDistFromAxis + 1 && Math.abs(chunk.getPos().z) <= maxChunkDistFromAxis + 1) {
            fillChunkWithBARRIER(chunk);
        }
    }
}