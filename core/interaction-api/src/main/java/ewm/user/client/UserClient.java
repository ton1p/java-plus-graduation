package ewm.user.client;

import ewm.user.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {
    @GetMapping("/{userId}")
    UserDto findById(@PathVariable Long userId);
}
