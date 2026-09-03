package com.moait.moai.domain.user.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code List<HoldingAsset>} ↔ JSON 문자열 변환.
 *
 * <p>{@code @JdbcTypeCode(SqlTypes.JSON)} 는 MySQL Connector/J 에서 값을 binary 로 바인딩해
 * {@code "Cannot create a JSON value from a string with CHARACTER SET 'binary'"} 오류가 나므로,
 * 컨버터로 String 바인딩(= {@code setString})을 강제한다.
 */
@Converter
public class HoldingAssetsConverter implements AttributeConverter<List<HoldingAsset>, String> {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<HoldingAsset>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<HoldingAsset> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        return MAPPER.writeValueAsString(attribute);
    }

    @Override
    public List<HoldingAsset> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        return MAPPER.readValue(dbData, TYPE);
    }
}
