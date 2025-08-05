package com.wyc.simple.rpc.core.toolkit;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import cn.hutool.setting.yaml.YamlUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 配置工具类，负责从application.properties文件中加载配置信息
 */
@Slf4j
public class ConfigUtil {

    public static <T> T loadConfig(String prefix, Class<T> tClass){
        return loadConfig(prefix, tClass, "");
    }

    public static <T> T loadConfig(String prefix, Class<T> tClass, String environment){

        String fileNamePrefix = "application";
        if (!StrUtil.isBlank(environment)){
            fileNamePrefix += "-" + environment;
        }

        try {
            return loadConfigFromYaml(prefix, tClass, fileNamePrefix + ".yaml");
        } catch (Exception exception) {
            return loadConfigFromProperties(prefix, tClass, fileNamePrefix + ".properties");
        }
    }

    private static <T> T loadConfigFromYaml(String prefix, Class<T> tClass, String fileName) {
        try (InputStream inputStream = ResourceUtil.getStream(fileName)) {
            if (inputStream == null) {
                throw new RuntimeException("[未找到 " + fileName + " 文件]");
            }

            // 从 yaml 文件中读取 map
            Map<String, Object> map = YamlUtil.load(new InputStreamReader(inputStream));
            Map<?, ?> configMap = MapUtil.get(map, prefix, Map.class);
            CopyOptions copyOptions = CopyOptions.create().ignoreCase();
            return BeanUtil.toBean(configMap, tClass, copyOptions);
        } catch (Exception e) {
            log.error("[{} 文件读取失败, error={}]", fileName, e.getMessage());
            throw new RuntimeException("[" + fileName + " 文件读取失败]", e);
        }
    }

    private static <T> T loadConfigFromProperties(String prefix, Class<T> tClass, String fileName) {
        try {
            Props props = new Props(fileName);
            return props.toBean(tClass, prefix);
        } catch (Exception e) {
            log.error("[{} 文件读取失败, error={}]", fileName, e.getMessage());
            throw new RuntimeException("[" + fileName + " 文件读取失败]", e);
        }
    }

}
