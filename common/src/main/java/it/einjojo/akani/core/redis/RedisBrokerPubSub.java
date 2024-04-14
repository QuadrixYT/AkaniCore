package it.einjojo.akani.core.redis;

import com.google.gson.Gson;
import it.einjojo.akani.core.api.messaging.ChannelMessage;
import redis.clients.jedis.JedisPubSub;

public class RedisBrokerPubSub extends JedisPubSub {

    private final RedisBrokerService requestService;

    private final Gson gson;

    public RedisBrokerPubSub(RedisBrokerService requestService, Gson gson) {
        this.requestService = requestService;

        this.gson = gson;
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            ChannelMessage channelMessage = gson.fromJson(message, ChannelMessage.class);
            if (!requestService.isChannelMessageForMe(channelMessage)) return;
            if (requestService.completePendingRequest(channelMessage))
                return; // if the message is a response to a pending request, we don't need to forward it to the processors
            requestService.forwardToProcessors(channelMessage);
        } catch (Exception e) {
            requestService.logger().severe("Error while processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
