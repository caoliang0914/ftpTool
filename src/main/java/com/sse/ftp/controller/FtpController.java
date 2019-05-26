package com.sse.ftp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author liang
 * @date 2019/5/26
 */
@Controller
@RequestMapping("/ftp")
public class FtpController {
    private static final Logger logger = LoggerFactory.getLogger(FtpController.class);

    @GetMapping(value = "/browser")
    public String browser() {
        return "browser";
    }
}