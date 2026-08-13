package com.elsherif.livecaption.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeWindow {
    DAY("day"),
    WEEK("week");

    private final String value;
}
