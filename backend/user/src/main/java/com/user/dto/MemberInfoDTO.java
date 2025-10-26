package com.user.dto;

import java.util.List;

public record MemberInfoDTO(
    List<Integer> pinnedTariffs
) {
}