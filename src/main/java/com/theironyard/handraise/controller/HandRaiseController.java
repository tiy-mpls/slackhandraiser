package com.theironyard.handraise.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Created by kdrudy on 10/7/16.
 */
@RestController
public class HandRaiseController {


    private static final Logger logger = LoggerFactory.getLogger(HandRaiseController.class);

    /**
     * The token you get while creating a new Slash Command. You
     * should paste the token in application.properties file.
     */
    @Value("${slashCommandToken}")
    private String slackToken;

    @Value("${test.channel}")
    private String testChannel;
    @Value("${test.instructor}")
    private String testInstructor;

    @Value("${bee.channel}")
    private String beeChannel;
    @Value("${bee.instructor}")
    private String beeInstructor;

    @Value("${fee.channel}")
    private String feeChannel;
    @Value("${fee.instructor}")
    private String feeInstructor;


    /**
     * Slash Command handler. When a user types for example "/app help"
     * then slack sends a POST request to this endpoint. So, this endpoint
     * should match the url you set while creating the Slack Slash Command.
     *
     * @param token
     * @param teamId
     * @param teamDomain
     * @param channelId
     * @param channelName
     * @param userId
     * @param userName
     * @param command
     * @param text
     * @param responseUrl
     * @return
     */
    @RequestMapping(value = "/raisehand",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(@RequestParam("token") String token,
                                             @RequestParam("team_id") String teamId,
                                             @RequestParam("team_domain") String teamDomain,
                                             @RequestParam("channel_id") String channelId,
                                             @RequestParam("channel_name") String channelName,
                                             @RequestParam("user_id") String userId,
                                             @RequestParam("user_name") String userName,
                                             @RequestParam("command") String command,
                                             @RequestParam("text") String text,
                                             @RequestParam("response_url") String responseUrl) {
        // validate token
        if (!token.equals(slackToken)) {
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

//        System.out.println("RAISEHAND: channelName:" + channelName);

        Optional<String> instructor = Optional.empty();
        if(channelName.equalsIgnoreCase(testChannel) || "privategroup".equalsIgnoreCase(channelName)) {
            instructor = Optional.ofNullable(testInstructor);
        } else if(channelName.equalsIgnoreCase(beeChannel)) {
            instructor = Optional.ofNullable(beeInstructor);
        } else if(channelName.equalsIgnoreCase(feeChannel)) {
            instructor = Optional.ofNullable(feeInstructor);
        }

        if(!"directmessage".equals(channelName)) {
            /* build response */
            RichMessage richMessage = new RichMessage("Hand Raised!");
            richMessage.setResponseType("in_channel");
            // set attachments
            Attachment[] attachments = new Attachment[1];
            attachments[0] = new Attachment();
            if (instructor.isPresent()) {
                attachments[0].setText("<@" + instructor.get() + "> hand raised by <@" + userName + ">");
            } else {
                attachments[0].setText("Hand raised by <@" + userName + ">");
            }
            richMessage.setAttachments(attachments);

            // For debugging purpose only
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Reply (RichMessage): {}", new ObjectMapper().writeValueAsString(richMessage));
                } catch (JsonProcessingException e) {
                    logger.debug("Error parsing RichMessage: ", e);
                }
            }

            return richMessage.encodedMessage(); // don't forget to send the encoded message to Slack
        }

        return null;
    }
}
