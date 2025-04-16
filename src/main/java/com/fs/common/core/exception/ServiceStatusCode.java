package com.fs.common.core.exception;

public enum ServiceStatusCode   {

    // 레거시 코드에 사용되었던 기본적인 상태 코드들
    SUCCESS(100, "[SUCCESS]"),
    FAILED(101, "[FAILED]"),

    // 쿼리 에러를 구분하기 위해 사용되는 상태 코드들
    ERROR_QUERY(200, "[ERROR_QUERY]"),
    ERROR_INSERT(201, "[ERROR_INSERT]"),
    ERROR_READ(202, "[ERROR_READ]"),
    ERROR_UPDATE(203, "[ERROR_UPDATE]"),
    ERROR_DELETE(204, "[ERROR_DELETE]"),
    ERROR_UPLOAD_FILE_SIZE_OVER(301, "[ERROR_UPLOAD_FILE_SIZE_OVER]"),
    ERROR_UPLOAD_FILE_FAIL(302, "[ERROR_UPLOAD_FILE_FAIL]"),
    ERROR_VALIDATE_FILE_EXTENSION(303, "[ERROR_VALIDATE_FILE_EXTENSION]"),          // 확장자 에러
    ERROR_VALIDATE_PATTERN_REGEX(304, "[ERROR_VALIDATE_PATTERN_REGEX]"),            // 정규식(이모지 및 연속된 자음 모음 제외)
    ERROR_JSON_PARSING(305, "[ERROR_JSON_PARSING]"),                                // json 데이터 파싱 에러

    // Open API에 주로 사용되었던 상태 코드들
    /* Parameter가 Null 일때 */
    ERROR_NULL(601, "[ERROR_NULL]"),
    ERROR_NO_PARAM(602, "[ERROR_NULL_PARAM]"), // 필수값이 안들어 왔을 때
    ERROR_NOT_SUPPORT_TYPE(603, "[ERROR_NOT_SUPPORT_TYPE]"),
    ERROR_PARAM_VALIDITY(604, "[ERROR_PARAM_VALIDITY]"),
    ERROR_PARAM_DUPLICATE(605, "[ERROR_PARAM_DUPLICATE]"),
    ERROR_PROCESS_FAILED(606, "[ERROR_PROCESS_FAILED]"),
    ERROR_TRIM(607, "[ERROR_TRIM]"),                                    // trim() 중 에러 발생


    ERROR_NETWORK(701, "[ERROR_NETWORK]"),
    ERROR_INTERNAL_SERVER(702, "[ERROR_INTERNAL_SERVER]"),
    ERROR_INTERNAL_SERVER_DEAD(703, "[ERROR_INTERNAL_SERVER_DEAD]"),

    /* 유저 혹은 인증에 관련된 상태 코드들 */
    // 인증 및 로그인 (82x)
    ERROR_NOT_EXISTS(820, "[ERROR_NOT_EXISTS]"),                    // 존재하지 않음
    ERROR_ACCESS_DENIED(821, "[ERROR_ACCESS_DENIED]"),              // 접근 거부(블랙리스트, 휴면, 탈퇴대기, 잘못된 토큰값 등)
    ERROR_ACCESS_INFO_EXPIRED(822, "[ERROR_ACCESS_INFO_EXPIRED]"),  // 만료된 접근 정보(토큰 만료 등)
    ERROR_ACCOUNT_DORMANT(823, "[ERROR_ACCOUNT_DORMANT]"),          // 휴면 계정
    ERROR_ACCOUNT_WITHDRAW(824, "[ERROR_ACCOUNT_WITHDRAW]"),        // 탈퇴 계정
    ERROR_ACCOUNT_BLACK(825, "[ERROR_ACCOUNT_BLACK]"),              // 차단 계정
    ERROR_SAME_TIME_ACT(826, "[ERROR_SAME_TIME_ACT]"),              // 동시성 에러
    ERROR_NOT_MATCH(828, "[ERROR_NOT_MATCH]"),                      // 일치하지 않은경우
    // 스프링 시큐리티를 이용한 로그인 에러 처리 (85x)
    LOGIN_SUCCESS(200,"[LOGIN_SUCCESS]"), // 로그인 성공 코드를 100으로 보내면 에러가 나서 200성공으로 셋팅

    ERROR_SYSTEM_EXCEPTION(1000, "[ERROR_SYSTEM_EXCEPTION]");




    private int error;
    private String reason;

    ServiceStatusCode(int error, String reason) {
        this.error = error;
        this.reason = reason;
    }

    public int getError() {
        return error;
    }

    public String getReason() {
        return reason;
    }

}
