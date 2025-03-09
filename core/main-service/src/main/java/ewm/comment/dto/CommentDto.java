package ewm.comment.dto;

import ewm.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private UserDto author;
}
