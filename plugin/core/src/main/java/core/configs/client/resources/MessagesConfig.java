package core.configs.client.resources;

import io.github.empee.lightwire.annotations.LightWired;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import core.MysticalBarriers;
import utils.IReloadable;
import utils.Messenger;
import utils.files.JarUtils;
import utils.files.ResourceConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for loading and managing the messages configuration files.
 */

@LightWired
public class MessagesConfig implements IReloadable {

  // Valid: test.yml or test-it.yml
  private static final Pattern MESSAGE_FILE_FORMAT = Pattern.compile("\\w+(-(\\w+))?\\.yml");
  private static final String MESSAGES_PARENT_FOLDER = "messages";

  private final PluginConfig pluginConfig;
  private final MysticalBarriers plugin;
  private final Map<String, List<ResourceConfig>> languages = new HashMap<>();

  public MessagesConfig(PluginConfig pluginConfig, MysticalBarriers plugin) {
    this.pluginConfig = pluginConfig;
    this.plugin = plugin;

    loadMessages();
  }

  @Override
  public void reload() {
    languages.clear();
    loadMessages();
  }

  private void loadMessages() {
    var shouldReplaceExistingMessage = plugin.isDevelop();
    var messages = findDefaultMessages().stream()
        .map(path -> new ResourceConfig(plugin, path, shouldReplaceExistingMessage, 2))
        .toList();

    for (ResourceConfig message : messages) {
      var lang = extractLangFrom(message.getFile().getName());
      if (lang == null) {
        lang = pluginConfig.getLang();
      }

      languages.computeIfAbsent(lang, k -> new ArrayList<>()).add(message);
    }

    Messenger.log("Loaded messages for {} languages", languages.size());
  }

  @SneakyThrows
  private List<String> findDefaultMessages() {
    var result = new ArrayList<String>();

    try (var jar = new JarFile(JarUtils.getJar())) {
      var resources = JarUtils.getContentFromJar(jar, MESSAGES_PARENT_FOLDER);
      for (JarEntry message : resources) {
        result.add(message.getName());
      }
    }

    return result;
  }

  @Nullable
  private String extractLangFrom(String input) {
    Matcher matcher = MESSAGE_FILE_FORMAT.matcher(input);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid message file name: " + input);
    }

    return matcher.group(2);
  }

  private List<ResourceConfig> getLanguage(String language) {
    var messages = languages.get(language);
    if (messages == null) {
      Messenger.error("Can't find messages for language '{}', falling back to default lang", language);
      messages = languages.get(pluginConfig.getLang());
    }

    return messages;
  }

  @Nullable
  public String get(String key) {
    return get(pluginConfig.getLang(), key, Collections.emptyMap());
  }

  @Nullable
  public String get(String key, Map<String, Object> args) {
    return get(pluginConfig.getLang(), key, args);
  }

  @Nullable
  public String get(String language, String key, Map<String, Object> args) {
    for (ResourceConfig messages : getLanguage(language)) {
      var message = messages.getConfig().getString(key);
      if (message == null) {
        continue;
      }

      return replacePlaceholders(message, args);
    }

    return null;
  }

  @Nullable
  public String get(String language, String key) {
    return get(language, key, Collections.emptyMap());
  }

  /**
   * Replace all the placeholders in the message with the values provided in the args map.
   * Placeholders are defined as {key} in the message string.
   */
  private static String replacePlaceholders(String message, Map<String, Object> args) {
    for (var arg : args.entrySet()) {
      message = message.replace("{" + arg.getKey() + "}", arg.getValue().toString());
    }

    return message;
  }
}
