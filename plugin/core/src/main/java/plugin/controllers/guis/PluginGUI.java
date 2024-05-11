package plugin.controllers.guis;

import java.util.HashMap;
import java.util.Map;

public abstract class PluginGUI {

  private static final Map<Class<?>, PluginGUI> guis = new HashMap<>();
  public static <T> T get(Class<T> clazz) {
    return (T) guis.get(clazz);
  }

  public PluginGUI() {
    guis.put(getClass(), this);
  }

}
