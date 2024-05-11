package plugin.repositories.nitrite;

import io.github.empee.lightwire.annotations.LightWired;
import org.dizitart.no2.repository.ObjectRepository;
import plugin.configs.server.db.nitrite.NitriteConfig;
import plugin.model.Barrier;

import java.util.List;

@LightWired
public class BarriersRepository {

  private final ObjectRepository<Barrier> repository;

  public BarriersRepository(NitriteConfig config) {
    this.repository = config.getRepository(Barrier.class);
  }

  public void save(Barrier barrier) {
    repository.update(barrier, true);
  }

  public List<Barrier> findAll() {
    return repository.find().toList();
  }

  public void delete(Barrier barrier) {
    repository.remove(barrier);
  }

}
