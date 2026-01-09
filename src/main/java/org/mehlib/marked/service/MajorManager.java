package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Major;
import org.mehlib.marked.dao.repositories.MajorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MajorManager implements MajorService {

    private final MajorRepository majorRepository;

    public MajorManager(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Major> getMajor(Long id) {
        return majorRepository.findById(id);
    }

    @Override
    public Major createMajor(Major major) {
        return majorRepository.save(major);
    }

    @Override
    public Major updateMajor(Major major) {
        if (major.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update major without an ID"
            );
        }
        return majorRepository.save(major);
    }

    @Override
    public void deleteMajor(Major major) {
        majorRepository.delete(major);
    }

    @Override
    public void deleteMajor(Long id) {
        majorRepository.deleteById(id);
    }
}
