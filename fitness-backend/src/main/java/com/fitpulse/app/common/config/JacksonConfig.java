package com.fitpulse.app.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局序列化配置。
 * <p>【核心修复 - JS 大整数精度丢失】
 * <p>MyBatis-Plus 雪花 ID 生成的 {@link Long} 主键为 18~19 位十进制，
 * 超过了 JavaScript 的 {@code Number.MAX_SAFE_INTEGER = 2^53 - 1 ≈ 9e15}，
 * 直接以 JSON number 返回会被浏览器的 JS 引擎舍入截断（末尾变 0），
 * 导致前端「查看详情 / 编辑 / 删除」拿到的 ID 与数据库主键不一致。
 * <p>修复方式：全局把 {@link Long} / {@link BigInteger} 序列化为 JSON 字符串，
 * 前端直接用字符串作为 URL 路径参数传递，不需要参与数值计算。
 *
 * <p>【附加统一】日期时间统一使用 ISO-8610 标准字符串，禁止写时间戳。
 *
 * @author FitPulse
 */
@Configuration
public class JacksonConfig {

    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN     = "yyyy-MM-dd";
    private static final String TIME_PATTERN     = "HH:mm:ss";

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(false).build();

        // ========== 1. 全局模块注册 ==========
        SimpleModule numberModule = new SimpleModule("LongToString");
        // Long / long → 字符串，雪花ID核心修复
        numberModule.addSerializer(Long.class,    ToStringSerializer.instance);
        numberModule.addSerializer(Long.TYPE,     ToStringSerializer.instance);
        // BigInteger → 字符串（兜底，防止后端自定义超大主键）
        numberModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        mapper.registerModule(numberModule);

        // ========== 2. JSR-310 日期时间（统一格式，不写时间戳）==========
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
        timeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        timeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        timeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
        timeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        timeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));
        mapper.registerModule(timeModule);

        // ========== 3. 通用开关 ==========
        // 序列化为日期数组时不写时间戳（兜底，实际应由上面 serializer 生效）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 反序列化时遇到未知字段不抛错（前后端版本迭代兼容）
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 空对象不抛错（VO 里无字段空实例场景）
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return mapper;
    }
}
