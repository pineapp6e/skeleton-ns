package com.hesine.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public final class PushConfig {

    private URL url;
    private String hcpsUrl;
    private String cpId;
    private String cpPwd;
    private Map<String, Object> paramMap;

    private PushConfig(String hcpsUrl, String cpId, String cpPwd, Map<String, Object> paramMap)
            throws MalformedURLException {
        super();
        this.hcpsUrl = hcpsUrl;
        this.url = new URL(hcpsUrl);
        this.cpId = cpId;
        this.cpPwd = cpPwd;
        this.paramMap = paramMap;
    }

    public static PushConfig initConfig(String hcpsUrl, String cpId, String cpPwd,
            Map<String, Object> paramMap) {
        PushConfig config = null;
        try {
            config = new PushConfig(hcpsUrl, cpId, cpPwd, paramMap);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return config;
    }

    /**
     * @return the hcpsUrl
     */
    public String getHcpsUrl() {
        return hcpsUrl;
    }

    /**
     * @return the cpId
     */
    public String getCpId() {
        return cpId;
    }

    /**
     * @return the cpPwd
     */
    public String getCpPwd() {
        return cpPwd;
    }

    /**
     * @return the paramMap
     */
    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getHost() {
        return url.getHost();
    }

    public int getPort() {
        return url.getPort();
    }
}
