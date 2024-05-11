package plugin.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * An exception thrown by the plugin
 */

@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class PluginException extends RuntimeException {

  @Getter
  private final String id;
  private Map<String, Object> arguments = Collections.emptyMap();

  public Map<String, Object> getArguments() {
    return Collections.unmodifiableMap(arguments);
  }

}
