package ewm.comment.service;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.CreateCommentDto;
import ewm.comment.mapper.CommentMapper;
import ewm.comment.model.Comment;
import ewm.comment.repository.CommentRepository;
import ewm.error.exception.ConflictException;
import ewm.error.exception.NotFoundException;
import ewm.event.client.EventClient;
import ewm.event.dto.EventDto;
import ewm.user.client.UserClient;
import ewm.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final String COMMENT_NOT_FOUND = "Comment not found";
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CreateCommentDto createCommentDto) {
        UserDto user = getUserById(userId);
        EventDto event = getEventById(eventId);

        Comment comment = Comment.builder()
                .author(user.getId())
                .eventId(event.getId())
                .content(createCommentDto.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentMapper.commentToCommentDto(saved, user);
    }

    @Override
    public CommentDto getComment(Long eventId, Long commentId) {
        getEventById(eventId);
        return CommentMapper.commentToCommentDto(getCommentById(commentId), getUserById(eventId));
    }

    @Override
    public List<CommentDto> getEventCommentsByUserId(Long userId, Long eventId) {
        getUserById(userId);
        getEventById(eventId);
        return commentRepository.findAllByEventIdAndAuthor(eventId, userId)
                .stream()
                .map((comment) -> CommentMapper.commentToCommentDto(comment, getUserById(comment.getAuthor())))
                .toList();
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId) {
        getEventById(eventId);
        return commentRepository.findAllByEventId(eventId)
                .stream()
                .map((comment) -> CommentMapper.commentToCommentDto(comment, getUserById(comment.getAuthor())))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CreateCommentDto createCommentDto) {
        getEventById(eventId);
        Comment comment = getCommentById(commentId);
        UserDto user = getUserById(userId);
        if (!Objects.equals(comment.getAuthor(), user.getId())) {
            throw new ConflictException("This user can't update comment");
        }
        comment.setContent(createCommentDto.getContent());
        return CommentMapper.commentToCommentDto(commentRepository.save(comment), user);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        getEventById(eventId);
        Comment comment = getCommentById(commentId);
        UserDto user = getUserById(userId);
        if (!Objects.equals(comment.getAuthor(), user.getId())) {
            throw new ConflictException("This user can't delete comment");
        }
        commentRepository.deleteById(commentId);
    }

    private UserDto getUserById(Long userId) {
        return userClient.findById(userId);
    }

    private EventDto getEventById(Long eventId) {
        return eventClient.getById(eventId);
    }

    private Comment getCommentById(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isEmpty()) {
            throw new NotFoundException(COMMENT_NOT_FOUND);
        }
        return optionalComment.get();
    }
}
