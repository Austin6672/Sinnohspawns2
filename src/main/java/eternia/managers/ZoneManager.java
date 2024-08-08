package eternia.managers;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import eternia.comm.Zone;
import eternia.configuration.ConfigManager;
import eternia.utilities.WorldInfo;


import java.util.Map;
import java.util.Set;

import static eternia.configuration.ConfigManager.getZoneRoot;

public class ZoneManager {
    private static Map<Vector3i, Set<Zone>> zoneMap = Maps.newHashMap();
    private static Set<Zone> spawned = Sets.newHashSet();

    public static void init() {
        getZoneRoot().getChildrenMap().forEach((key, value) -> {
            String id = key.toString();
            Zone zone = new Zone(
                    id,
                    ConfigManager.getZonesNode(id, "Zone", "Radius").getInt(),
                    ConfigManager.getZonesNode(id, "Zone", "Max-Spawns").getInt(),
                    ConfigManager.getZonesNode(id, "Zone", "SpawnPos").getString(),
                    new Vector3d(
                            ConfigManager.getZonesNode(id, "Zone", "X").getInt(),
                            ConfigManager.getZonesNode(id, "Zone", "Y").getInt(),
                            ConfigManager.getZonesNode(id, "Zone", "Z").getInt()
                    )
            );
            for (Vector3i chunk : zone.getContainedChunks()) {
                zoneMap.computeIfAbsent(chunk, k -> Sets.newHashSet()).add(zone);
            }
        });
    }

    public static Set<Zone> getZonesAtPosition(Vector3d position) {
        return zoneMap.getOrDefault(Sponge.getServer().getChunkLayout().forceToChunk(position.toInt()), Sets.newHashSet());
    }

    public static Set<Zone> getZonesNearby(Vector3d position, int radius) {
        Set<Vector3i> chunks = WorldInfo.getChunksIn(new AABB(position.sub(radius, radius, radius), position.add(radius, radius, radius)));
        Set<Zone> zones = Sets.newHashSet();
        for (Vector3i chunk : chunks) {
            if (zoneMap.containsKey(chunk)) {
                zones.addAll(zoneMap.get(chunk));
            }
        }
        return zones;
    }

    public static Set<Zone> getSpawned() {
        return spawned;
    }
}