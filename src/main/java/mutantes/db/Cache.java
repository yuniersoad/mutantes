package mutantes.db;

import java.util.List;

public interface Cache {
    Long incr(String key);
    List<String> getKeys(String ...keys);
}
