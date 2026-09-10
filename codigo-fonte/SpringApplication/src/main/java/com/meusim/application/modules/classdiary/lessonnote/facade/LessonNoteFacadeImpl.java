package com.meusim.application.modules.classdiary.lessonnote.facade;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import com.meusim.application.modules.classdiary.lessonnote.service.LessonNoteService;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LessonNoteFacadeImpl implements LessonNoteFacade {
    private final LessonNoteService lessonNoteService;

    public LessonNoteFacadeImpl(LessonNoteService lessonNoteService) {
        this.lessonNoteService = lessonNoteService;
    }

    @Override
    public LessonNote getByAttendanceId(UUID attendanceId) {
        return lessonNoteService.findByAttendanceIdWithCache(attendanceId);
    }

    @Override
    public LessonNote create(Attendance entity, CreateLessonNoteRequestDTO dto) {
        return lessonNoteService.create(entity, dto);
    }
}
