package core.configs.server;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import io.github.empee.lightwire.annotations.LightWired;
import utils.Messenger;

import java.util.List;

@LightWired
public class PacketListenersConfig {

  private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

  public PacketListenersConfig(List<PacketAdapter> listeners) {
    for (var listener : listeners) {
      PROTOCOL_MANAGER.addPacketListener(listener);
    }

    Messenger.log("Registered {} packet listeners", listeners.size());
  }

}
