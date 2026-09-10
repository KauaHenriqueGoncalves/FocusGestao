package com.meusim.application.modules.classdiary.lessonnote.service;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import java.util.UUID;

public interface LessonNoteService {
    LessonNote findByAttendanceId(UUID attendanceId);
    LessonNote findByAttendanceIdWithCache(UUID attendanceId);
    LessonNote create(Attendance attendanceEntity, CreateLessonNoteRequestDTO dto);
}
