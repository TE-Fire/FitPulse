package com.fitpulse.app.health.controller;

import com.fitpulse.app.common.result.PageResult;
import com.fitpulse.app.common.result.Result;
import com.fitpulse.app.health.entity.BodyMetric;
import com.fitpulse.app.health.entity.Food;
import com.fitpulse.app.health.mapper.BodyMetricMapper;
import com.fitpulse.app.health.mapper.FoodMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {
    private final BodyMetricMapper bmMapper;
    private final FoodMapper foodMapper;

    @PostMapping("/body-metrics")
    public Result<Void> addMetric(@RequestBody BodyMetric m) { bmMapper.insert(m); return Result.success(); }

    @PutMapping("/body-metrics/{id}")
    public Result<Void> updateMetric(@PathVariable Long id, @RequestBody BodyMetric m) {
        m.setId(id); bmMapper.updateById(m); return Result.success();
    }

    @GetMapping("/body-metrics")
    public Result<PageResult<BodyMetric>> listMetric(@RequestParam(defaultValue = "1") Long pageNum,
                                                      @RequestParam(defaultValue = "30") Long pageSize) {
        Page<BodyMetric> p = bmMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<BodyMetric>().orderByDesc(BodyMetric::getRecordDate));
        return Result.success(PageResult.of(p));
    }

    @GetMapping("/foods")
    public Result<PageResult<Food>> listFood(@RequestParam(defaultValue = "1") Long pageNum,
                                              @RequestParam(defaultValue = "20") Long pageSize,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String category,
                                              @RequestParam(required = false) String barcode) {
        Page<Food> p = foodMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Food>()
                        .and(keyword != null, w -> w.like(Food::getName, keyword).or().like(Food::getAlias, keyword))
                        .eq(category != null, Food::getCategory, category)
                        .eq(barcode != null, Food::getBarcode, barcode)
                        .orderByAsc(Food::getName));
        return Result.success(PageResult.of(p));
    }

    @PostMapping("/foods")
    public Result<Void> addFood(@RequestBody Food f) { foodMapper.insert(f); return Result.success(); }

    @PutMapping("/foods/{id}")
    public Result<Void> updateFood(@PathVariable Long id, @RequestBody Food f) {
        f.setId(id); foodMapper.updateById(f); return Result.success();
    }

    @DeleteMapping("/foods/{id}")
    public Result<Void> deleteFood(@PathVariable Long id) { foodMapper.deleteById(id); return Result.success(); }
}
