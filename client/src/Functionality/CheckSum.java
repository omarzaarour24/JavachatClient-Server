package Functionality;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class CheckSum {
    private String result;

    public CheckSum() {
        this.result = "";
    }

    public String create(File file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];
        int nread = 0;
        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        byte[] mdBytes = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdBytes.length; i++) {
            sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        fis.close();
        result=sb.toString();
        return sb.toString();
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean compare(String checksumString) {
        return checksumString.equals(result);
    }

}
