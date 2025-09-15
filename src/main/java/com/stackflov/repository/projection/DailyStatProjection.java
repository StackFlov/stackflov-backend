package com.stackflov.repository.projection;

import java.time.LocalDate;

public interface DailyStatProjection {
    LocalDate getDate();
    Long getCount();
}