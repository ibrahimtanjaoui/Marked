package org.mehlib.marked.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.mehlib.marked.dao.entities.DayType;

public record CalendarRequest(
    @NotNull(message = "Day of week is required")
    DayOfWeek dayOfWeek,

    @NotNull(message = "Date is required")
    LocalDateTime date,

    @Size(max = 50, message = "Holiday name cannot exceed 50 characters")
    String holidayName,

    @NotNull(message = "Day type is required")
    DayType dayType
) {}
