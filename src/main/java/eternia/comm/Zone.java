package eternia.comm;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import eternia.managers.SpawningManager;
import eternia.managers.ZoneManager;
import eternia.utilities.WorldInfo;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static eternia.configuration.ConfigManager.getZonesNode;

public class Zone {
    private String id;
    private int radius;
    private Vector3d position;
    private int maxSpawns;
    private String type;
    private Map<String, Integer> pokemon = Maps.newHashMap();
    private Set<UUID> spawned = Sets.newConcurrentHashSet();

    public Zone(String id, int radius, int maxSpawns, String type, Vector3d position) {
        this.id = id;
        this.radius = radius;
        this.position = position;
        this.maxSpawns = maxSpawns;
        this.type = type;
        getZonesNode(id, "Pokemon").getChildrenMap().forEach((key, value) -> {
            pokemon.put(key.toString(), value.getInt());
        });
    }

    public String getId() {
        return id;
    }

    public int getRadius() {
        return radius;
    }

    public Vector3i getPosition() {
        return position.toInt();
    }

    public AABB getBoundingBox() {
        return new AABB(position.sub(radius, radius, radius), position.add(radius, radius, radius));
    }

    public int getMaxSpawns() {
        return maxSpawns;
    }

    public String getType() {
        return type;
    }

    public Map<String, Integer> getPokemon() {
        return pokemon;
    }

    public boolean spawningAvailable() {
        if (ZoneManager.getSpawned().contains(this))
            return false;
        spawned.removeIf(id -> !WorldInfo.WORLD.getEntity(id).isPresent());
        return spawned.size() < getMaxSpawns();
    }

    public List<Vector3i> getSpawnPositions() {
        List<Vector3i> list = Lists.newArrayList();
        for (int x = getPosition().getX() - radius; x <= getPosition().getX() + radius; x++) {
            for (int z = getPosition().getZ() - radius; z <= getPosition().getZ() + radius; z++) {
                Vector3i pos = new Vector3i(x, getPosition().getY(), z);
                if (canSpawnAt(pos)) {
                    list.add(pos);
                }
            }
        }
        return list;
    }

    private static final Set<BlockType> VALID_CONTAINED_BLOCKS = Sets.newHashSet(BlockTypes.GRASS, BlockTypes.TALLGRASS);
    public boolean canSpawnAt(Vector3i position) {
        return VALID_CONTAINED_BLOCKS.contains(WorldInfo.WORLD.getBlockType(position));
    }

    public EntityPixelmon doSpawn() {
        ZoneManager.getSpawned().add(this);
        EntityPixelmon pixelmon = new SpawningManager(this).spawnPokemon();
        if (pixelmon != null) {
            spawned.add(pixelmon.getUniqueID());
        }
        return pixelmon;
    }

    public Set<Vector3i> getContainedChunks() {
        return WorldInfo.getChunksIn(getBoundingBox());
    }

    @Override
    public String toString() {
        return id;
    }
}

