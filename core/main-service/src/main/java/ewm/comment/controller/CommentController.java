package ewm.comment.controller;

import ewm.comment.dto.CommentDto;
import ewm.comment.dto.CreateCommentDto;
import ewm.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private static final String PRIVATE_PATH = "/users/{userId}/events/{eventId}/comments";
    private static final String PUBLIC_PATH = "/events/{eventId}/comments";

    @GetMapping(PUBLIC_PATH)
    public List<CommentDto> getEventComments(@PathVariable Long eventId) {
        return commentService.getEventComments(eventId);
    }

    @GetMapping(PRIVATE_PATH)
    public List<CommentDto> getEventCommentsByUserId(@PathVariable Long userId, @PathVariable Long eventId) {
        return commentService.getEventCommentsByUserId(userId, eventId);
    }

    @PostMapping(PRIVATE_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody @Valid CreateCommentDto createCommentDto
    ) {
        return commentService.addComment(userId, eventId, createCommentDto);
    }

    @GetMapping(PUBLIC_PATH + "/{commentId}")
    public CommentDto getComment(
            @PathVariable Long eventId,
            @PathVariable Long commentId
    ) {
        return commentService.getComment(eventId, commentId);
    }


    @PatchMapping(PRIVATE_PATH + "/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId,
            @RequestBody @Valid CreateCommentDto createCommentDto
    ) {
        return commentService.updateComment(userId, eventId, commentId, createCommentDto);
    }

    @DeleteMapping(PRIVATE_PATH + "/{commentId}")
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userId, eventId, commentId);
    }
}
