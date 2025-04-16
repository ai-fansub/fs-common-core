package com.fs.common.core.util;

import com.fs.common.core.exception.FsCommonException;
import com.fs.common.core.exception.ServiceStatusCode;
import com.fs.common.core.model.ResultInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

@Component
public class ResultInfoUtil {
    public static final String ADDITIONAL_RESULT_DATA = "AdditionalResultData";
    /**
     *  RestApi Return시 사용
     *  Result
     *  Code : 반드시 성공/실패 포함
     *  Message : 검색한 키워드(idx) or null or input messageVal
     *  ResultMap : result 그대로 전달.
     */
    public static void setAdditionalResultData(Object resultData) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        attributes.setAttribute(ADDITIONAL_RESULT_DATA, resultData, RequestAttributes.SCOPE_REQUEST);
    }
    public static Object getAdditionalResultData() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        return attributes.getAttribute(ADDITIONAL_RESULT_DATA, RequestAttributes.SCOPE_REQUEST);
    }

    public static ResultInfo setResultInfo(Object result) {
        return setResultInfoForMap(ServiceStatusCode.SUCCESS, result, null);
    }


    public static ResultInfo setResultInfo(ServiceStatusCode code, Object result) {
        return setResultInfoForMap(code, result, null);
    }

    public static ResultInfo setResultInfo(ServiceStatusCode code, Object result, int keyword) {
        return setResultInfoForMap(code, result, String.valueOf(keyword));
    }

    public static ResultInfo setResultInfo(ServiceStatusCode code, Object result, String msg) {
        return setResultInfoForMap(code, result, msg);
    }

    public static ResultInfo setResultInfo(ServiceStatusCode code, Map<String, Object> resultData) {
        return setResultInfoForMap(code, resultData, null);
    }

    public static ResultInfo setResultInfo(ServiceStatusCode code, Map<String, Object> resultData, String msg) {
        return setResultInfoForMap(code, resultData, msg);
    }
    public static ResultInfo setResultInfo(FsCommonException e) {
        return setResultInfoForMap(e);
    }
    public static ResultInfo setResultInfo(FsCommonException e, Object result) {
        return setResultInfoForMap(e, result);
    }

    private static ResultInfo setResultInfoForMap(ServiceStatusCode code, Object resultData, String msg) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(code.getError());
        resultInfo.setData(resultData);
        resultInfo.setMessage(msg);
        return resultInfo;
    }

    private static ResultInfo setResultInfoForMap(FsCommonException e) {
        return setResultInfoForMap(e, null);
    }
    private static ResultInfo setResultInfoForMap(FsCommonException e, Object result) {
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCode(e.getReason().getError());
        resultInfo.setData(result);
        resultInfo.setMessage(e.getMessage());
        return resultInfo;
    }


}
