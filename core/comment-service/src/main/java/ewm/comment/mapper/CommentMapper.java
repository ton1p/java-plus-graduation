package ewm.comment.mapper;

import ewm.comment.dto.CommentDto;
import ewm.comment.model.Comment;
import ewm.user.dto.UserDto;

public class CommentMapper {
    public static CommentDto commentToCommentDto(Comment comment, UserDto userDto) {
        return new CommentDto(comment.getId(), comment.getContent(), userDto);
    }
}
