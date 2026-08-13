package com.elsherif.livecaption.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {

    @NotNull
    @Min(0)
    private Integer progressSeconds;

    @Min(1)
    private Integer durationSeconds;
}
