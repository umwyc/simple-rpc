package com.wyc.simple.rpc.core.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wyc.simple.rpc.core.registry.RegistryCenter;
import com.wyc.simple.rpc.core.serializer.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {

    /**
     * 已加载的类: 接口全限定名 + 自定义 key ===> 接口实现类
     */
    private static final Map<String, Map<String, Class<?>>> LOAD_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 实例对象: 类全限定名 ===> 类的单例对象
     */
    private static final Map<String, Object> SINGLETON_MAP = new ConcurrentHashMap<>();

    /**
     * 系统目录
     */
    private static final String SIMPLE_RPC_SYSTEM_SPI_DIR = "META-INF/simple-rpc/system/";
    /**
     * 用户目录
     */
    private static final String SIMPLE_RPC_CUSTOM_SPI_DIR = "META-INF/simple-rpc/custom/";
    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIR = new String[]{SIMPLE_RPC_SYSTEM_SPI_DIR, SIMPLE_RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载类列表
     */
    private static final List<Class<?>> DYNAMIC_CLASS_LOAD_LIST = Arrays.asList(Serializer.class, RegistryCenter.class);

    /**
     * 加载所有类型
     */
    public static void loadAll(){
        log.info("[开始加载所有动态类]");
        for (Class<?> tClass : DYNAMIC_CLASS_LOAD_LIST) {
            load(tClass);
        }
        log.info("[终止加载所有动态类]");
    }

    /**
     * 加载指定类型
     * @param tClass 接口的全路径名
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> tClass){
        log.info("加载 {} 接口的动态类", tClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // 扫描路径
        for (String scanDir : SCAN_DIR) {
            // 根据接口的全限定名读取对应文件
            List<URL> resources = ResourceUtil.getResources(scanDir + tClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line;

                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split("=");
                        if(split.length > 1){
                            String key = split[0];
                            String className = split[1];
                            log.info("加载实现类 key={},className={}", key, className);
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("加载错误", e);
                }
            }
        }
        LOAD_CLASS_MAP.put(tClass.getName(), keyClassMap);
        return keyClassMap;
    }

    /**
     * 获取某个接口的实现
     * @param clazz 接口全路径名
     * @param key 自定义 key
     * @return 实现类
     * @param <T>
     */
    public static <T> T getInstance(Class<?> clazz, String key){
        Map<String, Class<?>> keyClassMap = LOAD_CLASS_MAP.get(clazz.getName());
        if(keyClassMap == null){
            throw new RuntimeException(String.format("[SpiLoader 未加载 %s 接口]", clazz.getName()));
        }
        if(!keyClassMap.containsKey(key)){
            throw new RuntimeException(String.format("[SpiLoader %s 接口不包含 key = %s 的实现类]", clazz.getName(), key));
        }

        // 获取类的全限定名
        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();
        if(!SINGLETON_MAP.containsKey(implClassName)){
            try {
                SINGLETON_MAP.put(implClassName, implClass.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String errorMessage = String.format("[%s 实例化失败]", implClassName);
                throw new RuntimeException(errorMessage, e);
            } catch (InvocationTargetException e) {
                log.error("[反射调用类的无参构造失败 className={}]", implClassName);
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) SINGLETON_MAP.get(implClassName);
    }
}
