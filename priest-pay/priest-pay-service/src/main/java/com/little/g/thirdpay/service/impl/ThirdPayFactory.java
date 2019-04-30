package com.little.g.thirdpay.service.impl;

import com.little.g.common.exception.ServiceDataException;
import com.little.g.pay.PayErrorCodes;
import com.little.g.thirdpay.service.api.ThirdPayService;
import com.little.g.thirdpay.mapper.PayChannelMapper;
import com.little.g.thirdpay.model.PayChannel;
import com.little.g.thirdpay.model.PayChannelExample;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.*;

/**
 * 第三方支付服务工厂
 *
 */
@Service("thirdPayFactory")
public class ThirdPayFactory {
    private static final Logger log = LoggerFactory.getLogger(ThirdPayFactory.class);
    private Map<String, ThirdPayService> thirdPayServiceMap = new HashMap<String, ThirdPayService>();

    @Resource
    PayChannelMapper payChannelMapper;

    List<PayChannel> channelList;

    private static final String CONFIG_VERSION = "little";

    @PostConstruct
    public void init() {

        List<PayChannel> channels = payChannelMapper.selectByExample(new PayChannelExample());
        channelList = channels;
        if (channels != null && channels.size() > 0) {
            for (PayChannel channel : channels) {
                register(channel);
            }
        }
    }

    public List<PayChannel> getChannelList() {
        return channelList;
    }

    public ThirdPayService getThirdPayService(String code) {
        String type = getKey(code);
        ThirdPayService thirdPayService = thirdPayServiceMap.get(type);
        if (thirdPayService == null) {
            PayChannelExample example=new PayChannelExample();
            example.or().andCodeEqualTo(code)
                        .andConfigVersionEqualTo(CONFIG_VERSION);
            List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
            if(CollectionUtils.isEmpty(payChannelList)){
                throw new ServiceDataException(PayErrorCodes.THIRDPAY_ERROR,"msg.thirdpay.unknow.paychannel");
            }

            return register(payChannelList.get(0));

        }
        return thirdPayService;
    }

    /**
     * 注册到第三方service到map中
     *
     * @param payChannel 支付方式
     * @return
     */
    private synchronized ThirdPayService register(PayChannel payChannel) {
        String type = getKey(payChannel.getCode(), payChannel.getConfigVersion());
        if (thirdPayServiceMap.get(type) != null) {
            return thirdPayServiceMap.get(type);
        }
         log.info("ThirdPayFactory.register:{}",payChannel);
        Map<String, String> confs = loadConf(payChannel.getCode(), payChannel.getConfigVersion());
        ThirdPayService service = ThirdPayService.newThirdPayService(payChannel.getCode(), confs);
        thirdPayServiceMap.put(type, service);
        return service;

    }


    private String getKey(String code){
        return getKey(code,null);
    }

    /**
     * 获取支付mapkey
     *
     * @param code          支付编码
     * @param configVersion 配置号
     * @return
     */
    private String getKey(String code, String configVersion) {
        if(StringUtils.isEmpty(configVersion)){
            configVersion = "little";
        }
        return code + "/" + configVersion;
    }

    /**
     * 加载支付配置
     *
     * @param code          支付编码
     * @param configVersion 配置号
     * @return
     */
    private Map<String, String> loadConf(String code, String configVersion) {
        Properties properties = new Properties();
        try {
            properties.load(ThirdPayFactory.class.getResourceAsStream("/payconfig/" + getKey(code, configVersion) + ".properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> confs = new HashMap<String, String>();
        Set<String> keys = properties.stringPropertyNames();
        for (String key : keys) {
            if (key.lastIndexOf("File") > -1) {
                String filePath = properties.getProperty(key);
                String basePath = ThirdPayFactory.class.getResource("/").getPath();

                InputStream in = ThirdPayFactory.class.getResourceAsStream(filePath);
                String realPath = basePath + filePath;
                File newFile = new File(realPath);
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                OutputStream out = null;
                try {
                    out = new FileOutputStream(newFile);
                    int b;
                    while ((b = in.read())!=-1) {
                        out.write(b);
                    }
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    throw new RuntimeException("copy file error");
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
                confs.put(key, realPath);
            } else {
                confs.put(key, properties.getProperty(key));
            }
        }
        return confs;
    }
}
