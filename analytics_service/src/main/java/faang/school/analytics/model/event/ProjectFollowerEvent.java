package faang.school.analytics.model.event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFollowerEvent {
    private Long followerId;
    private Long projectId;
    private Long creatorId;
    private LocalDateTime subscriptionDate;
}