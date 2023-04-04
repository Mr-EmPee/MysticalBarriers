package ml.empee.mysticalbarriers.services;

import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.model.entities.Barrier;
import ml.empee.mysticalbarriers.repositories.BarrierRepository;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer to perform generic operation between barriers
 */

@RequiredArgsConstructor
public class BarrierService implements Bean {

  private final BarrierRepository repository;

  public void save(Barrier barrier) {
    repository.save(barrier);
  }

  public void delete(Barrier barrier) {
    repository.delete(barrier);
  }

  public Set<Barrier> findAll() {
    return repository.findAll();
  }

  public List<Barrier> findBarrierNear(Location location) {
    return findAll().stream()
        .filter(b -> b.isNear(location))
        .toList();
  }

  public Optional<Barrier> findBarrierByBlock(Block block) {
    return findAll().stream()
            .filter(b -> b.isBarrierBlock(block))
            .findFirst();
  }

  public Optional<Barrier> findById(String id) {
    return repository.findAll().stream()
        .filter(b -> b.getId().equals(id))
        .findFirst();
  }

}
