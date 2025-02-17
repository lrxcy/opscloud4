package com.baiyi.opscloud.common.util;

import com.alibaba.fastjson.JSON;
import com.baiyi.opscloud.domain.model.property.ServerProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Author baiyi
 * @Date 2021/8/20 4:00 下午
 * @Version 1.0
 */
public class BusinessPropertyUtil {

    public static <T> T toProperty(String property, Class<T> targetClass) throws JsonSyntaxException {
        Yaml yaml = new Yaml();
        Object result = yaml.load(property);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(JSON.toJSONString(result), targetClass);
    }

    /**
     * 该方法是用于相同对象不同属性值的合并，如果两个相同对象中同一属性都有值，那么sourceBean中的值会覆盖tagetBean重点的值
     *
     * @param sourceBean 被提取的对象bean
     * @param targetBean 用于合并的对象bean
     * @return targetBean, 合并后的对象
     */
    public static ServerProperty.Server combineServerProperty(ServerProperty.Server sourceBean, ServerProperty.Server targetBean) {
        Class sourceBeanClass = sourceBean.getClass();
        Class targetBeanClass = targetBean.getClass();

        Field[] sourceFields = sourceBeanClass.getDeclaredFields();
        Field[] targetFields = targetBeanClass.getDeclaredFields();
        for (int i = 0; i < sourceFields.length; i++) {
            Field sourceField = sourceFields[i];
            if (Modifier.isStatic(sourceField.getModifiers())) {
                continue;
            }
            Field targetField = targetFields[i];
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            try {
                if (!(sourceField.get(sourceBean) == null) && !"serialVersionUID".equals(sourceField.getName().toString())) {
                    targetField.set(targetBean, sourceField.get(sourceBean));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return targetBean;
    }
}
