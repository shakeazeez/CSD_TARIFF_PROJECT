package com.user.dto;

import java.util.List;

public record UserInfoDTO(
    List<Integer> pinnedTariffs
) {
}