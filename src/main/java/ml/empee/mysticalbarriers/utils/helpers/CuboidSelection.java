package ml.empee.mysticalbarriers.utils.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

/**
 * An in-game selection
 */

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "empty")
public class CuboidSelection {
  private Location start;
  private Location end;

  public boolean isValid() {
    return start != null && end != null && start.getWorld().equals(end.getWorld());
  }
}
