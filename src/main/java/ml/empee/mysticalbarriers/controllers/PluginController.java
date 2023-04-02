package ml.empee.mysticalbarriers.controllers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import lombok.RequiredArgsConstructor;
import ml.empee.ioc.Bean;
import ml.empee.mysticalbarriers.config.BarriersConfig;
import ml.empee.mysticalbarriers.config.CommandsConfig;
import ml.empee.mysticalbarriers.constants.ItemRegistry;
import ml.empee.mysticalbarriers.constants.Permissions;
import ml.empee.mysticalbarriers.repositories.BarrierRepository;
import ml.empee.mysticalbarriers.utils.Logger;
import ml.empee.mysticalbarriers.utils.Translator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * Plugin related commands
 */

@RequiredArgsConstructor
public class PluginController implements Bean {

  private final CommandsConfig commandsConfig;
  private final BarrierRepository barrierRepository;
  private final BarriersConfig barriersConfig;

  @Override
  public void onStart() {
    commandsConfig.register(this);
  }

  @CommandMethod("mb wand")
  @CommandPermission(Permissions.ADMIN)
  public void giveWand(Player sender) {
    boolean wandReceived = sender.getInventory().addItem(
        ItemRegistry.SELECTION_WAND.build()
    ).isEmpty();

    if (!wandReceived) {
      Logger.log(sender, "&cYour inventory is full!");
      return;
    }

    Logger.log(sender, "&7You have received the selection tool");
  }

  @CommandMethod("mb reload")
  @CommandPermission(Permissions.ADMIN)
  public void reload(CommandSender sender) throws IOException {
    barriersConfig.reload();
    barrierRepository.loadBarriers();
    Translator.reload();

    Logger.log(sender, "&7The plugin has been reloaded");
  }

}
