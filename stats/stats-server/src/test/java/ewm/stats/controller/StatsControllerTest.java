package ewm.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ewm.dto.EndpointHitDTO;
import ewm.dto.EndpointHitResponseDto;
import ewm.dto.ViewStatsDTO;
import ewm.stats.service.HitService;
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

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    HitService hitService;

    EndpointHitDTO endpointHitDTO = EndpointHitDTO.builder()
            .app("test")
            .ip("test")
            .uri("test")
            .timestamp("2024-09-20 00:00:00")
            .build();

    EndpointHitResponseDto endpointHitResponseDto = EndpointHitResponseDto.builder()
            .id(1L)
            .app("test")
            .ip("test")
            .uri("test")
            .timestamp("2024-09-20 00:00:00")
            .build();

    ViewStatsDTO viewStatsDTO = ViewStatsDTO.builder()
            .app("test")
            .hits(5L)
            .uri("test")
            .build();

    @Test
    void createHit() throws Exception {
        Mockito.when(hitService.create(Mockito.any())).thenReturn(endpointHitResponseDto);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/hit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(endpointHitDTO))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(endpointHitResponseDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.app").value(endpointHitResponseDto.getApp()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.ip").value(endpointHitResponseDto.getIp()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.uri").value(endpointHitResponseDto.getUri()));
    }

    @Test
    void getHits() throws Exception {
        Mockito.when(hitService.getAll(Mockito.any())).thenReturn(List.of(viewStatsDTO));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stats")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].app").value(viewStatsDTO.getApp()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hits").value(viewStatsDTO.getHits()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uri").value(viewStatsDTO.getUri()));
    }
}
