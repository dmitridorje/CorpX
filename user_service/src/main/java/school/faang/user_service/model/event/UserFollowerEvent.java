package school.faang.user_service.model.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserFollowerEvent {
    private Long followerId;
    private Long followedUserId;
    private LocalDateTime subscriptionDate;
}