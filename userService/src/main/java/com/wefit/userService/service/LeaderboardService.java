package com.wefit.userService.service;

import com.wefit.userService.dto.UserResponseDto;
import com.wefit.userService.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.wefit.userService.dto.CursorPageResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CursorPageResponse<UserResponseDto> getGlobalLeaderboard(int limit) {
        List<UserResponseDto> data = userRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "xp")))
                .stream()
                .map(UserResponseDto::toDto)
                .collect(Collectors.toList());
        return new CursorPageResponse<>(data, null, false);
    }
}
