package ewm.user.client;

import ewm.user.dto.UserDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/admin/users")
public interface UserClient {
    @Retryable(retryFor = {FeignException.class}, maxAttempts = 5, backoff = @Backoff(delay = 2000))
    @GetMapping("/{userId}")
    UserDto findById(@PathVariable Long userId);
}
