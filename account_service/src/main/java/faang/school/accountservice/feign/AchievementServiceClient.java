package faang.school.accountservice.feign;

import faang.school.accountservice.model.dto.AchievementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "achievement-service", url = "${achievement-service.host}:${achievement-service.port}")
public interface AchievementServiceClient {

    @GetMapping("/achievements/{achievementId}")
    AchievementDto getAchievement(@PathVariable long achievementId);
}
