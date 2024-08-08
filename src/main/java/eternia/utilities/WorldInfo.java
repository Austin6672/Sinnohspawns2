package eternia.utilities;

import java.util.Set;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

public class WorldInfo {

    public static String WORLD_NAME;
    public static World WORLD;
    public static UUID WORLD_UUID;

    public static void init() {
        WORLD_NAME = Sponge.getServer().getDefaultWorldName();
        WORLD = Sponge.getServer().getWorld(WORLD_NAME).get();
        WORLD_UUID = WORLD.getUniqueId();
    }

    public static boolean isDay() {
        return (WORLD.getProperties().getWorldTime() % 24000) < 12000;
    }

    public static Set<Vector3i> getChunksIn(AABB boundingBox) {
        Vector3i min = getChunkPos(boundingBox.getMin());
        Vector3i max = getChunkPos(boundingBox.getMax());
        Set<Vector3i> chunks = Sets.newHashSet();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                chunks.add(new Vector3i(x, 0, z));
            }
        }
        return chunks;
    }

    private static Vector3i getChunkPos(Vector3d worldPos) {
        return Sponge.getServer().getChunkLayout().forceToChunk(worldPos.toInt());
    }
}

