package com.xiaohe.nacos.api.ability.constant;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;



public enum AbilityKey {


    SERVER_SUPPORT_PERSISTENT_INSTANCE_BY_GRPC("supportPersistentInstanceByGrpc",
            "support persistent instance by grpc", AbilityMode.SERVER),


    SERVER_TEST_1("test_1", "just for junit test", AbilityMode.SERVER),


    SERVER_TEST_2("test_2", "just for junit test", AbilityMode.SERVER),


    SDK_CLIENT_TEST_1("test_1", "just for junit test", AbilityMode.SDK_CLIENT),


    CLUSTER_CLIENT_TEST_1("test_1", "just for junit test", AbilityMode.CLUSTER_CLIENT);


    private final String keyName;


    private final String description;


    private final AbilityMode mode;

    AbilityKey(String keyName, String description, AbilityMode mode) {
        this.keyName = keyName;
        this.description = description;
        this.mode = mode;
    }

    public String getName() {
        return keyName;
    }

    public String getDescription() {
        return description;
    }

    public AbilityMode getMode() {
        return mode;
    }


    private static final Map<AbilityMode, Map<String, AbilityKey>> ALL_ABILITIES = new HashMap<>();


    public static Collection<AbilityKey> getAllValues(AbilityMode mode) {
        return Collections.unmodifiableCollection(ALL_ABILITIES.get(mode).values());
    }


    public static Collection<String> getAllNames(AbilityMode mode) {
        return Collections.unmodifiableCollection(ALL_ABILITIES.get(mode).keySet());
    }


    public static boolean isLegalKey(AbilityMode mode, String name) {
        return ALL_ABILITIES.get(mode).containsKey(name);
    }


    public static Map<AbilityKey, Boolean> mapEnum(AbilityMode mode, Map<String, Boolean> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return Collections.emptyMap();
        }
        return abilities.entrySet()
                .stream()
                .filter(entry -> isLegalKey(mode, entry.getKey()))
                .collect(Collectors.toMap((entry) -> getEnum(mode, entry.getKey()), Map.Entry::getValue));
    }


    public static Map<String, Boolean> mapStr(Map<AbilityKey, Boolean> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return Collections.emptyMap();
        }
        return abilities.entrySet()
                .stream()
                .collect(Collectors.toMap((entry) -> entry.getKey().getName(), Map.Entry::getValue));
    }


    public static AbilityKey getEnum(AbilityMode mode, String key) {
        return ALL_ABILITIES.get(mode).get(key);
    }

    static {
        try {
            for (AbilityKey value : AbilityKey.values()) {
                AbilityMode mode = value.getMode();
                Map<String, AbilityKey> map = ALL_ABILITIES.getOrDefault(mode, new HashMap<>());
                AbilityKey previous = map.putIfAbsent(value.getName(), value);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate key name field " + value + " and " + previous + " under mode: " + mode);
                }
                ALL_ABILITIES.put(mode, map);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
