package ewm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_action_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserActionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    private UserActionType actionType;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
