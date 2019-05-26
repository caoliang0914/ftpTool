package com.sse.ftpTool.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 测试demo的controller
 *
 * @author zcc ON 2018/2/8
 **/
@Controller
@RequestMapping("/ftp")
public class FtpBrowserController {
    private static final Logger log = LoggerFactory.getLogger(FtpBrowserController.class);

    @GetMapping(value = "/browser")
    public String hello(Model model) {
        String name = "jiangbei";
        model.addAttribute("name", name);
        return "browser";
    }
}