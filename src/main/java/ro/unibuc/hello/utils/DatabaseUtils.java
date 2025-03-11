package ro.unibuc.hello.utils;

import lombok.*;
import org.springframework.data.util.Pair;
import ro.unibuc.hello.data.entity.GameEntity;
import ro.unibuc.hello.data.entity.UserEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public interface DatabaseUtils {

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    class CompositeKey implements Serializable { // TODO: move somewhere common
        private String gameId;
        private String customerId;

        public static CompositeKey build(GameEntity game, UserEntity customer) {
            return new CompositeKey(game.getId(), customer.getId());
        }
    }

    Map<String, Pair<String, Integer>> info = new HashMap<>();

    static void setTemplate(String key, String template) {
        info.put(key, Pair.of(template + "%s", 0));
    }

    static String getId(String key, Integer at) {
        return String.format(info.get(key).getFirst(), at);
    }

    static String generateId(String key) {
        if (!info.containsKey(key)) {
            throw new RuntimeException(String.format("%s not found", key));
        }
        Pair<String, Integer> pairInfo = info.get(key);
        info.put(key, Pair.of(pairInfo.getFirst(), pairInfo.getSecond() + 1));
        return String.format(pairInfo.getFirst(), pairInfo.getSecond());
    }

}
