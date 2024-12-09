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
public class UserFollowerEvent {
    private Long followerId;
    private Long followedUserId;
    private LocalDateTime subscriptionDate;
}