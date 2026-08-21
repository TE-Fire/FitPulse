-- ============================================================
-- FitPulse 数据库迁移 V3: training 模块字段扩展 + 预置动作
-- 日期: 2026-08-21
-- 说明: 严格对齐 schema.sql 真实表结构：
--       1) exercise 表追加 muscle_group（原 schema 无此字段，分类展示用）
--       2) workout_plan 表追加 status + started_at + completed_at + actual_duration_sec
--          （原 schema 只有 plan_type/estimated_min，缺状态追踪字段）
--       3) workout_plan_exercise 表追加 target_weight_kg（计划默认重量）
--       4) 预置 10 个系统级动作（category 对齐 schema 的 TINYINT 枚举）
-- ============================================================

USE fitpulse_db;

-- ===== 1. exercise 表追加 muscle_group 字段 =====
ALTER TABLE exercise
    ADD COLUMN muscle_group VARCHAR(128) NULL COMMENT '目标肌群，如胸大肌、股四头肌' AFTER equipment;

-- ===== 2. workout_plan 表追加状态追踪字段（追加到表尾，避免依赖不存在的列） =====
ALTER TABLE workout_plan
    ADD COLUMN status              TINYINT    NOT NULL DEFAULT 0 COMMENT '计划状态 0=DRAFT草稿 1=IN_PROGRESS训练中 2=COMPLETED已完成 3=CANCELLED已取消' AFTER deleted,
    ADD COLUMN started_at          DATETIME   NULL COMMENT '开始训练时间戳，start接口写入' AFTER status,
    ADD COLUMN completed_at        DATETIME   NULL COMMENT '完成训练时间戳，complete接口写入' AFTER started_at,
    ADD COLUMN actual_duration_sec INT        NULL COMMENT '实际训练时长秒，前端计时器累计提交' AFTER completed_at;

-- ===== 3. workout_plan_exercise 追加默认建议重量 =====
ALTER TABLE workout_plan_exercise
    ADD COLUMN target_weight_kg DECIMAL(8,2) NULL COMMENT '计划建议重量kg，用户录入时可快速填充' AFTER rest_sec;

-- ===== 4. 预置系统级动作库（is_system=1，用户不可删除） =====
-- category 严格对齐 schema.sql 注释：
--   1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身
-- equipment 与 muscle_group 语义化字符串匹配习惯
INSERT INTO exercise (id, name, category, difficulty, equipment, muscle_group, description, is_system, created_at, updated_at, deleted) VALUES
    (2089345678901234001, '杠铃卧推',      1, 2, '杠铃',   '胸大肌、肱三头肌',              '平躺在卧推凳上，双手握杠铃下放至胸部再推起，胸部训练的黄金动作',       1, NOW(), NOW(), 0),
    (2089345678901234002, '哑铃飞鸟',      1, 1, '哑铃',   '胸大肌',                        '平躺在卧推凳上，双手持哑铃外展再内收，深度拉伸胸肌纤维',                1, NOW(), NOW(), 0),
    (2089345678901234003, '杠铃深蹲',      5, 3, '杠铃',   '股四头肌、臀大肌',              '杠铃置于斜方肌，蹲至大腿平行地面再站起，下肢训练之王',                  1, NOW(), NOW(), 0),
    (2089345678901234004, '腿举',          5, 1, '器械',   '股四头肌',                      '坐姿在腿举机上，双脚蹬板屈膝再推起，对膝盖压力比深蹲小',                1, NOW(), NOW(), 0),
    (2089345678901234005, '引体向上',      2, 3, '自重',   '背阔肌、肱二头肌',              '双手握单杠悬垂，向上拉至下巴过杠，背部宽度训练首选',                  1, NOW(), NOW(), 0),
    (2089345678901234006, '坐姿划船',      2, 1, '器械',   '背阔肌、菱形肌',                '坐姿在划船机上，握把手拉至腹部，收缩背部感受肩胛骨挤压',               1, NOW(), NOW(), 0),
    (2089345678901234007, '杠铃推举',      3, 2, '杠铃',   '三角肌（前中束为主）',         '站姿杠铃置于肩部，向上推举至头顶锁肘，肩部综合力量训练',               1, NOW(), NOW(), 0),
    (2089345678901234008, '哑铃侧平举',    3, 1, '哑铃',   '三角肌中束',                    '双手持哑铃，双臂向两侧展开与地面平行，塑造肩部宽度',                  1, NOW(), NOW(), 0),
    (2089345678901234009, '杠铃弯举',      4, 1, '杠铃',   '肱二头肌',                      '站姿双手握杠铃，曲肘将杠铃举至胸前，二头肌厚度基础训练动作',           1, NOW(), NOW(), 0),
    (2089345678901234010, '平板支撑',      6, 1, '自重',   '腹横肌、核心稳定肌群',         '俯卧双肘撑地，身体呈一条直线保持静止，核心稳定训练的基础动作',         1, NOW(), NOW(), 0);
