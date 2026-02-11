package com.fzg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.LONGVARCHAR})
@MappedTypes(Map.class)
public class JsonMapTypeHandler extends BaseTypeHandler<Map<Long, Double>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(
            PreparedStatement ps,
            int i,
            Map<Long, Double> parameter,
            JdbcType jdbcType) throws SQLException {

        try {
            ps.setString(i, MAPPER.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new SQLException("Json serialize error", e);
        }
    }

    @Override
    public Map<Long, Double> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Map<Long, Double> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Map<Long, Double> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private Map<Long, Double> parse(String json) throws SQLException {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return MAPPER.readValue(
                    json,
                    new TypeReference<Map<Long, Double>>() {}
            );
        } catch (Exception e) {
            throw new SQLException("Json deserialize error: " + json, e);
        }
    }
}
