package com.guild.search.redis;

import com.guild.search.Pearl;
import com.guild.search.PearlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Configurable
public class RedisMessageSubscriber implements MessageListener {


    private static final Logger log = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    @Autowired
    PearlRepository pearlRepository;

    @Autowired
    JedisConnectionFactory jedisConnectionFactory;

    private RedisConnection con;

    public RedisMessageSubscriber() {
    }

    public void onMessage(final Message message, final byte[] pattern) {
        log.info("Message received: " + new String(message.getBody()));

        try {
            if (con==null || con.isClosed()) {
                con = jedisConnectionFactory.getConnection();
            }

            List<Pearl> pearls = pearlRepository.findByName(message.toString());
            if (pearls == null || pearls.isEmpty()) {
//                con.hSet("values".getBytes(), message.getBody(), "שומר על זכות השתיקה".getBytes());
                con.publish("SEARCH_RES".getBytes(), "שומר על זכות השתיקה".getBytes());
                log.info("Value set: name:" + new String(message.getBody()) + " is silent!!!");
                return;
            }
            for (Pearl pearl : pearls) {
//                con.hSet("values".getBytes(), message.getBody(), pearl.getPearl().getBytes());
                con.publish("SEARCH_RES".getBytes(), pearl.getPearl().getBytes());
                log.info("Value set: name:" + new String(message.getBody()) + " message:"+pearl.getPearl());
            }

        } catch (Exception e) {
            log.error("RedisMessageSubscriber failure ", e);
        }
    }
}
