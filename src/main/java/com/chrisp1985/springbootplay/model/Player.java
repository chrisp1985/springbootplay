package com.chrisp1985.springbootplay.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Player(
        @NotEmpty(message = "Name is required") String name,
        @NotNull(message = "Age is required") Integer age,
        @NotNull(message = "Position is required") Position position,
        @NotNull(message = "Rating is required") Double rating
) {
}
