package ml.empee.mysticalbarriers.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.config.CommandsConfig;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.utils.Logger;
import org.bukkit.command.CommandSender;

import java.io.IOException;

/**
 * Plugin related commands
 */

@RequiredArgsConstructor
public class PluginController implements Bean {

  private final CommandsConfig commandsConfig;
  private final BarriersConfig barriersConfig;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  @CommandMethod("mb reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) throws IOException {
    barriersConfig.reload();
    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
