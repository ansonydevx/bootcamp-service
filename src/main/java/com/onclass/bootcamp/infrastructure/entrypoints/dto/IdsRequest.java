package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.util.List;

public record IdsRequest(
        List<Long> ids
) {}
