-- ============================================================
-- FitPulse 数据库迁移 V3: workout_plan 字段扩展 + training 预置数据
-- 日期: 2026-08-21
-- 说明: 1) 在 workout_plan 表追加 started_at / completed_at / actual_duration_sec 字段，
--          用于追踪计划执行的生命周期（开始/完成/时长）
--       2) workout_plan.status 枚举扩展为 4 态: 0=DRAFT 1=IN_PROGRESS 2=COMPLETED 3=CANCELLED
--          （原 schema 只有 0/1，向后兼容已有数据）
--       3) 预置 10 个系统级动作（exercise.is_system=1），开箱即用
-- ============================================================

USE fitpulse_db;

-- ===== 1. workout_plan 表追加字段 =====
ALTER TABLE workout_plan
    ADD COLUMN started_at          DATETIME   NULL COMMENT '开始训练时间戳，start 接口写入' AFTER status,
    ADD COLUMN completed_at        DATETIME   NULL COMMENT '完成训练时间戳，complete 接口写入' AFTER started_at,
    ADD COLUMN actual_duration_sec INT        NULL COMMENT '实际训练时长（秒），前端提交' AFTER completed_at;

-- ===== 2. 预置系统级动作库（is_system=1，用户不可删除） =====
-- 注意：此处 id 使用雪花算法样例值，实际由 MyBatis-Plus 生成时替换
INSERT INTO exercise (id, name, category, difficulty, muscle_group, equipment, description, is_system, created_at, updated_at, deleted) VALUES
    (2089345678901234001, '杠铃卧推',      'chest',    2, '胸大肌、三头肌',   'barbell',    '平躺在卧推凳上，双手握杠铃下放至胸部再推起，胸部训练的黄金动作', 1, NOW(), NOW(), 0),
    (2089345678901234002, '哑铃飞鸟',      'chest',    1, '胸大肌',           'dumbbell',   '平躺在卧推凳上，双手持哑铃外展再内收，拉伸胸肌纤维',              1, NOW(), NOW(), 0),
    (2089345678901234003, '杠铃深蹲',      'leg',      3, '股四头肌、臀大肌', 'barbell',    '杠铃置于斜方肌，蹲至大腿平行地面再站起，下肢训练之王',            1, NOW(), NOW(), 0),
    (2089345678901234004, '腿举',          'leg',      1, '股四头肌',         'machine',    '坐姿在腿举机上，双脚蹬板屈膝再推起，对膝盖压力较小',              1, NOW(), NOW(), 0),
    (2089345678901234005, '引体向上',      'back',     3, '背阔肌、肱二头肌', 'bodyweight', '双手握单杠悬垂，向上拉至下巴过杠，背部训练之王',                1, NOW(), NOW(), 0),
    (2089345678901234006, '坐姿划船',      'back',     1, '背阔肌、菱形肌',   'machine',    '坐姿在划船机上，握把手拉至腹部，收缩背部',                      1, NOW(), NOW(), 0),
    (2089345678901234007, '杠铃推举',      'shoulder', 2, '三角肌',           'barbell',    '站姿杠铃置于肩部，向上推举至头顶，肩部综合训练动作',            1, NOW(), NOW(), 0),
    (2089345678901234008, '哑铃侧平举',    'shoulder', 1, '三角肌中束',       'dumbbell',   '双手持哑铃，双臂向两侧展开与地面平行，塑造肩部宽度',            1, NOW(), NOW(), 0),
    (2089345678901234009, '杠铃弯举',      'arm',      1, '肱二头肌',         'barbell',    '站姿双手握杠铃，曲肘将杠铃举至胸前，二头基础训练',              1, NOW(), NOW(), 0),
    (2089345678901234010, '平板支撑',      'core',     1, '腹横肌、核心肌群', 'bodyweight', '俯卧双肘撑地，身体呈一条直线保持静止，核心稳定训练基础动作',    1, NOW(), NOW(), 0);
