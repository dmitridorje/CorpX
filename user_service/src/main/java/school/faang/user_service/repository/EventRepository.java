package school.faang.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import school.faang.user_service.model.entity.Event;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM event e
            WHERE e.user_id = :userId
            """)
    List<Event> findAllByUserId(long userId);

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM event e
            JOIN user_event ue ON ue.event_id = e.id
            WHERE ue.user_id = :userId
            """)
    List<Event> findParticipatedEventsByUserId(long userId);
}