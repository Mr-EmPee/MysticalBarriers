package ml.empee.mysticalBarriers.utils.helpers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import java.util.ArrayList;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProtocolLibHelper {

  private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(ProtocolLibHelper.class);
  private final ArrayList<PacketListener> listeners = new ArrayList<>();

  public void registerListener(PacketType packetType, Consumer<PacketEvent> consumer) {
    PacketAdapter adapter = new PacketAdapter(plugin, packetType) {
      @Override
      public void onPacketSending(PacketEvent event) {
        consumer.accept(event);
      }

      @Override
      public void onPacketReceiving(PacketEvent event) {
        consumer.accept(event);
      }
    };

    listeners.add(adapter);
    protocolManager.addPacketListener(adapter);
  }

  public void unregisterAll() {
    for (PacketListener listener : listeners) {
      protocolManager.removePacketListener(listener);
    }
  }

}
