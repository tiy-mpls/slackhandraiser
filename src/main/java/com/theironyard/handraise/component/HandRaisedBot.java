package com.theironyard.handraise.component;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by kdrudy on 10/7/16.
 */
@Component
public class HandRaisedBot extends Bot {

    @Value("${slackIncomingWebhookUrl}")
    private String slackIncomingWebhookUrl;

    @Value("${slackBotToken}")
    private String slackToken;

    @Value("${slackWebAPIToken}")
    private String webAPIToken;

    @Value("${bee.channel}")
    private String beeChannel;

    @Value("${bee.instructor}")
    private String beeInstructor;
    private String beeInstructorId;

    @Value("#{'${bee.students}'.split(',')}")
    private List<String> beeStudents;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = {EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) throws IOException {

        String userName = getUserName(event);

        System.out.printf("onReceivedDM: userId : %s : userName : %s\n", event.getUserId(), userName);

        if(!event.getUserId().equals(slackService.getCurrentUser().getId())) {
            if(event.getText().endsWith("?")) {

                String instructor = "";
                String classChannel = "";

                if(beeStudents.contains(userName)) {
                    instructor = beeInstructor;
                    classChannel = beeChannel;
                }

                if(instructor.isEmpty() && classChannel.isEmpty()) {
                    reply(session, event, new Message("Please inform your instructor that I don't know who you are so that they can fix me."));
                } else {
                    reply(session, event, new Message("Great question, posting it to chat!"));

                    RichMessage richMessage = new RichMessage();
                    richMessage.setText("Hand raised!");
                    richMessage.setChannel(classChannel);

                    // set attachments
                    Attachment[] attachments = new Attachment[1];
                    attachments[0] = new Attachment();
                    attachments[0].setText("<@" + instructor + ">: " + event.getText());
                    richMessage.setAttachments(attachments);

                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.postForEntity(slackIncomingWebhookUrl, richMessage.encodedMessage(), String.class);
                }
            } else {
                reply(session, event, new Message("That doesn't look like a question, questions end in a question mark."));
            }
        }
    }

    private String getUserName(Event event) throws IOException {
        URL userInfoUrl = new URL(String.format("https://slack.com/api/auth.test?token=%s&%s", webAPIToken, event.getUserId()));
        URLConnection uc = userInfoUrl.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String inputLine = in.readLine();

//        System.out.println(inputLine);

        JsonParser parser = new BasicJsonParser();
        Map<String, Object> userInfo = parser.parseMap(inputLine);

//        for(String info : userInfo.keySet()) {
//            System.out.println(info + " : " + userInfo.get(info));
//        }

        String userName = null;
        if(userInfo.get("ok").equals("true")) {
            userName = (String) userInfo.get("user");
        }
        return userName;
    }
}
