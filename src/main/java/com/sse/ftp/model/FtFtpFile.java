package com.sse.ftp.model;

import java.util.Date;

public class FtFtpFile {
    public static enum DtftFType {
        file, dir, symLink, unknown
    }

    public DtftFType type;
    public boolean userReadable;
    public boolean userWritable;
    public boolean userExecutable;
    public boolean groupReadable;
    public boolean groupWritable;
    public boolean groupExecutable;
    public boolean otherReadable;
    public boolean otherWritable;
    public boolean otherExecutable;
    public int hardLinks;
    public String user;
    public String group;
    public long size;
    public Date lastModifiedDt;
    public String name;

    public DtftFType getType() {
        return type;
    }

    public boolean isUserReadable() {
        return userReadable;
    }

    public boolean isUserWritable() {
        return userWritable;
    }

    public boolean isUserExecutable() {
        return userExecutable;
    }

    public boolean isGroupReadable() {
        return groupReadable;
    }

    public boolean isGroupWritable() {
        return groupWritable;
    }

    public boolean isGroupExecutable() {
        return groupExecutable;
    }

    public boolean isOtherReadable() {
        return otherReadable;
    }

    public boolean isOtherWritable() {
        return otherWritable;
    }

    public boolean isOtherExecutable() {
        return otherExecutable;
    }

    public int getHardLinks() {
        return hardLinks;
    }

    public String getUser() {
        return user;
    }

    public String getGroup() {
        return group;
    }

    public long getSize() {
        return size;
    }

    public Date getLastModifiedDt() {
        return lastModifiedDt;
    }

    public String getName() {
        return name;
    }
}
