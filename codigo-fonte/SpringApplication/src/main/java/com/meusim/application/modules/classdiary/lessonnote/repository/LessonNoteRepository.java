package com.meusim.application.modules.classdiary.lessonnote.repository;

import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonNoteRepository extends JpaRepository<LessonNote, UUID> {
    Optional<LessonNote> findByAttendanceId(UUID attendanceId);
}
