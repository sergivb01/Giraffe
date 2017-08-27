package net.veilmc.dataapi.redis;

import net.veilmc.dataapi.DataAPI;
import redis.clients.jedis.Jedis;

public class DataPublisher
{
    private DataAPI main;

    public DataPublisher(final DataAPI main) {
        this.main = main;
    }

    public void write(final String message) {
        Jedis jedis = main.getJedisPool().getResource();
        try {
            //jedis = main.getJedisPool().getResource();
            jedis.publish("ares", message);
        }finally {
            if (jedis != null){
                main.getJedisPool().returnResource(jedis);
                jedis.close();
            }
        }
    }
}