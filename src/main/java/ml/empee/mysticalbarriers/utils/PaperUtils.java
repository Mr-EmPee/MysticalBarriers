package ml.empee.mysticalbarriers.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utilities for falling back from paper
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaperUtils {
  public static final boolean IS_RUNNING_PAPER;

  static {
    IS_RUNNING_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
  }

  private static boolean hasClass(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
