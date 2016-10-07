package com.theironyard.handraise.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by kdrudy on 10/7/16.
 */
@Component
public class HandRaisedBot extends Bot {

    @Value("${slackIncomingWebhookUrl}")
    private String slackIncomingWebhookUrl;

    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = {EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) throws JsonProcessingException {
        reply(session, event, new Message("Hi, I am " + slackService.getCurrentUser().getName()));

        RestTemplate restTemplate = new RestTemplate();
        RichMessage richMessage = new RichMessage("Hand raised!");
        richMessage.setChannel("testingchannel");
        // set attachments
        Attachment[] attachments = new Attachment[1];
        attachments[0] = new Attachment();
        attachments[0].setText("Some data");
        richMessage.setAttachments(attachments);

        restTemplate.postForEntity(slackIncomingWebhookUrl, richMessage.encodedMessage(), String.class);
    }
}
