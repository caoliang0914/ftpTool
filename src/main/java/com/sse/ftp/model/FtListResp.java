package com.sse.ftp.model;

import java.util.List;

/**
 * @author liangcao
 * @date 2019/5/27 14:04
 * @Description
 */
public class FtListResp {

    private String ip;
    private int port = 21;
    private String user;
    private String password;
    private String path;
    private String cmds;
    private List<FtFtpFile> children;

    public String getIp() {
        return ip;
    }

    public FtListResp setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public FtListResp setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUser() {
        return user;
    }

    public FtListResp setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FtListResp setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPath() {
        return path;
    }

    public FtListResp setPath(String path) {
        this.path = path;
        return this;
    }

    public String getCmds() {
        return cmds;
    }

    public FtListResp setCmds(String cmds) {
        this.cmds = cmds;
        return this;
    }

    public List<FtFtpFile> getChildren() {
        return children;
    }

    public FtListResp setChildren(List<FtFtpFile> children) {
        this.children = children;
        return this;
    }
}
