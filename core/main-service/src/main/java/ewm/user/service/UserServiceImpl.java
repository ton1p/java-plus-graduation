package ewm.user.service;

import ewm.error.exception.ExistException;
import ewm.error.exception.NotFoundException;
import ewm.user.dto.UserDto;
import ewm.user.mapper.UserMapper;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	@Override
	public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
		int page = from / size;
		Pageable pageRequest = PageRequest.of(page, size);
		if (ids == null || ids.isEmpty()) return UserMapper.mapToUserDto(userRepository.findAll(pageRequest));
		else return UserMapper.mapToUserDto(userRepository.findAllById(ids));
	}

	@Transactional
	@Override
	public UserDto createUser(UserDto userDto) {
		if (userRepository.getByEmail(userDto.getEmail()).isPresent())
			throw new ExistException("Такой email уже есть");
		User user = UserMapper.mapToUser(userDto);
		log.info("Создан user --> {}", user);
		return UserMapper.mapToUserDto(userRepository.save(user));
	}

	@Transactional
	@Override
	public void deleteUser(Long userId) {
		getUserFromRepo(userId);
		userRepository.deleteById(userId);
		log.info("Удален user с id --> {}", userId);
	}

	private User getUserFromRepo(Long userId) {
		Optional<User> user = userRepository.findById(userId);
		if (user.isEmpty()) throw new NotFoundException("Пользователя с id = " + userId.toString() + " не существует");
		return user.get();
	}
}
