-- ============================================================
-- FitPulse 数据库迁移 V2: user_profile 字段扩展
-- 日期: 2026-08-19
-- 说明: 在现有 user_profile 表追加 weight_kg / body_fat_pct / fitness_level / theme 字段
--       不重写 schema.sql，保留已有数据
-- ============================================================

USE fitpulse_db;

-- user_profile 表追加字段
ALTER TABLE user_profile
    ADD COLUMN weight_kg      DECIMAL(5,1) NULL COMMENT '当前体重kg（缓存最新值，避免联表查 body_metric 历史）' AFTER height_cm,
    ADD COLUMN body_fat_pct   DECIMAL(4,1) NULL COMMENT '当前体脂率%（缓存最新值）' AFTER weight_kg,
    ADD COLUMN fitness_level  TINYINT      NULL COMMENT '健身等级 1=入门 2=进阶 3=达人 4=专业' AFTER body_fat_pct,
    ADD COLUMN theme          TINYINT      NULL DEFAULT 1 COMMENT '主题偏好 1=浅色 2=深色 3=跟随系统' AFTER fitness_level;
