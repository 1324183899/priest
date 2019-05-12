package com.little.g.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.builder.HCB;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import com.little.g.common.exception.ServiceDataException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.*;
import java.util.Map;

public class HttpUtils {


    private static HCB hcb;

    static {
        try {
            hcb = HCB.custom().pool(100, 10).timeout(1000);
        } catch (HttpProcessException e) {
            e.printStackTrace();
        }
    }


    public static byte[] download(String url) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpConfig config = HttpConfig.custom().client(hcb.build())
                .url(url)
                .out(out);

        OutputStream result = null;
        try {
            result = HttpClientUtil.down(config);
            return out.toByteArray();

        } catch (Exception e) {
            throw new ServiceDataException(e);
        } finally {
            try {
                if (result != null) {
                    result.close();
                }

            } catch (IOException e) {
                throw new ServiceDataException(e);
            }
        }

    }


    public static <T> T get(String url, Map<String, Object> params, String json, Class<T> clazz) {

        HttpConfig config = HttpConfig.custom().client(hcb.build());

        try {
            URIBuilder b = new URIBuilder(url);

            if (params != null && params.size() > 0) {

                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    b.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }

            config.url(b.toString());

            if (!StringUtils.isEmpty(json)) {
                config.json(json);
            }

            String result = HttpClientUtil.get(config);
            return JSONObject.parseObject(result, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }


    public static <T> T post(String url, Map<String, Object> params, String json, Class<T> clazz) {

        HttpConfig config = HttpConfig.custom().client(hcb.build())
                .url(url);

        if (params != null && params.size() > 0) {
            config.map(params);
        }

        if (!StringUtils.isEmpty(json)) {
            config.json(json);
        }

        try {
            String result = HttpClientUtil.post(config);
            return JSONObject.parseObject(result, clazz);
        } catch (HttpProcessException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {
        byte[] downloads = download("http://v6-dy.ixiguavideo.com/8d260c8e140b558429173c26d85d4154/5cc2c6f0/video/m/2204e2d64154d4a4c7ab2be5dc7fa111cb21161d7367000040580952bb59/?rc=M2ZkOWc7Zjp4bDMzZGkzM0ApQHRAb0U3NDg0NTU0NDQ1ODk3PDNAKXUpQGczdylAZmxkamV6aGhkZjs0QGJoMS5nYzRrb18tLTYtMHNzLW8jbyM%2BNjU2My0yLS0yMi4vLS4vaTpiLW8jOmAtbyNtbCtiK2p0OiMvLl4%3D");

        File f = new File("test.mp4");
        FileOutputStream l = null;
        try {
            l = new FileOutputStream(f);
            l.write(downloads);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                l.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println(downloads.length);
    }


}
