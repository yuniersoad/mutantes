package mutantes.health;

import com.codahale.metrics.health.HealthCheck;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

public class RedisHealthCheck extends HealthCheck {
    private final Jedis jedis;

    public RedisHealthCheck(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    protected Result check() {
        if (jedis.ping().equalsIgnoreCase("PONG")) {
            return Result.healthy();
        } else {
            final Client client = jedis.getClient();
            return Result.unhealthy("Cannot connect to %s:%d", client.getHost(), client.getPort());
        }
    }
}
