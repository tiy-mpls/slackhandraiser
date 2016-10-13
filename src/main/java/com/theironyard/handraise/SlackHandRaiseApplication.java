package com.theironyard.handraise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "com.theironyard.handraise"})
public class SlackHandRaiseApplication {

    public static final String VERSION = "1.0.0";

	public static void main(String[] args) {
		SpringApplication.run(SlackHandRaiseApplication.class, args);
	}
}
