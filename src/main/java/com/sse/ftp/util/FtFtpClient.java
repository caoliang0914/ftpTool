package com.sse.ftp.util;

import com.sse.ftp.model.FtFtpFile;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liangcao
 * @date 2019/5/27 13:22
 * @Description FTP客户端工具类
 */
public class FtFtpClient {
    public enum FtdFtpFeature {
        MDTM, REST, SIZE, MLST, MLSD, UTF8, CLNT, MFMT, EPSV, EPRT
    }

    private static final Logger logger = LoggerFactory.getLogger(FtFtpClient.class);

    private final String server;
    private final int port;
    private final String user;
    private final String pass;
    private final FTPClient fc = new FTPClient();
    private boolean logined = false;
    private final StringWriter sw = new StringWriter();

    public FtFtpClient(String server, int port, String user, String pass) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;

        fc.setControlEncoding("UTF-8");
        fc.setListHiddenFiles(true);
        fc.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(sw), true, (char) 0, true));

        FTPClientConfig fcc = new FTPClientConfig();
        // FTPClientConfig fcc = new FTPClientConfig("UNIX");
        fcc.setUnparseableEntries(true);
        // fcc.setDefaultDateFormatStr(null);
        // fcc.setRecentDateFormatStr(null);
        fc.configure(fcc);
    }

    /**
     * @return if success
     */
    public boolean open() {
        assert !fc.isConnected() && !logined;

        IOException re = null;
        String msg = null;

        try {
            fc.connect(server, port);
        } catch (IOException e) {
            re = e;
        }

        if (re == null) {
            int reply = fc.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                msg = "failed with reply " + reply;
            }
        }

        String systemType = null;
        if (re == null && msg == null) {
            logined = true;
            try {
                if (!fc.login(user, pass)) {
                    msg = "login fail";
                } else {
                    systemType = fc.getSystemType();
                    fc.setFileType(FTP.BINARY_FILE_TYPE);
                }
            } catch (IOException e) {
                re = e;
            }
        }

        if (re == null && msg == null) {
            fc.enterLocalPassiveMode();
            // fc.setUseEPSVwithIPv4(true);
        }

        if (re != null || msg != null) {
            if (re != null) {
                logger.error("open()", re);
            } else {
                logger.error("open(): {}", msg);
            }
            closeIfOpened();
            return false;
        }

        logger.info("open() ok, systemType={}", systemType);
        return true;
    }

    private void closeIfOpened() {
        if (logined) {
            logined = false;
            try {
                fc.noop(); // it could throw exception, no sure why use it
                if (!fc.logout()) {
                    logger.warn("closeIfOpened(): logout fail");
                }
            } catch (IOException e) {
                logger.warn("closeIfOpened()", e);
            }
        }
        if (fc.isConnected()) {
            try {
                fc.disconnect();
            } catch (IOException e) {
                logger.warn("closeIfOpened()", e);
            }
        }
    }

    public void close() {
        assert fc.isConnected();

        closeIfOpened();
        logger.info("close() ok");
    }

    private static boolean dealWithInValid(FTPFile f) {
        if (!f.getRawListing().trim().isEmpty() && !f.getRawListing().endsWith(":")) {
            logger.error("list() fail at {}", f.getRawListing());
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param path nullable
     * @return null if something fail
     */
    public List<FtFtpFile> list(String path) {
        try {
            List<FtFtpFile> r = new LinkedList<>();
            boolean hasError = false;
            FTPFile[] fs = null;
            if (supports(FtdFtpFeature.MLSD) != null) {
                fs = fc.mlistDir(path);
            } else {
                LinkedHashMap<String, FTPFile> fsm = new LinkedHashMap<>();
                for (FTPFile f : fc.listDirectories(path)) {
                    if (!f.isValid()) {
                        hasError = hasError || dealWithInValid(f);
                        continue;
                    }
                    if (!fsm.containsKey(f.getName())) {
                        fsm.put(f.getName(), f);
                    }
                }
                for (FTPFile f : fc.listFiles(path)) {
                    if (!f.isValid()) {
                        hasError = hasError || dealWithInValid(f);
                        continue;
                    }
                    if (!fsm.containsKey(f.getName())) {
                        fsm.put(f.getName(), f);
                    }
                }
                fs = fsm.values().toArray(new FTPFile[0]);
            }
            for (FTPFile f : fs) {
                if (!f.isValid()) {
                    hasError = hasError || dealWithInValid(f);
                    continue;
                }
                if (f.getName().equals(".") || f.getName().equals("..")) {
                    continue;
                }

                FtFtpFile x = new FtFtpFile();
                r.add(x);

                switch (f.getType()) {
                    case FTPFile.FILE_TYPE: {
                        x.type = FtFtpFile.DtftFType.file;
                    }
                    break;
                    case FTPFile.DIRECTORY_TYPE: {
                        x.type = FtFtpFile.DtftFType.dir;
                    }
                    break;
                    case FTPFile.SYMBOLIC_LINK_TYPE: {
                        x.type = FtFtpFile.DtftFType.symLink;
                    }
                    break;
                    default: {
                    }
                    break;
                }

                x.userReadable = f.hasPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION);
                x.userWritable = f.hasPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION);
                x.userExecutable = f.hasPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION);
                x.groupReadable = f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.READ_PERMISSION);
                x.groupWritable = f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.WRITE_PERMISSION);
                x.groupExecutable = f.hasPermission(FTPFile.GROUP_ACCESS, FTPFile.EXECUTE_PERMISSION);
                x.otherReadable = f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION);
                x.otherWritable = f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.WRITE_PERMISSION);
                x.otherExecutable = f.hasPermission(FTPFile.WORLD_ACCESS, FTPFile.EXECUTE_PERMISSION);
                x.hardLinks = f.getHardLinkCount();
                x.user = f.getUser();
                x.group = f.getGroup();
                x.size = f.getSize();
                x.lastModifiedDt = f.getTimestamp() == null ? null : f.getTimestamp().getTime();
                x.name = f.getName();
            }
            return hasError && r.isEmpty() ? null : r;
        } catch (IOException e) {
            logger.error("list()", e);
            return null;
        }
    }

    public boolean upload(String path, String fileName, byte[] data, boolean override) {
        if (!override) {
            ; // TODO check if exists
        }
        try {
            fc.changeWorkingDirectory(path);
            return fc.storeFile(fileName, new ByteArrayInputStream(data));
        } catch (IOException e) {
            logger.error("upload()", e);
            return false;
        }
    }

    /**
     * @param path
     * @return null if fail
     */
    public byte[] download(String path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (fc.retrieveFile(path, baos)) {
                return baos.toByteArray();
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error("download()", e);
            return null;
        }
    }

    /**
     * @param feature
     * @return null if not support, "" if support but no param, param if has one param or more
     */
    public String supports(FtdFtpFeature feature) {
        String[] rs = null;
        try {
            rs = fc.featureValues(feature.toString());
        } catch (IOException e) {
            logger.error("supports", e);
            return null;
        }
        if (rs == null || rs.length == 0) {
            return null;
        } else if (rs.length == 1) {
            if (rs[0].trim().isEmpty()) {
                return "";
            } else {
                return rs[0].trim();
            }
        } else {
            // TODO, not used yetW
            return rs[0].trim();
        }
    }

    /**
     * MDTM REST STREAM SIZE MLST type*;size*;modify*; MLSD UTF8 CLNT MFMT EPSV EPRT
     *
     * @return
     */
    public boolean printFeatures(String feature) {
        try {
            if (feature == null) {
                return fc.features();
            } else {
                String[] rs = fc.featureValues(feature);
                if (rs == null || rs.length == 0) {
                    System.out.println("-> feature " + feature + " don't support");
                } else if (rs.length == 1) {
                    if (rs[0].trim().isEmpty()) {
                        System.out.println("-> feature " + feature + " supports");
                    } else {
                        System.out.println("-> feature " + feature + " supports " + rs[0]);
                    }
                } else {
                    System.out.println("-> feature " + feature + " supports length=" + rs.length + ", v0=" + rs[0]);
                }
                return true;
            }
        } catch (IOException e) {
            logger.error("printFeatures()", e);
            return false;
        }
    }

    // TODO: it's a memory leak
    public String getRawCommands() {
        return sw.toString();
    }

    public long getFileSize(String path, String fileName) {
        try {
            FTPFile[] files = fc.listFiles(path);
            for (FTPFile file : files) {
                if (file.getName() != null && file.getName().equals(fileName)) {
                    return file.getSize();
                }
            }
        } catch (IOException e) {
            logger.error("getFileSize()", e);
        }
        return 0L;
    }

    public Date getLastModifyTime(String path, String fileName) {
        try {
            FTPFile[] files = fc.listFiles(path);
            for (FTPFile file : files) {
                if (file.getName() != null && file.getName().equals(fileName)) {
                    return file.getTimestamp().getTime();
                }
            }
        } catch (IOException e) {
            logger.error("getLastModifyTime()", e);
        }
        return null;
    }

    public void rename(String fileName, String newFileName, String path) {
        try {
            fc.changeWorkingDirectory(path);
            fc.rename(fileName, newFileName);
        } catch (IOException e) {
            logger.error("rename()", e);
        }
    }
}
