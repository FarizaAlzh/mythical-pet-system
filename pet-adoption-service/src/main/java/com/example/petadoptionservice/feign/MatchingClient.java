package com.example.petadoptionservice.feign;
// что этот обьект должен сделать
import com.example.petadoptionservice.dto.matching.MatchRequestDto;
import com.example.petadoptionservice.dto.matching.MatchResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

//отправляем hhtp запрос на 2 сервис
@FeignClient(name = "matching-service", url = "${matching.service.url}") // вставит адрес 2 сервиса 8081
public interface MatchingClient {

    @PostMapping("/api/match-score") // отправляем пост запрос на /match-score
    MatchResponseDto calculateMatchScore(@RequestHeader("X-Internal-Token") String internalToken,
                                         @RequestBody MatchRequestDto requestDto); // обьект джава нужен чтобы ушел в запрос как json
}
