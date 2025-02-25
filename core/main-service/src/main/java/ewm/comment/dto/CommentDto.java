package ewm.comment.dto;

import ewm.user.model.User;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private User author;
}
