package plugin.registries;

import plugin.model.Barrier;

public class Permissions {
  public static final String ADMIN = "mysticalbarriers.admin";
  private static final String BYPASS_BARRIER = "mysticalbarriers.bypass.";

  public static String bypassPermission(Barrier barrier) {
    return BYPASS_BARRIER + barrier.getId();
  }
}
