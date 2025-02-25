package ewm.event.model;

import ewm.category.model.Category;
import ewm.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "events")
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(columnDefinition = "VARCHAR(4000)")
	private String annotation;
	@Column(name = "create_on")
	private LocalDateTime createdOn;
	@Column(columnDefinition = "VARCHAR(7000)")
	private String description;
	private LocalDateTime eventDate;
	private Boolean paid;
	private Integer participantLimit;
	private LocalDateTime publishedOn;
	private Boolean requestModeration;
	private EventState state;
	private String title;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User initiator;

	private Double lat;
	private Double lon;
	private Long views;

	private Integer confirmedRequests = 0;
}
