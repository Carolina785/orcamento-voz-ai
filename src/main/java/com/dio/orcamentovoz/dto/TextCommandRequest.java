package com.dio.orcamentovoz.dto;

import jakarta.validation.constraints.NotBlank;

public record TextCommandRequest(@NotBlank String comando) {
}
