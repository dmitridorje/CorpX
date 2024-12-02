package school.faang.user_service.model.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectFollowerEvent {
    private Long followerId;
    private Long projectId;
    private Long creatorId;
    private LocalDateTime subscriptionDate;
}