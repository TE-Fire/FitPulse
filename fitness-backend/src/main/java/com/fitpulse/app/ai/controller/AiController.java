package com.fitpulse.app.ai.controller;

import com.fitpulse.app.ai.service.AiService;
import com.fitpulse.app.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/generate-plan")
    public Result<String> generatePlan(@RequestBody Map<String, Object> body) {
        Integer goal = body.get("goal") == null ? 1 : Integer.valueOf(body.get("goal").toString());
        String equipment = body.get("equipment") == null ? null : body.get("equipment").toString();
        Integer days = body.get("daysPerWeek") == null ? 3 : Integer.valueOf(body.get("daysPerWeek").toString());
        String level = body.get("level") == null ? "入门" : body.get("level").toString();
        Integer mins = body.get("minutesPerDay") == null ? 45 : Integer.valueOf(body.get("minutesPerDay").toString());
        return Result.success(aiService.generatePlan(goal, equipment, days, level, mins));
    }

    @PostMapping("/diet-advice")
    public Result<String> dietAdvice(@RequestBody Map<String, Object> body) {
        String meals = body.get("todayMeals") == null ? "" : body.get("todayMeals").toString();
        Integer kcal = body.get("totalKcal") == null ? 0 : Integer.valueOf(body.get("totalKcal").toString());
        Integer goal = body.get("goal") == null ? 1 : Integer.valueOf(body.get("goal").toString());
        Integer target = body.get("targetKcal") == null ? 1800 : Integer.valueOf(body.get("targetKcal").toString());
        return Result.success(aiService.dietAdvice(meals, kcal, goal, target));
    }

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        return Result.success(aiService.chat(body.getOrDefault("message", "你好")));
    }
}
