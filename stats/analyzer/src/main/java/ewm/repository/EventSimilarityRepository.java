package ewm.repository;

import ewm.model.EventSimilarity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    List<EventSimilarity> findByEventAOrEventB(Long eventA, Long eventB);
}
