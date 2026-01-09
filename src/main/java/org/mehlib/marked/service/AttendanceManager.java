package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Attendance;
import org.mehlib.marked.dao.repositories.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceManager implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceManager(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Attendance> getAttendance(Long id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public Attendance createAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance updateAttendance(Attendance attendance) {
        if (attendance.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update attendance without an ID"
            );
        }
        return attendanceRepository.save(attendance);
    }

    @Override
    public void deleteAttendanceById(Long id) {
        attendanceRepository.deleteById(id);
    }

    @Override
    public void deleteAttendance(Attendance attendance) {
        attendanceRepository.delete(attendance);
    }
}
