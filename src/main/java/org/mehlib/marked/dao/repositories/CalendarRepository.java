package org.mehlib.marked.dao.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.mehlib.marked.dao.entities.Calendar;
import org.mehlib.marked.dao.entities.DayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    List<Calendar> findByDayType(DayType dayType);

    List<Calendar> findByDayTypeOrderByDateAsc(DayType dayType);

    @Query(
        "SELECT c FROM Calendar c WHERE c.date BETWEEN :from AND :to ORDER BY c.date ASC"
    )
    List<Calendar> findByDateRange(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
        "SELECT c FROM Calendar c WHERE c.date BETWEEN :from AND :to AND c.dayType = :dayType ORDER BY c.date ASC"
    )
    List<Calendar> findByDateRangeAndDayType(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("dayType") DayType dayType
    );

    List<Calendar> findByHolidayNameNotNullOrderByDateAsc();

    @Query(
        "SELECT c FROM Calendar c WHERE c.date BETWEEN :from AND :to AND c.holidayName IS NOT NULL ORDER BY c.date ASC"
    )
    List<Calendar> findHolidaysInDateRange(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    boolean existsByDate(LocalDateTime date);

    List<Calendar> findByDateOrderByDateAsc(LocalDateTime date);
}
