package com.tariff.calculation.tariffCalc.category;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = false)
public class FloatArrayConverter implements AttributeConverter<float[], String> {

    private static final Logger log = LoggerFactory.getLogger(FloatArrayConverter.class);

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) {
            // log.info("FloatArrayConverter: Converting null array to database");
            return null;
        }
        
        // log.info("FloatArrayConverter: Converting float[] of length {} to database string", attribute.length);
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(attribute[i]);
        }
        sb.append("]");
        
        String result = sb.toString();
        // log.info("FloatArrayConverter: Converted to string (length: {}, first 100 chars: {})", 
                // result.length(), 
                // result.length() > 100 ? result.substring(0, 100) + "..." : result);
        
        return result;
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            // log.info("FloatArrayConverter: Converting null database string to array");
            return null;
        }
        
        // log.info("FloatArrayConverter: Converting database string to float[] (length: {}, first 100 chars: {})", 
        //         dbData.length(), 
        //         dbData.length() > 100 ? dbData.substring(0, 100) + "..." : dbData);
        
        String s = dbData.trim();
        if (s.length() < 2) {
            // log.info("FloatArrayConverter: Database string too short: '{}'", s);
            return new float[0];
        }
        
        // if (!s.startsWith("[") || !s.endsWith("]")) {
        //     log.info("FloatArrayConverter: Database string missing brackets: '{}'", 
        //             s.length() > 50 ? s.substring(0, 50) + "..." : s);
        // }
        
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) {
            // log.info("FloatArrayConverter: Empty array content, returning empty float[]");
            return new float[0];
        }
        
        String[] parts = s.split(",");
        float[] out = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Float.parseFloat(parts[i].trim());
            } catch (NumberFormatException e) {
                out[i] = 0.0f;
            }
        }
        return out;
    }
}
