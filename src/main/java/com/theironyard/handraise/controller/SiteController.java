package com.theironyard.handraise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by kdrudy on 10/24/16.
 */
@Controller
public class SiteController {

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home() {
        return "home";
    }
}
