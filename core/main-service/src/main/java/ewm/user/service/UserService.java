package ewm.user.service;

import ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {
	List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

	UserDto createUser(UserDto userDto);

	void deleteUser(Long userId);
}
