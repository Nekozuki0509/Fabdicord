package fabdicord.fabdicord.config;

import com.mojang.datafixers.util.Pair;

public class ModConfigs {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;


    public static String SERVER_NAME;


    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of( "fabdicordconfig").provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("SERVER_NAME", "s"), "String", "it's this server name");
    }

    private static void assignConfigs() {
        SERVER_NAME = CONFIG.getOrDefault("SERVER_NAME", "s");

        System.out.println("All " + configs.getConfigsList().size() + " have been set properly");
    }
}