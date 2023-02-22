package ml.empee.mysticalbarriers.listeners;

import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.ioc.RegisteredListener;
import ml.empee.mysticalbarriers.config.Config;
import ml.empee.mysticalbarriers.model.Barrier;
import ml.empee.mysticalbarriers.services.BarriersService;
import ml.empee.mysticalbarriers.utils.reflection.ReflectionUtils;
import ml.empee.mysticalbarriers.utils.reflection.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityMountEvent;

@RequiredArgsConstructor
public class BarrierIllegalActionsBlocker implements RegisteredListener, Bean {

  private final LingeringSplashPotionListener lingeringSplashPotionListener = new LingeringSplashPotionListener();
  private final JavaPlugin plugin;
  private final BarriersService barriersService;
  private final Config config;

  public void onStart() {
    if (ServerVersion.isGreaterThan(1, 9)) {
      Bukkit.getPluginManager().registerEvents(lingeringSplashPotionListener, plugin);
    }
  }

  public void onStop() {
    HandlerList.unregisterAll(lingeringSplashPotionListener);
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnPlayerMoveInsideBarrier(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(player)) {
      event.setCancelled(true);
      if (player.getVehicle() != null) {
        player.getVehicle().eject();
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnTeleportingInsideBarrier(PlayerTeleportEvent event) {
    Barrier barrier = barriersService.findBarrierAt(event.getTo());
    if (barrier != null && !barrier.isHiddenFor(event.getPlayer())) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void cancelOnChorusTeleportationNearBarrier(PlayerTeleportEvent event) {
    if (!config.getBlockChorusFruitTeleportation() || !event.getCause().name().equals("CHORUS_FRUIT")) {
      return;
    }

    List<Barrier> barriers = barriersService.findBarriersWithinRangeAt(event.getFrom(), 8);
    if (!barriers.isEmpty() && barriers.stream().anyMatch(b -> !b.isHiddenFor(event.getPlayer()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnEntityDamageInsideBarrier(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      Barrier barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
      if (barrier != null && !barrier.isHiddenFor((Player) event.getDamager())) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnProjectileDamageInsideBarrier(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Projectile)) {
      return;
    }

    Projectile projectile = (Projectile) event.getDamager();
    if (!(projectile.getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getEntity().getLocation());
    if (barrier == null || barrier.isHiddenFor((Player) projectile.getShooter())) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnPotionSplashInsideBarrier(PotionSplashEvent potionSplashEvent) {
    if (!(potionSplashEvent.getEntity().getShooter() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(potionSplashEvent.getEntity().getLocation());
    if (barrier != null && !barrier.isHiddenFor((Player) potionSplashEvent.getEntity().getShooter())) {
      potionSplashEvent.getAffectedEntities().forEach(entity -> {
        potionSplashEvent.setIntensity(entity, 0);
      });
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void cancelOnEntityMountInsideBarrier(EntityMountEvent event) {
    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    Barrier barrier = barriersService.findBarrierAt(event.getMount().getLocation());
    if (barrier != null && !barrier.isHiddenFor((Player) event.getEntity())) {
      event.setCancelled(true);
    }
  }

  private class LingeringSplashPotionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEvent(LingeringPotionSplashEvent event) {
      ThrownPotion potion = ReflectionUtils.getThrownPotion(event);
      if (!(potion.getShooter() instanceof Player)) {
        return;
      }

      Barrier barrier = barriersService.findBarrierAt(potion.getLocation());
      if (barrier != null && !barrier.isHiddenFor((Player) potion.getShooter())) {
        event.getAreaEffectCloud().remove();
      }
    }
  }

}
