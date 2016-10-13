package com.theironyard.handraise.component;

import com.theironyard.handraise.SlackHandRaiseApplication;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.boot.json.JsonParser;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    //Testing Values
    @Value("${test.channel}")
    private String testChannel;

    @Value("${test.instructor}")
    private String testInstructor;

    @Value("#{'${test.students}'.split(',')}")
    private List<String> testStudents;


    //Back End Values
    @Value("${bee.channel}")
    private String beeChannel;

    @Value("${bee.instructor}")
    private String beeInstructor;

    @Value("#{'${bee.students}'.split(',')}")
    private List<String> beeStudents;

    //Front End Values
    @Value("${fee.channel}")
    private String feeChannel;

    @Value("${fee.instructor}")
    private String feeInstructor;

    @Value("#{'${fee.students}'.split(',')}")
    private List<String> feeStudents;

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

        String userName = getUserName(event.getUserId());

        System.out.printf("onReceivedDM: userId : %s : userName : %s\n", event.getUserId(), userName);

        if(!event.getUserId().equals(slackService.getCurrentUser().getId())) {

            String instructor = "";
            String classChannel = "";

            if(beeStudents.contains(userName)) {
                instructor = beeInstructor;
                classChannel = beeChannel;
            } else if(feeStudents.contains(userName)) {
                instructor = feeInstructor;
                classChannel = feeChannel;
            } else if(testStudents.contains(userName)) {
                instructor = testInstructor;
                classChannel = testChannel;
            }


            if(instructor.isEmpty() && classChannel.isEmpty()) {
                reply(session, event, new Message("Please inform your instructor that I don't know who you are so that they can fix me."));
            } else if(event.getText().equalsIgnoreCase("raise") || event.getText().equalsIgnoreCase("raise hand")) {
                reply(session, event, new Message("Raising your hand in " + classChannel + "."));

                RichMessage richMessage = new RichMessage();
                richMessage.setText("Hand raised!");
                richMessage.setChannel(classChannel);

                // set attachments
                Attachment[] attachments = new Attachment[1];
                attachments[0] = new Attachment();
                attachments[0].setText("<@" + instructor + "> raised hand from <@" + userName + ">.");
                richMessage.setAttachments(attachments);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.postForEntity(slackIncomingWebhookUrl, richMessage.encodedMessage(), String.class);


            } else if(event.getText().endsWith("?")) {
                reply(session, event, new Message("Great question, posting it to chat!"));

                RichMessage richMessage = new RichMessage();
                richMessage.setText("Hand raised!");
                richMessage.setChannel(classChannel);

                // set attachments
                Attachment[] attachments = new Attachment[1];
                attachments[0] = new Attachment();
                attachments[0].setText("<@" + instructor + "> " + event.getText());
                richMessage.setAttachments(attachments);

                RestTemplate restTemplate = new RestTemplate();
                restTemplate.postForEntity(slackIncomingWebhookUrl, richMessage.encodedMessage(), String.class);

            } else if(event.getText().equalsIgnoreCase("about")) {
                reply(session, event, new Message(String.format("HandRaiseBot v%s written by Kyle David Rudy for The Iron Yard Twin Cities", SlackHandRaiseApplication.VERSION)));
            } else {
                reply(session, event, new Message("I don't know what you mean.  Type 'Raise' to raise your hand or a question to ask a question to your class channel."));
            }
        }
    }

    public String getUserName(String userId) throws IOException {
        URL userInfoUrl = new URL(String.format("https://slack.com/api/auth.test?token=%s&%s", webAPIToken, userId));
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
