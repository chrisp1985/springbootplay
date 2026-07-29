package com.chrisp1985.springbootplay.model;

import jakarta.validation.constraints.NotEmpty;

public record PlayerDetailsRequest(
        @NotEmpty(message = "Name is required") String name
) {
}
