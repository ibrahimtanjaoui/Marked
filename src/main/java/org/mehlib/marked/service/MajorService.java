package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Major;

import java.util.List;
import java.util.Optional;

public interface MajorService {
    List<Major> getAllMajors();
    Optional<Major> getMajor(Long id);
    Major createMajor(Major major);
    Major updateMajor(Major major);
    void deleteMajor(Major major);
    void deleteMajor(Long id);
}
