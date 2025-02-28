package ewm.user.mapper;

import ewm.user.dto.UserDto;
import ewm.user.model.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
	public static User mapToUser(UserDto userDto) {
		return User.builder()
				.id(userDto.getId())
				.name(userDto.getName())
				.email(userDto.getEmail())
				.build();
	}

	public static UserDto mapToUserDto(User user) {
		return new UserDto(
				user.getId(),
				user.getName(),
				user.getEmail()
		);
	}

	public static List<UserDto> mapToUserDto(Iterable<User> users) {
		List<UserDto> dtos = new ArrayList<>();
		for (User user : users) {
			dtos.add(mapToUserDto(user));
		}
		return dtos;
	}
}
