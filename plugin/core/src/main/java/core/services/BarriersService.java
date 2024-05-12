package core.services;

import core.model.Barrier;
import core.registries.Permissions;
import core.repositories.nitrite.BarriersRepository;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import utils.regions.CubicRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LightWired
@RequiredArgsConstructor
public class BarriersService {

  private final BarriersRepository barriersRepository;

  public Barrier updateBarrierRange(String barrierId, int range) {
    Validate.isTrue(range > 0);

    var barrier = findById(barrierId).orElseThrow();

    barrier.setActivationRange(range);
    barriersRepository.update(barrier);

    return barrier;
  }

  public Barrier updateBarrierWall(String barrierId) {
    var barrier = findById(barrierId).orElseThrow();

    List<Barrier.Block> barrierBlocks = barrier.getBarrierBlocks();
    for (var barrierBlock : barrierBlocks) {
      var newBarrierBlock = barrierBlock.getLocation().getBlock().getBlockData();
      barrierBlock.setData(newBarrierBlock);
    }

    barrier.setBarrierBlocks(barrierBlocks);
    barriersRepository.update(barrier);

    for (var barrierBlock : barrierBlocks) {
      barrierBlock.getLocation().getBlock().setType(Material.AIR);
    }

    return barrier;
  }

  public Barrier createBarrier(String id, CubicRegion region) {
    List<Barrier.Block> barrierBlocks = new ArrayList<>();

    region.forEach(loc -> {
      var serverBlock = loc.getBlock();
      if (serverBlock.getType() == Barrier.MASK_BLOCK) {
        barrierBlocks.add(Barrier.Block.of(loc, Barrier.DEFAULT_BLOCK));
        serverBlock.setType(Material.AIR);
      }
    });

    var result = Barrier.builder()
        .id(id)
        .region(region)
        .barrierBlocks(barrierBlocks)
        .build();

    barriersRepository.save(result);

    return result;
  }

  public Optional<Barrier> findById(String id) {
    return findAll().stream()
        .filter(b -> b.getId().equalsIgnoreCase(id))
        .findFirst();
  }

  public void delete(String barrierId) {
    var barrier = findById(barrierId).orElseThrow();
    barriersRepository.delete(barrier);
  }

  public List<Barrier> findAll() {
    return barriersRepository.findAll();
  }

  public boolean isHidden(Barrier barrier, Player player) {
    return player.hasPermission(Permissions.bypassPermission(barrier));
  }

  public Optional<Barrier> findBarrierAt(Location location) {
    return findAll().stream()
        .filter(b -> b.isWithin(location))
        .findFirst();
  }

}
