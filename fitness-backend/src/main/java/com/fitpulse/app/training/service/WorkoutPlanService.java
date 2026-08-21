package com.fitpulse.app.training.service;

import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.training.dto.req.PlanCompleteReq;
import com.fitpulse.app.training.dto.req.PlanCreateReq;
import com.fitpulse.app.training.dto.req.PlanUpdateReq;
import com.fitpulse.app.training.dto.vo.*;

/**
 * 训练计划业务接口：CRUD + 状态流转。
 * <p>核心业务流程：DRAFT → start → IN_PROGRESS → complete（自动生成 record+sets）→ COMPLETED
 *                  ↘ cancel → CANCELLED → 可再次 start
 *
 * <p>方法清单（10 个）：
 * <ol>
 *   <li>page        - 计划列表</li>
 *   <li>detail      - 计划详情（含 exercises）</li>
 *   <li>create      - 新建计划</li>
 *   <li>update      - 修改计划（仅 DRAFT）</li>
 *   <li>delete      - 删除计划</li>
 *   <li>start       - 开始训练（DRAFT/CANCELLED → IN_PROGRESS）</li>
 *   <li>complete    - 完成训练（IN_PROGRESS → COMPLETED + 生成 record+sets）</li>
 *   <li>cancel     - 放弃训练（IN_PROGRESS → CANCELLED）</li>
 *   <li>copy       - 复制计划（深拷贝 + DRAFT）</li>
 *   <li>inProgress  - 当前进行中训练（恢复计时器）</li>
 * </ol>
 *
 * @author FitPulse
 */
public interface WorkoutPlanService {

    /**
     * 训练计划列表（分页）。
     */
    PageResult<PlanListVO> page(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 计划详情（含关联动作列表）。
     */
    PlanDetailVO detail(Long userId, Long id);

    /**
     * 新建计划（含批量动作关联，@Transactional）。
     */
    PlanDetailVO create(Long userId, PlanCreateReq req);

    /**
     * 修改计划（仅 DRAFT，exercises 全量替换，@Transactional）。
     */
    PlanDetailVO update(Long userId, Long id, PlanUpdateReq req);

    /**
     * 删除计划（级联删除 workout_plan_exercise）。
     */
    void delete(Long userId, Long id);

    /**
     * 开始训练（DRAFT/CANCELLED → IN_PROGRESS，写入 started_at）。
     */
    PlanStartResp start(Long userId, Long id);

    /**
     * 完成训练（IN_PROGRESS → COMPLETED，自动生成 record+sets，@Transactional 三表原子）。
     * <p>校验：durationSec ≥ 300（5 分钟），actualSets 非空。
     * <p>自动计算：totalVolume（热身组跳过）/ totalSets / totalReps。
     */
    PlanCompleteResp complete(Long userId, Long id, PlanCompleteReq req);

    /**
     * 放弃训练（IN_PROGRESS → CANCELLED，不生成 record）。
     */
    void cancel(Long userId, Long id);

    /**
     * 复制计划（深拷贝 plan + exercises，新 status=DRAFT，@Transactional）。
     */
    PlanCopyResp copy(Long userId, Long id);

    /**
     * 当前进行中训练（查询 status=IN_PROGRESS 的计划，返回 elapsedSec 供前端恢复计时器）。
     */
    InProgressVO inProgress(Long userId);
}
