package ewm.request.repository;

import ewm.request.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByRequesterAndEvent(Long userId, Long eventId);

    List<Request> findAllByRequester(Long userId);

    List<Request> findAllByEvent(Long eventId);
}
