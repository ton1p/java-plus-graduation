package ewm.event.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@Builder
@Validated
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventDto {

    @NotBlank(message = "Краткое описание события не может быть пустым")
    @Size(min = 20, max = 2000, message = "Краткое описание не может быть меньше 20 или больше 2000 символов")
    private String annotation;

    @NotNull(message = "Категория должна быть указана")
    private Long category;

    @NotBlank(message = "Полное описание события не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание не можеть быть меньше 20 или больше 7000 символов")
    private String description;

    @NotNull(message = "Время события должно быть указано")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Место проведения события должно быть указано")
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Заголовок события не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок не может быть меньше 3 или больше 120 символов")
    private String title;
}
