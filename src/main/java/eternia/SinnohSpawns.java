package eternia;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.RandomHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.listener.RepelHandler;
import eternia.comm.Zone;
import eternia.commands.Base;
import eternia.configuration.ConfigManager;
import eternia.eventlisteners.PixelmonListener;
import eternia.managers.PixelmonManager;
import eternia.managers.SpawningManager;
import eternia.managers.ZoneManager;
import eternia.utilities.WorldInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.slf4j.Logger;


import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Plugin(id = SinnohSpawns.ID,
        name = SinnohSpawns.NAME,
        authors = SinnohSpawns.AUTHORS,
        description = SinnohSpawns.DESCRIPTION,
        version = SinnohSpawns.VERSION,
        dependencies = @Dependency(id = Pixelmon.MODID, version = Pixelmon.VERSION))

public class SinnohSpawns {

    public static final String ID = "eternia";
    public static final String NAME = "SinnohSpawns";
    public static final String VERSION = "2.1.0";
    public static final String DESCRIPTION = "Completely configurable spawning system for Pixelmon Reforged";

    private static SinnohSpawns instance;
    private static Map<Player, AtomicInteger> steps = Maps.newHashMap();

    @Inject private Logger logger;
    @Inject private PluginContainer container;
    @Inject @ConfigDir(sharedRoot = false) private Path dir;

    @Listener
    public void onPreInit(GamePreInitializationEvent e) {
        instance = this;
        ConfigManager.setup(dir);
    }

    @Listener
    public void onInit(GameInitializationEvent e) {
        Sponge.getCommandManager().register(this, Base.build(), "as", "spawning", "sinnohspawning");
        Pixelmon.EVENT_BUS.register(new PixelmonListener());
    }

    @Listener
    public void onStart(GameStartedServerEvent e) {
        WorldInfo.init();
        ZoneManager.init();
        Task.builder()
                .execute(() -> {
                    for (Player player : Sponge.getServer().getOnlinePlayers()) {
                        if (!RepelHandler.hasRepel((EntityPlayerMP) player)) {
                            for (Zone zone : ZoneManager.getZonesNearby(player.getPosition(), 32)) {
                                for (int i = 0; i < (int) (Math.random() * 3) + 1; i++) {
                                    if (zone.spawningAvailable()) {
                                        zone.doSpawn();
                                    }

                                }
                            }
                        }
                    }
                    ZoneManager.getSpawned().clear();
                })
                .interval(4, TimeUnit.SECONDS)
                .submit(instance);
    }

    @Listener
    public void onReload(GameReloadEvent e) {
        ConfigManager.load();
        ZoneManager.init();
        logger.info("SinnohSpawns has been reloaded!");
    }

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Join event, @Root Player player) {
        steps.put(player, new AtomicInteger(RandomHelper.getRandomNumberBetween(5, 8)));
    }

    @Listener
    public void onPlayerLogout(ClientConnectionEvent.Disconnect event, @Root Player player) {
        steps.remove(player);
    }

    @Listener
    public void onEntityMove(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player))
            return;
        Player player = (Player) event.getTargetEntity();
        if (atValidLocation(player) && !event.getFromTransform().getPosition().toInt().equals(event.getToTransform().getPosition().toInt())) {
            if (BattleRegistry.getBattle((EntityPlayer) player) == null && !RepelHandler.hasRepel((EntityPlayerMP) player)) {
                Set<Zone> zones = ZoneManager.getZonesAtPosition(player.getPosition());
                if (!zones.isEmpty()) {
                    if (steps.get(player).decrementAndGet() > 0)
                        return;
                    steps.get(player).set(RandomHelper.getRandomNumberBetween(5, 8));
                    if (RandomHelper.getRandomChance(.85))
                        return;
                    Zone zone = Lists.newArrayList(zones).get((int) (Math.random() * zones.size()));
                    String poke = new SpawningManager(zone).choosePokemon();
                    if (poke == null)
                        return;
                    EntityPixelmon entity = new PixelmonManager(
                            poke,
                            player.getPosition().getFloorX(),
                            player.getPosition().getFloorY(),
                            player.getPosition().getFloorZ()
                    ).doSpawn();
                    WildPixelmonParticipant wildParticipant = new WildPixelmonParticipant(entity);
                    EntityPixelmon player1FirstPokemon = Pixelmon.storageManager
                            .getParty(player.getUniqueId())
                            .getAndSendOutFirstAblePokemon((Entity) player);
                    PlayerParticipant participant1 = new PlayerParticipant((EntityPlayerMP) player, player1FirstPokemon);
                    BattleRegistry.startBattle(wildParticipant, participant1);
                }
            }
        }
    }

    private boolean atValidLocation(Player player) {
        if (Sets.newHashSet(BlockTypes.TALLGRASS, BlockTypes.DOUBLE_PLANT, BlockTypes.WATER).contains(player.getLocation().getBlockType()))
            return true;
        if (Sets.newHashSet(Blocks.STONE.getStateFromMeta(5),Blocks.WOOL.getStateFromMeta(7)).contains(player.getLocation().sub(0,1,0).getBlock()))
            return true;
        if (Sets.newHashSet(BlockTypes.SNOW, BlockTypes.SNOW_LAYER).contains(player.getLocation().sub(0,1,0).getBlockType()))
            return true;
        return false;
    }

    public static SinnohSpawns getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static PluginContainer getContainer() {
        return instance.container;
    }
}
