package utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.util.Arrays;

@UtilityClass
public class PluginUtils {

  public boolean hasPlugin(String name) {
    return Arrays.stream(Bukkit.getPluginManager().getPlugins())
        .map(p -> p.getName().toLowerCase())
        .anyMatch(p -> p.contains(name.toLowerCase()));
  }

  public boolean hasProtocolLib() {
    try {
      if (hasPlugin("ProtocolLib")) {
        return true;
      }

      Class.forName("com.comphenix.protocol.ProtocolLibrary");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
