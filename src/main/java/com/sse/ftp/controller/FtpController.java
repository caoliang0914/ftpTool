package com.sse.ftp.controller;

import com.sse.ftp.model.FtFtpFile;
import com.sse.ftp.model.FtListResp;
import com.sse.ftp.util.FtFtpClient;
import com.sse.ftp.util.FtHandyUtils;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

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

    @PostMapping(value = "/list")
    @ResponseBody
    public FtListResp list(@RequestParam(name = "path", required = false) String path,
                           @RequestParam(name = "ip", required = false) String ip,
                           @RequestParam(name = "port", required = false, defaultValue = "21") int port,
                           @RequestParam(name = "user", required = false) String user,
                           @RequestParam(name = "password", required = false) String password) {

        ip = FtHandyUtils.trimAndEmpty(ip);
        user = FtHandyUtils.trimAndEmpty(user);
        password = FtHandyUtils.trimAndEmpty(password);
        path = organizePath(FtHandyUtils.trimAndEmpty(path));

        List<FtFtpFile> children = null;

        String cmds = "";
        if (!ip.isEmpty() && !user.isEmpty() && !password.isEmpty() && port > 0) {
            FtFtpClient dc = new FtFtpClient(ip, port, user, password);
            if (dc.open()) {
                children = dc.list(path);
                dc.close();
            }
            cmds = dc.getRawCommands();
        }

        logger.info("ftp cmds {}", cmds);

        return new FtListResp().setIp(ip).setPort(port).setUser(user).setPassword(password).setPassword(password)
                .setCmds(cmds).setChildren(children == null ? new LinkedList<>() : children);
    }

    private static String organizePath(String path) {
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (path.endsWith("/../")) {
            path = path.substring(0, path.length() - 4);
            int p = path.lastIndexOf('/');
            if (p == 0) {
                path = "";
            } else if (p > 0) {
                path = path.substring(0, p);
            }
        }
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }


    private static String parseFileName(String path) {
        int p = path.lastIndexOf('/');
        if (p < 0) {
            p = path.lastIndexOf('\\');
        }
        return path.substring(p + 1);
    }

    @PostMapping("/download")
    public void download(@RequestParam(name = "path", required = true) String path,
                         @RequestParam(name = "ip", required = false) String ip,
                         @RequestParam(name = "port", required = false) int port,
                         @RequestParam(name = "user", required = false) String user,
                         @RequestParam(name = "pass", required = false) String pass, HttpServletResponse rsp,
                         HttpServletRequest req) {
        path = FtHandyUtils.trimAndEmpty(path);
        ip = FtHandyUtils.trimAndEmpty(ip);
        user = FtHandyUtils.trimAndEmpty(user);
        pass = FtHandyUtils.trimAndEmpty(pass);

        FtFtpClient dc = new FtFtpClient(ip, port, user, pass);
        if (dc.open()) {
            byte[] data = dc.download(path);
            dc.close();

            if (data != null && data.length > 0) {
                String fileName = parseFileName(path);
                String contentType = URLConnection.guessContentTypeFromName(fileName);
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
                rsp.setContentType(contentType);
                rsp.setContentLength(data.length);
                String userAgentString = req.getHeader("User-Agent");
                boolean isFirefox = userAgentString.contains("firefox");
                String encodedFileName = fileName;
                try {
                    if (!isFirefox) {
                        encodedFileName = URLEncoder.encode(fileName, "utf-8").replaceAll("\\+", "%20");
                    } else {
                        encodedFileName = MimeUtility.encodeWord(fileName);
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.warn("ftpDownload()", e);
                }
                rsp.setHeader("Content-Disposition", "attachment; fileName=\"" + encodedFileName + "\"");
                try {
                    OutputStream os = rsp.getOutputStream();
                    os.write(data);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    logger.warn("ftpDownload()", e);
                }
            }
        }
    }

}