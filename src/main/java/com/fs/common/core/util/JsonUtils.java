package com.fs.common.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fs.common.core.exception.FsCommonException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.fs.common.core.util.ObjectUtils.getJavaType;


/**
 * Created by kevin on 2017-12-26
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object object) {
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);

        String json = "";
        try {
            json = mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return json;
    }

    public static <T> T toObject(String json, Class<T> valueType) {
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        T object = null;
        try {
            object = mapper.readValue(json, valueType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return object;

    }

    public static <T> T toObject(String json, TypeReference<T> valueTypeRef) {
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        T object = null;
        try {
            object = mapper.readValue(json, valueTypeRef);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return object;
    }

    /**
     * {@literal
     * List<String, Map<VO>>, List<VO>, Map<String, VO> 등의 파싱에 사용하기 위한 메서드
     *
     * Type 혹은 ParameterizedType을 받아올 수 있을 때 사용 가능함
     * JavaType을 받아온 다음 파라미터값으로 집어넣으면 됨
     * a1
     * @param json 받아온 JSON
     * @param type Type 혹은 제네릭 정보가 담긴 ParameterizedType
     * @return 제네릭 타입의 VO까지 캐치하여 역직렬화된 객체
     */
    public static <T> T toObject(String json, Type type) {
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);

        T object = null;
        try {
            object = mapper.readValue(json, getJavaType(type));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return object;
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    private static JsonNode getRootNode(String json) {
        if (StringUtils.isEmpty(json)) return null;

        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private static JsonNode getDataNode(String json, String dataName) {
        JsonNode rootNode = getRootNode(json);
        if (rootNode == null) return null;

        return rootNode.path(dataName);
    }

    private static <T> T getValueFromNode(JsonNode dataNode, Class<T> valueType) {
        try {
            return mapper.readValue(mapper.treeAsTokens(dataNode), valueType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private static <T> T getValueFromNode(JsonNode dataNode, TypeReference<T> valueTypeRef) {
        try {
            return mapper.readValue(mapper.treeAsTokens(dataNode), valueTypeRef);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private static <T> T getValueFromNode(JsonNode dataNode, Type type) {
        try {
            return mapper.readValue(mapper.treeAsTokens(dataNode), getJavaType(type));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static <T> T toDataObject(String json, String dataName, Class<T> valueType) {
        JsonNode dataNode = getDataNode(json, dataName);
        if (dataNode == null) return null;

        return getValueFromNode(dataNode, valueType);
    }

    public static <T> T toDataObject(String json, String dataName, TypeReference<T> valueTypeRef) {
        JsonNode dataNode = getDataNode(json, dataName);
        if (dataNode == null) return null;

        return getValueFromNode(dataNode, valueTypeRef);
    }

    public static <T> T toDataObject(String json, String dataName, Type type) {
        JsonNode dataNode = getDataNode(json, dataName);
        if (dataNode == null) return null;

        return getValueFromNode(dataNode, type);
    }

    public static <T> T toListToMap(String json, String dataName, TypeReference<T> valueTypeRef) {
        JsonNode dataNode = getDataNode(json, dataName);
        if (dataNode == null) return null;

        JsonNode oneNode;
        if (!dataNode.isArray()) {
            oneNode = dataNode;
        } else {
            ArrayNode arrayNode = (ArrayNode) dataNode;

            oneNode = arrayNode.get(0);
        }

        String filterNode = oneNode.toString();

        try {
            return mapper.readValue(filterNode, valueTypeRef);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static <T> ArrayList<T> toListToListMap(String json, String dataName, TypeReference<T> valueTypeRef) {
        JsonNode rootNode = getRootNode(json);
        if (rootNode == null) return null;

        try {
            if (rootNode.get(dataName).isNull()) return null;

            ArrayNode arrayNode = (ArrayNode) rootNode.path(dataName);

            ArrayList<T> convertList = new ArrayList<>();
            for (JsonNode jsonNode : arrayNode) {
                convertList.add(mapper.readValue(jsonNode.toString(), valueTypeRef));
            }

            return convertList;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public static <T> List<T> toVoList(String json, String fieldName, Class<T> valueType) {
        JsonNode rootNode = getRootNode(json);
        if (rootNode == null) return null;

        try {
            JsonNode targetNode = rootNode.get(fieldName);

            List<T> convertList = new ArrayList<>();
            if (!targetNode.isArray()) {
                convertList.add(mapper.readValue(mapper.treeAsTokens(targetNode), valueType));
            } else {
                ArrayNode arrayNode = (ArrayNode) targetNode;
                for (JsonNode jsonNode : arrayNode) {
                    convertList.add(mapper.readValue(mapper.treeAsTokens(jsonNode), valueType));
                }
            }

            return convertList;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    // json 형식 VO List로 변환 ( FieldName 없을 경우 )
    public static <T> List<T> jsonToVOList(String json, Class<T> valueType) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> toVoList(String json, Class<T> valueType, String[]... removeNodeArray) {
        JsonNode rootNode = getRootNode(json);
        if (rootNode == null) return null;

        try {
            List<T> convertList = new ArrayList<>();
            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode jsonNode : arrayNode) {

                    // 특정 Node 반영 X
                    if (null != removeNodeArray && 0 != removeNodeArray.length) {
                        for (String[] removeNode : removeNodeArray) {
                            ((ObjectNode) jsonNode).remove("languageType");
                        }
                    }

                    convertList.add(mapper.readValue(mapper.treeAsTokens(jsonNode), valueType));
                }
            }

            return convertList;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * toObject(String json, Type type) 으로 변경됨
     */
    @Deprecated
    public static <T> T toType(String json, Type type) {
        return toObject(json, type);
    }

    public static List<Object> toTypeList(String json, List<Type> typeList) {
        List<Object> result = new ArrayList<>();

        JsonNode node = getRootNode(json);
        if (node != null && node.isArray()) {
            Iterator<Type> iterator = typeList.iterator();

            node.forEach(element -> {
                if (!iterator.hasNext()) {
                    throw new IllegalArgumentException("content length of json array is too many! (typeList.length() != <json array length>)");
                }

                JsonParser tokens = mapper.treeAsTokens(element);
                JavaType type = getJavaType(iterator.next());

                try {
                    result.add(mapper.readValue(tokens, type));
                } catch (IOException e) {
                    throw new FsCommonException(e);
                }
            });

            if (iterator.hasNext()) {
                throw new IllegalArgumentException("content length of json array is too few! (typeList.length() != <json array length>)");
            }
        } else {
            throw new IllegalArgumentException("json must be array!");
        }

        return result;
    }
}
