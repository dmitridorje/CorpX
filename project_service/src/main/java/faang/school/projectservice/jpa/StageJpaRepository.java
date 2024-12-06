package faang.school.projectservice.jpa;

import faang.school.projectservice.model.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StageJpaRepository extends JpaRepository<Stage, Long> {
}
