package com.example.backend.dto.request;

import com.example.backend.utils.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TeamCreateRequest {
    @NotBlank(message = "Team name is required")
    private String name;

    private String description;

    @NotNull(message = "Team level is required")
    private Enums.TeamLevel level;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Enums.TeamLevel getLevel() {
        return level;
    }

    public void setLevel(Enums.TeamLevel level) {
        this.level = level;
    }
}
