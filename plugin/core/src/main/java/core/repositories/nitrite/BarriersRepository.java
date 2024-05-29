package core.repositories.nitrite;

import core.configs.server.db.nitrite.NitriteConfig;
import core.model.Barrier;
import io.github.empee.lightwire.annotations.LightWired;
import org.dizitart.no2.repository.ObjectRepository;

import java.util.List;

@LightWired
public class BarriersRepository {

  private final ObjectRepository<Barrier> repository;

  public BarriersRepository(NitriteConfig config) {
    this.repository = config.getRepository(Barrier.class);
  }

  public void save(Barrier barrier) {
    repository.insert(barrier);
  }

  public void update(Barrier barrier) {
    repository.update(barrier);
  }

  public List<Barrier> findAll() {
    return repository.find().toList();
  }

  public void delete(Barrier barrier) {
    repository.remove(barrier);
  }

}
