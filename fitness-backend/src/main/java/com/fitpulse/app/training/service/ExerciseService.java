package com.fitpulse.app.training.service;

import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.training.dto.req.ExerciseCreateReq;
import com.fitpulse.app.training.dto.req.ExerciseUpdateReq;
import com.fitpulse.app.training.dto.vo.ExerciseVO;

/**
 * 动作库业务接口：分页查询 / 详情 / 新增 / 修改 / 删除。
 * <p>Controller 面向此接口注入，Spring 自动装配 impl 包下的实现类。
 *
 * @author FitPulse
 */
public interface ExerciseService {

    /**
     * 动作库分页查询（系统动作 + 当前用户自定义动作）。
     *
     * @param userId     当前登录用户 ID
     * @param pageNum    页码（从 1 开始）
     * @param pageSize   每页条数
     * @param name       动作名称模糊搜索（可空）
     * @param category   动作分类 1-8（可空）
     * @param difficulty 难度 1-3（可空）
     * @return 分页结果
     */
    PageResult<ExerciseVO> page(Long userId, Integer pageNum, Integer pageSize,
                                String name, Integer category, Integer difficulty);

    /**
     * 动作详情。
     *
     * @param userId 当前登录用户 ID
     * @param id     动作 ID
     * @return 动作 VO
     */
    ExerciseVO detail(Long userId, Long id);

    /**
     * 新增自定义动作（is_system=0, user_id=当前用户）。
     *
     * @param userId 当前登录用户 ID
     * @param req    新增请求
     * @return 新增后的动作 VO
     */
    ExerciseVO create(Long userId, ExerciseCreateReq req);

    /**
     * 修改动作（仅自己的自定义动作，部分更新语义）。
     *
     * @param userId 当前登录用户 ID
     * @param id     动作 ID
     * @param req    修改请求
     * @return 修改后的动作 VO
     */
    ExerciseVO update(Long userId, Long id, ExerciseUpdateReq req);

    /**
     * 删除动作（系统预置拒绝，非本人拒绝，被引用拒绝）。
     *
     * @param userId 当前登录用户 ID
     * @param id     动作 ID
     */
    void delete(Long userId, Long id);
}
