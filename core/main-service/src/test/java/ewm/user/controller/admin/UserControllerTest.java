package ewm.user.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ewm.user.dto.UserDto;
import ewm.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	UserService userService;

	UserDto userDto = UserDto.builder()
			.id(1L)
			.email("test@gmail.com")
			.name("test")
			.build();

	@Test
	void getUsers() throws Exception {
		Mockito.when(userService.getUsers(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(List.of(userDto));

		mockMvc.perform(
						MockMvcRequestBuilders.get("/admin/users?ids=" + userDto.getId())
								.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.[0].id").value(userDto.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.[0].name").value(userDto.getName()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.[0].email").value(userDto.getEmail()));
	}

	@Test
	void createUser() throws Exception {
		Mockito.when(userService.createUser(Mockito.any())).thenReturn(userDto);

		mockMvc.perform(
						MockMvcRequestBuilders.post("/admin/users")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(userDto))
				)
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userDto.getId()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.name").value(userDto.getName()))
				.andExpect(MockMvcResultMatchers.jsonPath("$.email").value(userDto.getEmail()));
	}

	@Test
	void deleteUser() throws Exception {
		userService.deleteUser(userDto.getId());

		mockMvc.perform(
						MockMvcRequestBuilders.delete("/admin/users/" + userDto.getId())
				)
				.andExpect(MockMvcResultMatchers.status().isNoContent());
	}
}