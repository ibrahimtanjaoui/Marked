package org.mehlib.marked.service;

import java.util.List;
import java.util.Optional;
import org.mehlib.marked.dao.entities.Section;
import org.mehlib.marked.dao.repositories.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SectionManager implements SectionService {

    private final SectionRepository sectionRepository;

    public SectionManager(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Section> getSection(Long id) {
        return sectionRepository.findById(id);
    }

    @Override
    public Section createSection(Section section) {
        return sectionRepository.save(section);
    }

    @Override
    public Section updateSection(Section section) {
        if (section.getId() == null) {
            throw new IllegalArgumentException(
                "Cannot update section without an ID"
            );
        }
        return sectionRepository.save(section);
    }

    @Override
    public void deleteSection(Section section) {
        sectionRepository.delete(section);
    }

    @Override
    public void deleteSectionById(Long id) {
        sectionRepository.deleteById(id);
    }
}
