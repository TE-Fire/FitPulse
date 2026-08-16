package com.fitpulse.app.ai.service;

import com.fitpulse.app.common.config.AiPromptProperties;
import com.fitpulse.app.common.exception.BusinessException;
import com.fitpulse.app.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatClient.Builder chatClientBuilder;
    private final AiPromptProperties prompts;

    private ChatClient client() { return chatClientBuilder.build(); }

    public String generatePlan(Integer goal, String equipment, Integer daysPerWeek, String level, Integer minutesPerDay) {
        String prompt = prompts.getPlanPromptTemplate()
                .replace("{goal}", String.valueOf(goal))
                .replace("{equipment}", equipment == null ? "徒手" : equipment)
                .replace("{daysPerWeek}", String.valueOf(daysPerWeek == null ? 3 : daysPerWeek))
                .replace("{level}", level == null ? "入门" : level)
                .replace("{minutesPerDay}", String.valueOf(minutesPerDay == null ? 45 : minutesPerDay));
        return callAi(prompt);
    }

    public String dietAdvice(String todayMeals, Integer totalKcal, Integer goal, Integer targetKcal) {
        String prompt = prompts.getDietPromptTemplate()
                .replace("{todayMeals}", todayMeals == null ? "无记录" : todayMeals)
                .replace("{totalKcal}", String.valueOf(totalKcal == null ? 0 : totalKcal))
                .replace("{goal}", String.valueOf(goal == null ? 1 : goal))
                .replace("{targetKcal}", String.valueOf(targetKcal == null ? 1800 : targetKcal));
        return callAi(prompt);
    }

    public String chat(String userMessage) { return callAi(userMessage); }

    private String callAi(String prompt) {
        try {
            return client().prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException(ResultCode.AI_CALL_FAIL, "AI 调用失败：" + e.getMessage());
        }
    }
}
