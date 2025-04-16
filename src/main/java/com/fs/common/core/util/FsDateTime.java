package com.fs.common.core.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fs.common.core.exception.FsCommonException;
import com.fs.common.core.exception.ErrorType;
import com.fs.common.core.exception.ServiceStatusCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.*;

/**
 * 시간 및 시간대에 대한 정보 저장, 연산 및 출력을 할 수 있는 객체
 * 기존 시간 관련 연산 및 시간대가 꼬이는 문제에 대한 해결을 위해 제작함
 *
 * 기본적으로 시간대를 무조건 명시하게끔 함으로써 개발자의 실수를 줄이고자 함
 *
 * 똑같은 시간을 복제하여 값을 변경하고 비교 대상으로 사용 등의 작업을 위해 Cloneable 인터페이스 및 clone() 메서드 구현
 * 또한, 해당 객체는 직렬화 가능한 객체라는 것에 대한 명시를 위해 Serializable 인터페이스 구현
 * 시간의 비교를 위한 Comparable 인터페이스 및 compareTo() 메서드를 구현함
 *
 * 기본적으로 생성자를 비활성화한 대신 createDateTimeFrom() 메서드를 이용해 생성 가능, 이때 시간대는 무조건 명시해야하며
 * 시간은 LocalDateTime 혹은 Instant 중 어떤 클래스로 가져와도 무방함
 *
 * 이후 생성된 객체에 대해선 빌더 패턴처럼 사용이 가능함:
 *  MyloDateTime.createDateTimeFrom(...).addYears(1).setDay(1).get("yyyyMMdd HH:mm:ss");
 */
@Slf4j
@JsonSerialize(using = FsDateTimeUtil.HtDateTimeSerializer.class)
@JsonDeserialize(using = FsDateTimeUtil.HtDateTimeDeserializer.class)
public class FsDateTime implements Cloneable, Serializable, Comparable<FsDateTime> {
    // TODO Javadoc 형태의 메서드 주석 모두 작성할 것
    /*
     * 내부적으로 시간대(ZoneId)와 시간(LocalDateTime)을 저장함
     * 연산 시 시간 데이터로만 연산하되, 시간대가 필요해지는 convertTo(ZoneId) 메서드 등에서는 시간대(ZoneId)가 사용됨
     */
    private ZoneId zone;
    private LocalDateTime localDateTime;

    private FsDateTime setZone(ZoneId zone) {
        this.zone = zone;
        return this;
    }

    private FsDateTime setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }

    /*
     * 기본 생성자를 막은 대신 createDateTimeFrom() 메서드로 시간을 가져올 수 있음
     * createDateTimeFrom() 메서드는 무조건 시간대를 명시해야 함
     * (시간대 누락으로 잘못된 시간이 입력되는 것을 방지)
     * 명시된 시간대는 zoneId 필드에 저장되어 있다가, convertTo() 메서드 등에 사용됨
     */
    private FsDateTime(ZoneId zone, LocalDateTime localDateTime) {
        this.zone = zone;
        this.localDateTime = localDateTime;
    }

    public static class Builder {
        private ZoneId zone;
        private LocalDateTime localDateTime;

        public Builder zone(ZoneId zone) {
            this.zone = zone;
            return this;
        }

        public Builder localDateTime(LocalDateTime localDateTime) {
            this.localDateTime = localDateTime;
            return this;
        }

        public FsDateTime build() {
            return new FsDateTime(zone, localDateTime);
        }
    }

    /*
     * LocalDateTime 클래스와는 다르게 가변객체이다 보니 해당 객체를 복사해서 독립적인 시간 객체를 만들고 싶을 때
     * 사용할 수 있도록 clone() 메서드 구현
     */
    @Override
    public FsDateTime clone() {
        try {
            return (FsDateTime) super.clone();
        } catch (CloneNotSupportedException e) {
            // Cloneable 인터페이스를 구현받았고, Object 클래스를 바로 상속받기에 문제는 없지만
            // 나중에 구조가 바뀌게 된다면 에러가 발생할 수 있으므로, 주의바람
            log.error("Can't clone HtDateTime object!", e);
            throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_SYSTEM_EXCEPTION, e);
        }
    }

    /*
     * 시간 관련 연산을 하는 메서드
     * 시간의 비교, 특정 값으로 설정, 덧셈 및 뺄셈 등의 기능을 함
     */
    public boolean isEqual(FsDateTime target) {
        return this.localDateTime.isEqual(target.localDateTime);
    }

    // yyyy-MM-dd 비교, 즉 연월일이 동일한지 확인
    public boolean isDateEqual(FsDateTime target) {
        return (this.getYear().equals(target.getYear()) && this.getMonth().equals(target.getMonth())
                && this.getDay().equals(target.getDay()));
    }
    public boolean isBefore(FsDateTime target) {
        if (!this.zone.equals(target.zone)) throw new IllegalArgumentException("FsDateTime.isBefore(FsDateTime target) - Can't compare time with different timezone! this.zone: '"+this.zone+"', target.zone: '"+target.zone+"'");

        return this.localDateTime.isBefore(target.localDateTime);
    }

    public boolean isAfter(FsDateTime target) {
        if (!this.zone.equals(target.zone)) throw new IllegalArgumentException("FsDateTime.isAfter(FsDateTime target) - Can't compare time with different timezone! this.zone: '"+this.zone+"', target.zone: '"+target.zone+"'");

        return this.localDateTime.isAfter(target.localDateTime);
    }

    public boolean isBetween(FsDateTime start, FsDateTime end) {
        return this.isAfter(start) && this.isBefore(end);
    }

    public FsDateTime setNano(int nano) {
        return set(NANO_OF_SECOND, nano);
    }
    public FsDateTime setSecond(int second) {
        return set(SECOND_OF_MINUTE, second);
    }
    public FsDateTime setMinute(int minute) {
        return set(MINUTE_OF_HOUR, minute);
    }
    public FsDateTime setHour(int hour) {
        return set(HOUR_OF_DAY, hour);
    }

    public FsDateTime setDay(int day) {
        return set(DAY_OF_MONTH, day);
    }
    public FsDateTime setMonth(int month) {
        return set(MONTH_OF_YEAR, month);
    }
    public FsDateTime setYear(int year) {
        return set(YEAR, year);
    }

    public FsDateTime setDayOfWeek(DayOfWeek dayOfWeek) {
        // 달력을 켰을 때 현재 주의 "월화수목금토일" 중 일요일에 위치시킨 후, 그 주(행, 가로줄)의 dayOfWeek 요일을 선택함 (월요일을 기준점으로)
        // 일요일을 한 주의 시작일로 봤을 때 기준으로 작성한 로직 (Calendar.set(Calendar.DAY_OF_WEEK, param); 과 동일한 결과를 내는 로직)
        return set(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).set(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    public FsDateTime setDayToLastOfMonth() {
        // 현재 날짜 기준으로 이달의 마지막 날로 설정
        return set(TemporalAdjusters.lastDayOfMonth());
    }

    public FsDateTime addYears(int amount) {
        return add(amount, YEARS);
    }
    public FsDateTime addMonths(int amount) {
        return add(amount, MONTHS);
    }
    public FsDateTime addWeeks(int amount) {
        return add(amount, WEEKS);
    }
    public FsDateTime addDays(int amount) {
        return add(amount, DAYS);
    }
    public FsDateTime addHours(int amount) {
        return add(amount, HOURS);
    }
    public FsDateTime addMinutes(int amount) {
        return add(amount, MINUTES);
    }
    public FsDateTime addSeconds(int amount) {
        return add(amount, SECONDS);
    }


    public FsDateTime subtractYears(int amount) {
        return subtract(amount, YEARS);
    }
    public FsDateTime subtractMonths(int amount) {
        return subtract(amount, MONTHS);
    }
    public FsDateTime subtractWeeks(int amount) {
        return subtract(amount, WEEKS);
    }
    public FsDateTime subtractDays(int amount) {
        return subtract(amount, DAYS);
    }
    public FsDateTime subtractHours(int amount) {
        return subtract(amount, HOURS);
    }
    public FsDateTime subtractMinutes(int amount) {
        return subtract(amount, MINUTES);
    }
    public FsDateTime subtractSeconds(int amount) {
        return subtract(amount, SECONDS);
    }

    /*
     * 내부 시간대 변경 메서드, 시간대가 변환됨에 따라 내부에 저장된 시간도 변화함
     */
    public FsDateTime convertTo(ZoneId zoneTo) {
        ZoneId zoneFrom = this.zone;

        LocalDateTime convertedDateTime = this.localDateTime.atZone(zoneFrom).withZoneSameInstant(zoneTo).toLocalDateTime();
        return setLocalDateTime(convertedDateTime).setZone(zoneTo);
    }
    public FsDateTime changeZone(ZoneId zoneTo) {
        setZone(zoneTo);
        return this;
    }
    public ZoneId getZone(FsDateTime target) {
        return target.zone;
    }

    /*
     * 형식 문자열을 가져오거나, 일부 데이터를 추출하여 가져오는 메서드
     * 시간을 문자열 혹은 숫자로 가공하고 싶을 때 사용함
     */
    public String get(DateTimeFormatter formatter) {
        return this.localDateTime.format(formatter);
    }
    public String get(String pattern) {
        return get(DateTimeFormatter.ofPattern(pattern));
    }

    // int 대신 Integer, Wrapper 클래스 사용 -> getYear().toString() 등 문자열 값으로의 치환을 직관적으로 사용 가능
    public Integer getYear() {
        return this.localDateTime.getYear();
    }
    public Integer getMonth() {
        return this.localDateTime.getMonth().getValue();
    }
    public Integer getDay() {
        return this.localDateTime.getDayOfMonth();
    }

    public DayOfWeek getDayOfWeek() {
        return this.localDateTime.getDayOfWeek();
    }

    public Integer getHour() {
        return this.localDateTime.getHour();
    }
    public Integer getMinute() {
        return this.localDateTime.getMinute();
    }
    public Integer getSecond() {
        return this.localDateTime.getSecond();
    }
    public Integer getNano() {
        return this.localDateTime.getNano();
    }

    // 유닉스 타임스탬프(밀리초단위)로 리턴
    public Long getUnixSeconds() {
        return this.localDateTime.atZone(zone).toInstant().getEpochSecond();
    }
    // 유닉스 타임스탬프(밀리초단위)로 리턴
    public Long getUnixMillis() {
        return this.localDateTime.atZone(zone).toInstant().toEpochMilli();
    }

    /*
     * toString() 메서드는 기본적으로 저장된 시간을 ISO 표준 포맷으로 보여줌
     */
    @Override
    public String toString() {
        return get(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /*
     * Comparable<HtDateTime> 인터페이스 구현을 위해 필요한 메서드
     * 시간순 정렬이 필요할 때를 위해 제작해둠
     */
    @Override
    public int compareTo(FsDateTime another) {
        return this.localDateTime.compareTo(another.localDateTime);
    }


    /*
     * 비교 연산시 필요한 equals() 및 hashCode() 메서드 오버라이드
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof FsDateTime) {
            FsDateTime another = (FsDateTime) object;
            return (this.localDateTime.isEqual(another.localDateTime)) && this.zone.equals(another.zone);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return this.localDateTime.hashCode();
    }

    /*
     * 내부에서만 사용하는 로직
     * LocalDateTime 클래스의 시간 관련 연산 메서드를 HtDateTime 내부에서 사용할 수 있게끔 재정의
     */
    private FsDateTime set(TemporalAdjuster adjuster) {
        LocalDateTime convertedDateTime = this.localDateTime.with(adjuster);
        return setLocalDateTime(convertedDateTime);
    }

    private FsDateTime set(TemporalField field, long newValue) {
        LocalDateTime convertedDateTime = this.localDateTime.with(field, newValue);
        return setLocalDateTime(convertedDateTime);
    }

    private FsDateTime add(TemporalAmount amount) {
        LocalDateTime convertedDateTime = this.localDateTime.plus(amount);
        return setLocalDateTime(convertedDateTime);
    }

    private FsDateTime add(long amount, TemporalUnit unit) {
        LocalDateTime convertedDateTime = this.localDateTime.plus(amount, unit);
        return setLocalDateTime(convertedDateTime);
    }

    private FsDateTime subtract(TemporalAmount amount) {
        LocalDateTime convertedDateTime = this.localDateTime.minus(amount);
        return setLocalDateTime(convertedDateTime);
    }

    private FsDateTime subtract(long amount, TemporalUnit unit) {
        LocalDateTime convertedDateTime = this.localDateTime.minus(amount, unit);
        return setLocalDateTime(convertedDateTime);
    }
}