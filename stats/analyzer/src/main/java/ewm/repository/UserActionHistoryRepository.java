package ewm.repository;

import ewm.model.UserActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActionHistoryRepository extends JpaRepository<UserActionHistory, Long> {
    List<UserActionHistory> findByUserId(Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
