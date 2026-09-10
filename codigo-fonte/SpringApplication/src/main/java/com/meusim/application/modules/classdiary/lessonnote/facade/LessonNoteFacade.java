package com.meusim.application.modules.classdiary.lessonnote.facade;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import java.util.UUID;

public interface LessonNoteFacade {
    LessonNote getByAttendanceId(UUID attendanceId);
    LessonNote create(Attendance entity, CreateLessonNoteRequestDTO dto);
}
