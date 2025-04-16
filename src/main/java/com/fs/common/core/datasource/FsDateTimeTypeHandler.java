package com.fs.common.core.datasource;

import com.fs.common.core.util.FsDateTime;
import com.fs.common.core.util.FsDateTimeUtil;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;

/**
 * {@literal
 * MyBatis 단에서 MyloDateTime 타입을 변환시켜주는 변환기
 *
 * mybatis-config.xml 파일에 <typeHandler> 태그로 등록되어있어야 함
 * }
 */
public class FsDateTimeTypeHandler implements TypeHandler<FsDateTime> {
    @Override
    public void setParameter(PreparedStatement ps, int i, FsDateTime parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setTimestamp(i, null);
        } else {
            long epochMillis = parameter.getUnixMillis();
            ps.setTimestamp(i, new Timestamp(epochMillis));
        }
    }

    @Override
    public FsDateTime getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return getMyloDateTime(rs.getTimestamp(columnName));
        } catch (SQLException e) {
            return getMyloDateTime(rs.getString(columnName));
        }
    }

    @Override
    public FsDateTime getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return getMyloDateTime(rs.getTimestamp(columnIndex));
        } catch (SQLException e) {
            return getMyloDateTime(rs.getString(columnIndex));
        }
    }

    @Override
    public FsDateTime getResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return getMyloDateTime(cs.getTimestamp(columnIndex));
        } catch (SQLException e) {
            return getMyloDateTime(cs.getString(columnIndex));
        }
    }

    private FsDateTime getMyloDateTime(Timestamp timestamp) {
        // DB의 DATE 혹은 DATETIME 컬럼 값이 null일 경우 VO에서도 null을 집어넣게 함
        if (timestamp == null) return null;

        long epochMillis = timestamp.getTime();
        // DB의 시간대가 UTC일 것이라고 가정하고 불러옴
        return FsDateTimeUtil.getDateTimeFrom(FsDateTimeUtil.UTC, epochMillis);
    }

    private FsDateTime getMyloDateTime(String string) {
        // DB의 DATE 혹은 DATETIME 컬럼 값이 null일 경우 VO에서도 null을 집어넣게 함
        if (string == null) return null;

        // DB의 시간대가 UTC일 것이라고 가정하고 불러옴
        return FsDateTimeUtil.getDateTimeFrom(FsDateTimeUtil.UTC, string);
    }
}
