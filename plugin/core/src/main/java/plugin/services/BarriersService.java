package plugin.services;

import io.github.empee.lightwire.annotations.LightWired;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import plugin.model.Barrier;
import plugin.registries.Permissions;
import plugin.repositories.nitrite.BarriersRepository;

import java.util.List;
import java.util.Optional;

@LightWired
@RequiredArgsConstructor
public class BarriersService {

  private final BarriersRepository barriersRepository;

  public Optional<Barrier> findById(String id) {
    return findAll().stream()
        .filter(b -> b.getId().equalsIgnoreCase(id))
        .findFirst();
  }

  public void save(Barrier barrier) {
    barriersRepository.save(barrier);
  }

  public void delete(Barrier barrier) {
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
