package core.configs.server.db.nitrite.migrations;

import core.model.Barrier;
import org.dizitart.no2.migration.InstructionSet;
import org.dizitart.no2.migration.Migration;

public class SchemaUpdateV2 extends Migration {

  public SchemaUpdateV2() {
    super(1, 2);
  }

  @Override
  public void migrate(InstructionSet instructions) {
    updateBarrierRepository(instructions);
  }

  private void updateBarrierRepository(InstructionSet instructions) {
    var repository = instructions.forRepository(Barrier.class);
    repository.renameField("barrierBlocks", "structure");
  }

}
