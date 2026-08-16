package com.fitpulse.app.admin.controller;

import com.fitpulse.app.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentWeight", 76.5);
        data.put("targetWeight", 70.0);
        data.put("startWeight", 80.0);
        data.put("lostKg", 3.5);
        data.put("remainingKg", 6.5);
        data.put("goalRate", 35);
        data.put("weeklyVolumeKg", 18650);
        data.put("weeklyVolumeDelta", 12.5);
        data.put("avgCalorieGap7d", -420);
        data.put("totalWorkoutsMonth", 16);
        data.put("streakDays", 8);
        data.put("projectedDate", LocalDate.now().plusDays(112).toString());
        return Result.success(data);
    }

    @GetMapping("/training-volume")
    public Result<Map<String, Object>> trainingVolume() {
        List<String> weeks = new ArrayList<>();
        List<BigDecimal> volumes = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        LocalDate base = LocalDate.now();
        Random r = new Random(42);
        for (int i = 11; i >= 0; i--) {
            LocalDate monday = base.minusWeeks(i);
            weeks.add(monday.minusDays(monday.getDayOfWeek().getValue() - 1) + "周");
            int vol = 8000 + r.nextInt(16000);
            volumes.add(BigDecimal.valueOf(vol));
            durations.add(120 + r.nextInt(240));
        }
        return Result.success(Map.of("weeks", weeks, "volumes", volumes, "durations", durations));
    }

    @GetMapping("/muscle-distribution")
    public Result<List<Map<String, Object>>> muscleDistribution() {
        String[] names = {"胸部", "背部", "腿部", "肩部", "手臂", "核心", "有氧"};
        int[] minutes = {180, 240, 300, 150, 160, 120, 260};
        String[] colors = {"#7c5cff", "#22d3ee", "#f59e0b", "#10b981", "#ef4444", "#8b5cf6", "#06b6d4"};
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", names[i]); m.put("value", minutes[i]); m.put("color", colors[i]);
            list.add(m);
        }
        return Result.success(list);
    }

    @GetMapping("/weight-progress")
    public Result<Map<String, Object>> weightProgress() {
        List<LocalDate> dates = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        LocalDate base = LocalDate.now();
        Random r = new Random(7);
        double w = 80.0;
        for (int i = 29; i >= 0; i--) {
            LocalDate d = base.minusDays(i);
            dates.add(d);
            w -= 0.05 + r.nextDouble() * 0.3;
            weights.add(BigDecimal.valueOf(Math.round(w * 10) / 10.0));
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("dates", dates);
        map.put("weights", weights);
        map.put("startWeight", 80.0);
        map.put("targetWeight", 70.0);
        map.put("currentWeight", weights.get(weights.size() - 1));
        map.put("goalRate", 35);
        return Result.success(map);
    }

    @GetMapping("/calorie-gap")
    public Result<Map<String, Object>> calorieGap() {
        List<String> dates = new ArrayList<>();
        List<Integer> actual = new ArrayList<>();
        List<Integer> target = new ArrayList<>();
        List<Integer> gap = new ArrayList<>();
        LocalDate base = LocalDate.now();
        Random r = new Random(11);
        for (int i = 13; i >= 0; i--) {
            LocalDate d = base.minusDays(i);
            dates.add(d.toString());
            int t = 1800;
            int a = 1300 + r.nextInt(900);
            target.add(t); actual.add(a); gap.add(a - t);
        }
        return Result.success(Map.of("dates", dates, "actual", actual, "target", target, "gap", gap));
    }
}
