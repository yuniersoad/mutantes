package mutantes.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedisCache implements Cache {

    private final Jedis cache;

    public RedisCache(Jedis cache) {
        this.cache = cache;
    }

    @Override
    public Long incr(String key) {
        return cache.incr(key);
    }

    @Override
    public List<String> getKeys(String... keys) {
        final Pipeline p = cache.pipelined();
        final List<Response<String>> results = new ArrayList<>(keys.length);
        for (String key: keys) {
           results.add(p.get(key));
        }
        p.sync();
        return results.stream().map(Response::get).collect(Collectors.toList());
    }
}
