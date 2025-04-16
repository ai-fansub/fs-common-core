package com.fs.common.core.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Object를 원하는 형태로 형변환 해주는 클래스
 * LinkedHashMap을 vo 형태로 받고 싶어 만들었는데 제네릭에 대한 공부가 더욱 필요하다...
 */
public class ObjectUtils {

    public static String valueToStringOrEmpty(Map<String, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    public static String valueToStringOrEmpty(Map<String, ?> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : value.toString();
    }

    public static boolean isEmpty(Collection<?> coll) {
        return (coll == null || coll.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    public static boolean isEmpty(Map<?, ?> map, Object key) {
        return map.containsKey(key);
    }



    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T toType(Object object, Class<T> valueType) {
        if (object == null) return null;

        return mapper.convertValue(object, valueType);
    }

    public static <T> T toType(Object object, TypeReference<T> valueTypeRef) {
        if (object == null) return null;

        return mapper.convertValue(object, valueTypeRef);
    }

    public static <T> T toType(Object object, Type type) {
        if (object == null) return null;

        return mapper.convertValue(object, getJavaType(type));
    }

    public static <T> List<T> toTypeList(Object object, Class<T> valueType) {
        if (object == null || valueType == null) return null;

        return mapper.convertValue(object, TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, valueType));
    }

    /**
     * 일반 타입일 경우 일반 타입에 대한 JavaType 반환
     *
     * 제네릭 타입일 경우 재귀적인 호출을 통해 모든 구체적인 제네릭 타입이 명시된 JavaType 반환
     *
     * @param type 일반 타입(리플렉션 Type) 혹은 파라미터라이즈 타입(리플렉션 ParameterizedType)
     * @return 제네릭 데이터가 존재하는 JavaType
     */
    public static JavaType getJavaType(Type type) {
        TypeFactory typeFactory = TypeFactory.defaultInstance();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            List<JavaType> javaTypeList = new ArrayList<>();
            for (Type genericType : parameterizedType.getActualTypeArguments()) {
                javaTypeList.add(getJavaType(genericType));
            }

            return typeFactory.constructParametricType((Class<?>) rawType, javaTypeList.toArray(new JavaType[0]));
        } else {
            return typeFactory.constructType(type);
        }
    }

}
