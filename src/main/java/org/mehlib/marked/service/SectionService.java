package org.mehlib.marked.service;

import org.mehlib.marked.dao.entities.Section;

import java.util.List;
import java.util.Optional;

public interface SectionService {
    List<Section> getAllSections();
    Optional<Section> getSection(Long id);
    Section createSection(Section section);
    Section updateSection(Section section);
    void deleteSection(Section section);
    void deleteSectionById(Long id);
}
