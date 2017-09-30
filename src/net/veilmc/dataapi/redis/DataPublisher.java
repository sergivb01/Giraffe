package net.veilmc.dataapi.redis;

import net.veilmc.dataapi.Giraffe;
import redis.clients.jedis.Jedis;

public class DataPublisher
{
    private Giraffe main;

    public DataPublisher(final Giraffe main) {
        this.main = main;
    }

    public void write(final String message) {
        Jedis jedis = null;
        try {
            jedis = this.main.getPool().getResource();
            jedis.publish("ares", message);
            this.main.getPool().returnResource(jedis);
        }
        finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
