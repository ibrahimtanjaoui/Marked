package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Attendance;

import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    List<Attendance> getAllAttendances();
    Optional<Attendance> getAttendance(Long id);
    Attendance createAttendance(Attendance attendance);
    Attendance updateAttendance(Attendance attendance);
    void deleteAttendanceById(Long id);
    void deleteAttendance(Attendance attendance);
}
