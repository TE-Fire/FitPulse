-- ============================================================
-- FitPulse 数据库初始化 DDL (v1.0 冻结版)
-- 数据库: fitpulse_db   字符集: utf8mb4
-- ============================================================

DROP TABLE IF EXISTS water_log;
CREATE TABLE water_log (
  id            BIGINT       NOT NULL,
  user_id       BIGINT       NOT NULL,
  ecord_date   DATE         NOT NULL,
  mount_ml     INT          NOT NULL,
  drink_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user_date (user_id,ecord_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水记录';

DROP TABLE IF EXISTS meal_record;
CREATE TABLE meal_record (
  id            BIGINT       NOT NULL,
  user_id       BIGINT       NOT NULL,
  ecord_date   DATE         NOT NULL,
  meal_type     TINYINT      NOT NULL,
  ood_id       BIGINT       DEFAULT NULL,
  ood_name     VARCHAR(128) NOT NULL,
  serving_g     DECIMAL(8,2) NOT NULL,
  	otal_kcal    DECIMAL(8,2) NOT NULL,
  protein_g     DECIMAL(6,2) DEFAULT 0,
  carb_g        DECIMAL(6,2) DEFAULT 0,
  at_g         DECIMAL(6,2) DEFAULT 0,
  photo_url     VARCHAR(512) DEFAULT NULL,
  
ote          VARCHAR(255) DEFAULT NULL,
  eat_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user_date_meal (user_id,ecord_date,meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录';

DROP TABLE IF EXISTS ood;
CREATE TABLE ood (
  id            BIGINT        NOT NULL,
  
ame          VARCHAR(128)  NOT NULL,
  lias         VARCHAR(255)  DEFAULT NULL,
  arcode       VARCHAR(32)   DEFAULT NULL,
  category      VARCHAR(32)   DEFAULT '其他',
  kcal_per_100g DECIMAL(8,2)  NOT NULL,
  protein_g     DECIMAL(6,2)  NOT NULL DEFAULT 0,
  carb_g        DECIMAL(6,2)  NOT NULL DEFAULT 0,
  at_g         DECIMAL(6,2)  NOT NULL DEFAULT 0,
  iber_g       DECIMAL(6,2)  DEFAULT 0,
  serving_unit  VARCHAR(16)   DEFAULT '100g',
  serving_g     DECIMAL(8,2)  DEFAULT 100,
  cover_url     VARCHAR(512)  DEFAULT NULL,
  is_custom     TINYINT(1)    NOT NULL DEFAULT 0,
  user_id       BIGINT        DEFAULT NULL,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_barcode (arcode),
  KEY idx_name (
ame)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物库';

DROP TABLE IF EXISTS ody_metric;
CREATE TABLE ody_metric (
  id             BIGINT       NOT NULL,
  user_id        BIGINT       NOT NULL,
  ecord_date    DATE         NOT NULL,
  weight_kg      DECIMAL(5,2) DEFAULT NULL,
  ody_fat_pct   DECIMAL(4,2) DEFAULT NULL,
  muscle_pct     DECIMAL(4,2) DEFAULT NULL,
  chest_cm       DECIMAL(5,2) DEFAULT NULL,
  waist_cm       DECIMAL(5,2) DEFAULT NULL,
  hip_cm         DECIMAL(5,2) DEFAULT NULL,
  rm_cm         DECIMAL(5,2) DEFAULT NULL,
  	high_cm       DECIMAL(5,2) DEFAULT NULL,
  calf_cm        DECIMAL(5,2) DEFAULT NULL,
  
eck_cm        DECIMAL(5,2) DEFAULT NULL,
  mi            DECIMAL(4,2) DEFAULT NULL,
  
ote           VARCHAR(500) DEFAULT NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted        TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_date (user_id,ecord_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身体指标';

DROP TABLE IF EXISTS workout_set;
CREATE TABLE workout_set (
  id            BIGINT       NOT NULL,
  ecord_id     BIGINT       NOT NULL,
  exercise_id   BIGINT       NOT NULL,
  exercise_name VARCHAR(128) DEFAULT NULL,
  set_no        TINYINT      NOT NULL,
  weight_kg     DECIMAL(8,2) DEFAULT 0,
  eps          INT          DEFAULT 0,
  is_done       TINYINT(1)   NOT NULL DEFAULT 1,
  is_pr         TINYINT(1)   NOT NULL DEFAULT 0,
  duration_sec  INT          DEFAULT NULL,
  
ote          VARCHAR(255) DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_record_exercise (ecord_id,exercise_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练每组明细';

DROP TABLE IF EXISTS workout_record;
CREATE TABLE workout_record (
  id              BIGINT        NOT NULL,
  user_id         BIGINT        NOT NULL,
  plan_id         BIGINT        DEFAULT NULL,
  plan_name       VARCHAR(128)  DEFAULT NULL,
  start_time      DATETIME      NOT NULL,
  end_time        DATETIME      DEFAULT NULL,
  duration_min    INT           DEFAULT NULL,
  	otal_volume_kg DECIMAL(12,2) DEFAULT 0,
  	otal_sets      INT           DEFAULT 0,
  	otal_reps      INT           DEFAULT 0,
  calorie_burned  INT           DEFAULT NULL,
  
ote            VARCHAR(500)  DEFAULT NULL,
  mood            TINYINT       DEFAULT 3,
  created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted         TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user_date (user_id,created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练打卡记录';

DROP TABLE IF EXISTS workout_plan_exercise;
CREATE TABLE workout_plan_exercise (
  id           BIGINT       NOT NULL,
  plan_id      BIGINT       NOT NULL,
  exercise_id  BIGINT       NOT NULL,
  day_no       TINYINT      NOT NULL DEFAULT 1,
  order_no     TINYINT      NOT NULL DEFAULT 1,
  	arget_sets  TINYINT      NOT NULL DEFAULT 3,
  	arget_reps  VARCHAR(32)  DEFAULT '8-12',
  est_sec     INT          NOT NULL DEFAULT 90,
  
ote         VARCHAR(255) DEFAULT NULL,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted      TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_plan_day (plan_id,day_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划动作明细';

DROP TABLE IF EXISTS workout_plan;
CREATE TABLE workout_plan (
  id            BIGINT        NOT NULL,
  
ame          VARCHAR(128)  NOT NULL,
  description   VARCHAR(500)  DEFAULT NULL,
  plan_type     TINYINT       NOT NULL DEFAULT 1,
  days_per_week TINYINT       NOT NULL DEFAULT 3,
  duration_days INT           NOT NULL DEFAULT 28,
  is_template   TINYINT(1)    NOT NULL DEFAULT 1,
  user_id       BIGINT        DEFAULT NULL,
  difficulty    TINYINT       NOT NULL DEFAULT 1,
  cover_url     VARCHAR(512)  DEFAULT NULL,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_plan_type (plan_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划模板';

DROP TABLE IF EXISTS exercise;
CREATE TABLE exercise (
  id          BIGINT        NOT NULL,
  
ame        VARCHAR(128)  NOT NULL,
  lias       VARCHAR(255)  DEFAULT NULL,
  category    TINYINT       NOT NULL DEFAULT 1,
  muscle      VARCHAR(64)   DEFAULT NULL,
  difficulty  TINYINT       NOT NULL DEFAULT 1,
  equipment   TINYINT       NOT NULL DEFAULT 1,
  cover_url   VARCHAR(512)  DEFAULT NULL,
  ideo_url   VARCHAR(512)  DEFAULT NULL,
  steps       TEXT          DEFAULT NULL,
  	ips        TEXT          DEFAULT NULL,
  reathing   VARCHAR(255)  DEFAULT NULL,
  sort        INT           NOT NULL DEFAULT 0,
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted     TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_category (category),
  KEY idx_name (
ame)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动作库';

DROP TABLE IF EXISTS user_goal;
CREATE TABLE user_goal (
  id               BIGINT       NOT NULL,
  user_id          BIGINT       NOT NULL,
  goal_type        TINYINT      NOT NULL DEFAULT 1,
  start_weight_kg  DECIMAL(5,2) DEFAULT NULL,
  	arget_weight_kg DECIMAL(5,2) DEFAULT NULL,
  deadline         DATE         DEFAULT NULL,
  daily_calorie    INT          DEFAULT NULL,
  protein_g_per_kg DECIMAL(3,1) NOT NULL DEFAULT 1.6,
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted          TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身目标';

DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
  id              BIGINT       NOT NULL,
  user_id         BIGINT       NOT NULL,
  gender          TINYINT      NOT NULL DEFAULT 0,
  irthday        DATE         DEFAULT NULL,
  height_cm       DECIMAL(5,2) DEFAULT NULL,
  ctivity_level  TINYINT      NOT NULL DEFAULT 2,
  unit_system     TINYINT      NOT NULL DEFAULT 1,
  dark_mode       TINYINT      NOT NULL DEFAULT 0,
  emind_train    VARCHAR(8)   DEFAULT '20:00',
  emind_water    TINYINT(1)   NOT NULL DEFAULT 1,
  water_goal_ml   INT          NOT NULL DEFAULT 2000,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted         TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户档案';

DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id            BIGINT       NOT NULL,
  username      VARCHAR(64)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  
ickname      VARCHAR(64)  DEFAULT NULL,
  vatar_url    VARCHAR(512) DEFAULT NULL,
  email         VARCHAR(128) DEFAULT NULL,
  phone         VARCHAR(20)  DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 1,
  last_login_at DATETIME     DEFAULT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

DROP TABLE IF EXISTS ile_resource;
CREATE TABLE ile_resource (
  id            BIGINT        NOT NULL,
  user_id       BIGINT        NOT NULL,
  object_name   VARCHAR(255)  NOT NULL,
  original_name VARCHAR(255)  NOT NULL,
  ucket        VARCHAR(64)   NOT NULL DEFAULT 'fitpulse-assets',
  content_type  VARCHAR(128)  DEFAULT NULL,
  size_bytes    BIGINT        NOT NULL DEFAULT 0,
  ile_md5      VARCHAR(64)   DEFAULT NULL,
  storage_type  TINYINT       NOT NULL DEFAULT 1,
  url           VARCHAR(512)  DEFAULT NULL,
  iz_type      VARCHAR(32)   DEFAULT NULL,
  iz_id        BIGINT        DEFAULT NULL,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT(1)    NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_object (object_name),
  KEY idx_biz (iz_type,iz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源记录';
