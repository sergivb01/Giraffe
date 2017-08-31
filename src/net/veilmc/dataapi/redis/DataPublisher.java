package net.veilmc.dataapi.redis;

import net.veilmc.dataapi.*;
import redis.clients.jedis.*;

public class DataPublisher
{
    private DataAPI main;

    public DataPublisher(final DataAPI main) {
        this.main = main;
    }

    public void write(final String message) {
        final Jedis jedis = this.main.getJedisPool().getResource();
        try {
            jedis.publish("ares", message);
        }
        finally {
            if (jedis != null) {
                //this.main.getJedisPool().returnResource(jedis);
                //jedis.close();
            }
        }
    }
}
