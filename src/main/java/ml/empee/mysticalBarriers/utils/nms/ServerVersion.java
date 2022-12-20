package ml.empee.mysticalBarriers.utils.nms;

import org.bukkit.Bukkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerVersion {

    public static final String VALUE = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static final int[] VERSION;

    static {
        String[] rawVersioning = Bukkit.getBukkitVersion().split("-")[0].split("\\.");

        VERSION = new int[rawVersioning.length];

        for(int i=0; i<VERSION.length; i++) {
            VERSION[i] = Integer.parseInt(rawVersioning[i]);
        }
    }

    /**
     * @return <b>true</b> if the server's version is greater or equals
     */
    public static boolean isGreaterThan(int... v) {

        for(int i=0; i<v.length; i++) {
            int work = v[i] - VERSION[i];

            if(work > 0) {
                return false;
            } else if(work < 0) {
                break;
            }

        }

        return true;

    }

    /**
     * @return <b>true</b> if the server's version is lower
     */
    public static boolean isLowerThan(int... v) {
        return !isGreaterThan(v);
    }

}
