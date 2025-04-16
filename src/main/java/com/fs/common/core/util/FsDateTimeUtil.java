package com.fs.common.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fs.common.core.exception.FsCommonException;
import com.fs.common.core.exception.ErrorType;
import com.fs.common.core.exception.ServiceStatusCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * HtDateTime 객체의 생성을 돕고, 시간 사이의 비교와 시간 관련으로 편하게 접근 가능한 상수들을 정의한 유틸리티 클래스
 *
 * 생성을 위해 getNow(시간대) 및 getDateTimeFrom(시간 및 시간대)를 사용 가능함
 * 시간 관련 작업이 필요한 경우 다음 import 문 사용 후 getNow() 및 getDateTimeFrom() 메서드로 시작하여 작업을 진행하면 됨
 * <pre>
 *     import static com.hanteo.common.core.util.HtDateTimeUtil.*;
 * </pre>
 */
@Slf4j
public class FsDateTimeUtil {
    /*
     * 유틸리티에서 편하게 사용할 수 있게 지정해둔 상수 목록
     */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static final ZoneId CST = ZoneId.of("Asia/Shanghai");
    public static final ZoneId UTC = ZoneOffset.UTC;

    public static final String ISO_DATE = "yyyy-MM-dd";
    public static final String ISO_TIME = "HH:mm:ss";
    public static final String ISO_DATE_TIME = ISO_DATE+"'T'"+ISO_TIME;

    public static final String YYYYMMDD_DOT = "yyyy.MM.dd"; // Dot
    public static final String YYYYMMDD_HYP = "yyyy-MM-dd"; // Hyphen
    public static final String YYYYMMDD_SLA = "yyyy/MM/dd"; // Slash

    public static final String YYYYMMDD_DOT_HHMM = "yyyy.MM.dd HH:mm"; // Dot
    public static final String YYYYMMDD_HYP_HHMM = "yyyy-MM-dd HH:mm"; // Hyphen

    public static final String YYYYMMDD_DOT_HHMMSS = "yyyy.MM.dd HH:mm:ss"; // Dot
    public static final String YYYYMMDD_HYP_HHMMSS = "yyyy-MM-dd HH:mm:ss"; // Hyphen

    public static final String YYYYMMDD_HYP_HHMMSS_SSSSSS = "yyyy.MM.dd HH:mm:ss.SSSSSS"; // Hyphen

    /*
     * 시간의 비교 관련 유틸리티 메서드
     */
    public static int getDaysBetween(String start, String end) {
        return getDaysBetween(getDateTimeFromUTC(start), getDateTimeFromUTC(end));
    }
    public static int getDaysBetween(FsDateTime start, FsDateTime end) {
        LocalDate startDate = LocalDate.of(start.getYear(), start.getMonth(), start.getDay());
        LocalDate endDate = LocalDate.of(end.getYear(), end.getMonth(), end.getDay());

        return (int) DAYS.between(startDate, endDate);
    }

    public static int getDateTimeBetween(ChronoUnit unit, String start, String end) {
        return getDateTimeBetween(unit, getDateTimeFromUTC(start), getDateTimeFromUTC(end));
    }
    public static int getDateTimeBetween(ChronoUnit unit, FsDateTime start, FsDateTime end) {
        LocalDateTime startDate = LocalDateTime.of(start.getYear(), start.getMonth(), start.getDay(), start.getHour(), start.getMinute(), start.getSecond());
        LocalDateTime endDate = LocalDateTime.of(end.getYear(), end.getMonth(), end.getDay(), end.getHour(), end.getMinute(), end.getSecond());

        return (int) unit.between(startDate, endDate);
    }

    public static boolean isEqual(FsDateTime a, FsDateTime b) {
        return (a == null && b == null) || (a != null && a.isEqual(b));
    }

    public static boolean isSameOnFormat(String pattern, FsDateTime a, FsDateTime b) {
        if (a == null || b == null) return false;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return a.get(formatter).equals(b.get(formatter));
    }

    public static boolean isSameDate(FsDateTime a, FsDateTime b) {
        return isSameOnFormat(ISO_DATE, a, b);
    }

    public static boolean hasDate(String input) {
        if (input == null) return false;

        return getDatePattern().matcher(input).find();
    }
    public static boolean hasTime(String input) {
        if (input == null) return false;

        return getTimePattern().matcher(input).find();
    }
    public static boolean isDateTime(String input) {
        return hasDate(input) && hasTime(input);
    }

    /*
     * FsDateTime 객체를 좀 더 편하게 생성할 수 있도록 도와주는 유틸리티 메서드
     */
    public static FsDateTime getDateTimeFrom(ZoneId zone, String input) {
        LocalDateTime localDateTime = createLocalDateTime(zone, input);

        if (localDateTime != null) {
            FsDateTime.Builder builder = new FsDateTime.Builder();
            return builder.zone(zone).localDateTime(localDateTime).build();
        } else {
            return null;
        }
    }

    public static FsDateTime getDateTimeFrom(ZoneId zone, String input, String format) throws FsCommonException {
        SimpleDateFormat legacyFormatter = new SimpleDateFormat(format);
        Date legacyDate;

        try {
            legacyDate = legacyFormatter.parse(input);
        } catch (ParseException e) {
            throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_PARAM_VALIDITY, e);
        }

        LocalDateTime localDateTime = legacyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        FsDateTime.Builder builder = new FsDateTime.Builder();
        return builder.zone(zone).localDateTime(localDateTime).build();
    }

    public static FsDateTime getDateTimeFrom(ZoneId zone, long epochMillis) {
        Instant instant = Instant.ofEpochMilli(epochMillis);

        FsDateTime.Builder builder = new FsDateTime.Builder();
        return builder.zone(zone).localDateTime(instant.atZone(zone).toLocalDateTime()).build();
    }

    public static FsDateTime getDateTimeFromUTC(String input) {
        return getDateTimeFrom(UTC, input);
    }

    public static FsDateTime getDateTimeFromUTC(String input, String format) throws FsCommonException {
        return getDateTimeFrom(UTC, input, format);
    }

    public static FsDateTime getDateTimeFromUTC(long epochMillis) {
        return getDateTimeFrom(UTC, epochMillis);
    }
    
    public static FsDateTime getNow(ZoneId zone) {
        FsDateTime.Builder builder = new FsDateTime.Builder();
        return builder.zone(zone).localDateTime(ZonedDateTime.now(zone).toLocalDateTime()).build();
    }
    public static FsDateTime getNowUTC() {
        return getNow(UTC);
    }

    /*
     * 내부에서만 사용하는 로직
     * 정규 표현식을 이용해 문자열에서 시간과 시간대 관련 패턴을 찾아내거나, 문자열로 시간을 만들어내는 기능 등을 수행함
     */
    private static Pattern getDatePattern() {
        // 정규표현식: 년월일(사이에 구분자 존재) 형태, 각각 year, month, day 캡쳐그룹으로 가져올 수 있음
        // ex. 2020-01-23, 2021/02/27, 2002.1.1 ...
        return Pattern.compile("^(?<year>\\d+)([./-])(?<month>\\d{1,2})\\2(?<day>\\d{1,2})(?:[^\\d]|$)");
    }
    private static Pattern getSimpleDatePattern() {
        // 정규표현식: 년월일(사이에 구분자 없음) 형태, 각각 year, month, day 캡쳐그룹으로 가져올 수 있음
        // ex. 951103, 19950201 ...
        return Pattern.compile("^(?<year>(?:\\d*\\d{2})?\\d{2})(?<month>\\d{2})(?<day>\\d{2})$");
    }
    private static Pattern getTimePattern() {
        // 정규표현식: 시:분[:초[.나노초]] 형태, 각각 hour, minute, second, nano 캡쳐그룹으로 가져올 수 있음
        // ex. 13:00, 9:30:24, 19:27:30.1324
        return Pattern.compile("(?:^|[^\\d])(?<hour>\\d{1,2}):(?<minute>\\d{1,2})(?::(?<second>\\d{1,2})(?:\\.(?<nano>\\d{1,9}))?)?$");
    }

    private static LocalDateTime createLocalDateTime(ZoneId zone, String input) {
        LocalDate date = null;
        boolean hasDate = true;
        //input에 Z가 붙어있다면 지워야함
        input = input.replaceAll("Z$", "");
        // 먼저 일반적인 형식 (21-04-26, 2021.06.08 등)의 날짜를 검색
        // 이후 없다면 심플 형식(940826, 19921105 등)의 날짜를 검색
        Matcher dateMatcher = getDatePattern().matcher(input);
        if (!dateMatcher.find()) {
            dateMatcher = getSimpleDatePattern().matcher(input);
            if (!dateMatcher.find()) {
                hasDate = false;
            }
        }

        // 위에서 검색된 날짜가 없다면 날짜가 존재하지 않는다는 뜻, 오늘 날짜로 설정함
        if (hasDate) {
            int year;
            // 스트링으로 가져온 날짜가 0000년 (00년은 2000년생을 의미)일 경우 0년이며, 0년에 대한 처리로 넘김
            // 만약 날짜가 5자리 규격이 나오거나 3자리 규격이 나오기 시작해서 000년 혹은 00000년이 생긴다면 이에 대한 처리를 해주길 바람
            if ("0000".equals(dateMatcher.group("year"))) {
                // 생일을 기준으로 작성, 생일의 기록 이유가 나이를 기록하기 위함으로 0년일 경우 의미없는 데이터
                // 따라서 null을 리턴하여 DB에 null이 작성되도록 함, 대신 경고를 날려 의도치 않은 작업일 경우 확인하도록 함
                log.warn("Input HtDateTime '{}' has zero year! returning 'NULL' (If it's not birthday, please check HtDateTimeUtil.createLocalDateTime())", input);
                return null;
            } else {
                year = Integer.parseInt(dateMatcher.group("year"));
                if (year < 100) {
                    // 로그를 위해 기존 year int값 복사
                    int yearBeforeProcess = year;

                    // 97, 02 등 2자리 년도가 입력될 경우 앞에 19 혹은 20을 붙여줌
                    // 기준점은 50년을 기준으로 50년 이하는 21세기, 50년 초과는 20세기로 계산
                    if (year > 50) {
                        year += 1900;
                    } else {
                        year += 2000;
                    }

                    log.warn("Input HtDateTime '{}' has 2 digits year! converting year '{}' to '{}' (If it's unexpected, please check HtDateTimeUtil.createLocalDateTime())", input, yearBeforeProcess, year);
                }
            }
            int month = Integer.parseInt(dateMatcher.group("month"));
            int day = Integer.parseInt(dateMatcher.group("day"));
            if (month == 0 || day == 0) {
                // 생일을 기준으로 작성, 생일의 기록 이유가 나이를 기록하기 위함으로 0월 혹은 0일일 경우 년도만 기록하기 위해
                // 0월은 1월으로, 0일은 1일로 바꿈, 대신 경고를 날려 의도치 않은 작업일 경우 확인하도록 함
                if (month == 0) month = 1;
                if (day == 0) day = 1;
                log.warn("Input HtDateTime '{}' has zero month or zero day! returning '{}-{}-{}' (If it's not birthday, please check HtDateTimeUtil.createLocalDateTime())", input, year, month, day);
                date = LocalDate.of(year, 1, 1);
            }

            date = LocalDate.of(year, month, day);
        }

        LocalTime time = createLocalTime(input);

        // date 및 time 둘 다 존재하지 않을 경우 잘못된 파라미터라는 예외를 던져줌
        if (date == null && time == null) throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_PARAM_VALIDITY,
                MessageFormat.format("Wrong HtDateTime input, please check parameter (input text: {0})", input));

        // dateString 파라미터에 날짜가 없을 경우 디폴트 값: 이 메서드를 실행한 시간 기준으로 오늘 날짜
        date = date == null ? LocalDate.now(zone) : date;
        // dateString 파라미터에 시간이 없을 경우 디폴트 값: 0시 0분 0초
        time = time == null ? LocalTime.of(0, 0, 0, 0) : time;

        return LocalDateTime.of(date, time);
    }

    private static LocalTime createLocalTime(String input) {
        Matcher timeMatcher = getTimePattern().matcher(input);
        if (!timeMatcher.find()) return null;

        int hour = Integer.parseInt(timeMatcher.group("hour"));

        int minute = Integer.parseInt(timeMatcher.group("minute"));

        int second = 0; // 초 기본값: 0
        if (timeMatcher.group("second") != null) {
            second = Integer.parseInt(timeMatcher.group("second"));
        }

        int nano = 0; // 나노초 기본값: 0
        if (timeMatcher.group("nano") != null) {
            // 문자열에서 뽑아온 나노초의 나머지 자릿수가 공란일 경우 의도한 값을 적용하려면 자릿수만큼 0을 채워줘야 함
            // ex. 19:12:07.12가 들어왔을 때 19:12:07.000000012가 아닌 19:12:07.120000000을 의미함
            int zeroes = (int) Math.pow(10, (9 - timeMatcher.group("nano").length()));

            nano = Integer.parseInt(timeMatcher.group("nano"))*zeroes;
        }

        if (hour < 0 || hour >= 24 || minute < 0 || minute >= 60 || second < 0 || second >= 60 || nano < 0 || nano >= 1000000000) {
            throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_PARAM_VALIDITY, "Wrong time(hour="+hour+",minute="+minute+",second="+second+",nano="+nano+"), please check your input");
        } else {
            return LocalTime.of(hour, minute, second, nano);
        }
    }

    public static class HtDateSerializer extends JsonSerializer<FsDateTime> {
        @Override
        public void serialize(FsDateTime fsDateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(fsDateTime.get(ISO_DATE));
        }
    }

    public static class HtDateTimeSerializer extends JsonSerializer<FsDateTime> {
        @Override
        public void serialize(FsDateTime fsDateTime, JsonGenerator generator, SerializerProvider provider) throws IOException {
            if (fsDateTime.getZone(fsDateTime).getId().equals("Z")) {
                generator.writeString(fsDateTime.get(ISO_DATE_TIME) +"Z");
            }
            else {
                generator.writeString(fsDateTime.get(ISO_DATE_TIME));
            }
        }
    }

    public static class HtDateTimeDeserializer extends JsonDeserializer<FsDateTime> {
        @Override
        public FsDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
            String isoDateTime = parser.getValueAsString();

            // 받아온 시간이 UTC라고 가정하고 변환함
            try {
                return getDateTimeFromUTC(isoDateTime);
            } catch (FsCommonException e) {
                throw new RuntimeException(MessageFormat.format("Wrong FsDateTime input, please check parameter (input text: {0})", isoDateTime));
            }
        }
    }

}