package ewm.user.service;

import ewm.error.exception.NotFoundException;
import ewm.user.dto.UserDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserServiceImplTest {
	@Autowired
	private UserService userService;

	private final UserDto userDto = UserDto.builder()
			.email("test@gmail.com")
			.name("test")
			.build();

	@Test
	void getUsers() {
		UserDto responseDto = userService.createUser(userDto);

		List<UserDto> users = userService.getUsers(List.of(responseDto.getId()), 0, 10);
		assertThat(users.getFirst(), equalTo(responseDto));
	}

	@Test
	void createUser() {
		UserDto responseDto = userService.createUser(userDto);
		Assertions.assertNotNull(responseDto);
		Assertions.assertEquals(userDto.getName(), responseDto.getName());
		Assertions.assertEquals(userDto.getEmail(), responseDto.getEmail());
		assertThat(responseDto.getId(), notNullValue());
	}

	@Test
	void deleteUser() {
		UserDto responseDto = userService.createUser(userDto);
		userService.deleteUser(responseDto.getId());
		assertThatThrownBy(() -> userService.deleteUser(responseDto.getId()))
				.isInstanceOf(NotFoundException.class);
	}
}