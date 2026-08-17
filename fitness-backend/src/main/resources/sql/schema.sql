-- ============================================================
-- FitPulse 数据库初始化 DDL (v1.0 冻结版)
-- 数据库: fitpulse_db   字符集: utf8mb4
-- 基准: docs/设计契约.md 第 2.2 节
-- ============================================================

-- ========== 用户域 ==========
CREATE TABLE IF NOT EXISTS user (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    username        VARCHAR(64)  NOT NULL UNIQUE COMMENT '用户名',
    password_hash   VARCHAR(128) NOT NULL COMMENT 'BCrypt哈希',
    email           VARCHAR(128) NULL COMMENT '邮箱',
    phone           VARCHAR(20)  NULL COMMENT '手机号',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=启用 0=禁用',
    last_login_at   DATETIME     NULL COMMENT '最后登录时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

CREATE TABLE IF NOT EXISTS user_profile (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID=user.id',
    user_id         BIGINT       NOT NULL UNIQUE,
    nickname        VARCHAR(64)  NULL COMMENT '昵称',
    avatar_url      VARCHAR(512) NULL COMMENT '头像文件URL',
    gender          TINYINT      NULL COMMENT '0=未知 1=男 2=女',
    birthday        DATE         NULL,
    height_cm       DECIMAL(5,1) NULL COMMENT '身高cm',
    bio             VARCHAR(512) NULL COMMENT '简介',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资料表';

CREATE TABLE IF NOT EXISTS user_goal (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    goal_type       TINYINT      NOT NULL COMMENT '1=减脂 2=增肌 3=塑形 4=维持健康 5=力量举',
    target_weight   DECIMAL(5,1) NULL COMMENT '目标体重kg',
    target_body_fat DECIMAL(4,1) NULL COMMENT '目标体脂%',
    weekly_workouts TINYINT      NOT NULL DEFAULT 3 COMMENT '每周训练次目标',
    daily_calories  INT          NULL COMMENT '每日摄入目标kcal',
    daily_water_ml  INT          NOT NULL DEFAULT 2000 COMMENT '每日饮水目标ml',
    start_date      DATE         NOT NULL,
    target_date     DATE         NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户目标表';

-- ========== 训练域 ==========
CREATE TABLE IF NOT EXISTS exercise (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    name            VARCHAR(128) NOT NULL COMMENT '动作名称',
    category        TINYINT      NOT NULL COMMENT '1=胸 2=背 3=肩 4=手臂 5=腿 6=核心 7=有氧 8=全身',
    difficulty      TINYINT      NOT NULL DEFAULT 2 COMMENT '1=入门 2=中级 3=高级',
    equipment       VARCHAR(64)  NULL COMMENT '器械',
    description     TEXT         NULL COMMENT '动作说明',
    image_url       VARCHAR(512) NULL COMMENT '示范图',
    is_system       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=系统预置 0=自定义',
    user_id         BIGINT       NULL COMMENT '自定义创建者',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作库';

CREATE TABLE IF NOT EXISTS workout_plan (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(128) NOT NULL COMMENT '计划名 如: 推/拉/腿 Day1',
    plan_type       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=力量 2=有氧 3=混合',
    description     VARCHAR(512) NULL,
    estimated_min   INT          NULL COMMENT '预估时长分钟',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划(模板)';

CREATE TABLE IF NOT EXISTS workout_plan_exercise (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    plan_id         BIGINT       NOT NULL,
    exercise_id     BIGINT       NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0 COMMENT '动作顺序',
    target_sets     TINYINT      NOT NULL DEFAULT 3,
    target_reps     VARCHAR(32)  NULL COMMENT '如 8-12 / 力竭',
    rest_sec        INT          NULL COMMENT '组间休息秒',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划动作关联表';

CREATE TABLE IF NOT EXISTS workout_record (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    plan_id         BIGINT       NULL COMMENT '来源计划模板',
    record_date     DATE         NOT NULL COMMENT '训练日期',
    duration_sec    INT          NULL COMMENT '实际时长秒',
    total_volume    DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '训练总容量=sum(重量x次数) B维度重点',
    total_sets      INT          NOT NULL DEFAULT 0 COMMENT '总组数 B维度重点',
    total_reps      INT          NOT NULL DEFAULT 0 COMMENT '总次数 B维度重点',
    note            VARCHAR(1024) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练记录表(单次训练头)';

CREATE TABLE IF NOT EXISTS workout_set (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    record_id       BIGINT       NOT NULL COMMENT '关联workout_record.id',
    exercise_id     BIGINT       NOT NULL,
    set_no          TINYINT      NOT NULL COMMENT '第N组',
    weight_kg       DECIMAL(8,2) NULL COMMENT '负重量kg',
    reps            INT          NULL COMMENT '完成次数',
    is_completed    TINYINT      NOT NULL DEFAULT 1,
    is_warmup       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否热身组',
    rpe             TINYINT      NULL COMMENT 'RPE 1-10',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练组明细';

-- ========== 健康域 ==========
CREATE TABLE IF NOT EXISTS body_metric (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    record_date     DATE         NOT NULL,
    weight_kg       DECIMAL(5,1) NULL COMMENT '体重kg A维度重点',
    body_fat_pct    DECIMAL(4,1) NULL COMMENT '体脂率% A维度重点',
    muscle_kg       DECIMAL(5,1) NULL COMMENT '肌肉量kg',
    bmi             DECIMAL(4,1) NULL COMMENT 'BMI',
    waist_cm        DECIMAL(5,1) NULL COMMENT '腰围',
    note            VARCHAR(256) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身体指标记录';

CREATE TABLE IF NOT EXISTS food (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    name            VARCHAR(128) NOT NULL COMMENT '食物名',
    category        VARCHAR(32)  NULL COMMENT '主食/肉蛋奶/蔬菜/水果/零食/饮品',
    kcal_per_100g   DECIMAL(8,2) NOT NULL COMMENT '每100g热量',
    protein_g       DECIMAL(6,2) NULL,
    carb_g          DECIMAL(6,2) NULL,
    fat_g           DECIMAL(6,2) NULL,
    fiber_g         DECIMAL(6,2) NULL,
    is_system       TINYINT      NOT NULL DEFAULT 1,
    user_id         BIGINT       NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物库';

CREATE TABLE IF NOT EXISTS meal_record (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    record_date     DATE         NOT NULL,
    meal_type       TINYINT      NOT NULL COMMENT '1=早餐 2=午餐 3=晚餐 4=加餐',
    food_id         BIGINT       NULL COMMENT '关联food.id 空=自定义',
    food_name       VARCHAR(128) NOT NULL,
    quantity_g      DECIMAL(8,2) NOT NULL COMMENT '摄入量g',
    total_kcal      DECIMAL(8,2) NOT NULL COMMENT '本次热量 B维度',
    protein_g       DECIMAL(6,2) NULL,
    carb_g          DECIMAL(6,2) NULL,
    fat_g           DECIMAL(6,2) NULL,
    photo_url       VARCHAR(512) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

CREATE TABLE IF NOT EXISTS water_log (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    record_date     DATE         NOT NULL,
    amount_ml       INT          NOT NULL COMMENT '本次饮水量ml B维度重点',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水记录表';

-- ========== 公共域 ==========
CREATE TABLE IF NOT EXISTS file_resource (
    id              BIGINT PRIMARY KEY COMMENT '雪花ID',
    user_id         BIGINT       NOT NULL,
    bucket          VARCHAR(64)  NOT NULL COMMENT 'MinIO bucket 或 本地子目录',
    object_key      VARCHAR(512) NOT NULL COMMENT '对象key / 相对路径',
    original_name   VARCHAR(256) NOT NULL,
    file_size       BIGINT       NOT NULL COMMENT '字节',
    content_type    VARCHAR(128) NULL,
    file_url        VARCHAR(512) NOT NULL COMMENT '可访问URL',
    storage_type    TINYINT      NOT NULL DEFAULT 1 COMMENT '1=MinIO 2=本地文件',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_bucket (user_id, bucket)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';
