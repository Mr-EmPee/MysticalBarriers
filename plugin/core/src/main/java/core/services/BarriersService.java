package core.services;

import core.model.Barrier;
import core.registries.Permissions;
import core.repositories.nitrite.BarriersRepository;
import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import utils.regions.CubicRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LightWired
@RequiredArgsConstructor
public class BarriersService {

  private final BarriersRepository barriersRepository;

  public void updateBarrierRange(Barrier barrier, int range) {
    if (range <= 0) throw new IllegalArgumentException("Range must be greater then 0");

    barrier.setActivationRange(range);
    barriersRepository.update(barrier);
  }

  public void updateBarrierFillBlock(Barrier barrier, BlockData data) {
    barrier.setFillBlock(data);
    barriersRepository.update(barrier);
  }

  public void removeBarrierStructure(Barrier barrier) {
    if (barrier.getFillBlock() == null) {
      barrier.setFillBlock(Barrier.DEFAULT_FILL_BLOCK);
    }

    barrier.setStructure(null);
    barriersRepository.update(barrier);
  }

  public void updateBarrierStructure(Barrier barrier) {
    List<Barrier.Block> structure = barrier.getStructure();
    for (var barrierBlock : structure) {
      var newBarrierBlock = barrierBlock.getLocation().getBlock().getBlockData();
      barrierBlock.setData(newBarrierBlock);
    }

    barrier.setStructure(structure);
    barriersRepository.update(barrier);

    for (var barrierBlock : structure) {
      barrierBlock.getLocation().getBlock().setType(Material.AIR);
    }
  }

  public void updateBarrierStructureMask(Barrier barrier) {
    List<Barrier.Block> barrierBlocks = new ArrayList<>();

    barrier.getRegion().forEach(loc -> {
      var serverBlock = loc.getBlock();
      if (serverBlock.getType() == Barrier.STRUCTURE_MASK) {
        barrierBlocks.add(Barrier.Block.of(loc, Barrier.STRUCTURE_DEFAULT_BLOCK));
        serverBlock.setType(Material.AIR);
      }
    });

    barrier.setStructure(barrierBlocks);
    barriersRepository.update(barrier);
  }

  public Barrier createBarrier(String id, CubicRegion region) {
    var result = Barrier.builder()
        .id(id)
        .region(region)
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
