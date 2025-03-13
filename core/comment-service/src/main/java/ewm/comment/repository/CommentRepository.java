package ewm.comment.repository;

import ewm.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventIdAndAuthor(Long eventId, Long authorId);

    List<Comment> findAllByEventId(Long eventId);
}
