package ewm.event.dto;

import ewm.request.dto.RequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class EventRequestStatusUpdateResult {
	private List<RequestDto> confirmedRequests;
	private List<RequestDto> rejectedRequests;
}
