package com.meusim.application.modules.classdiary.attendance.service;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.attendance.dto.CreateAttendanceRequestDTO;
import com.meusim.application.modules.classdiary.lesson.Lesson;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {
    List<Attendance> findAllByLessonId(UUID lessonId);
    List<Attendance> findAllByLessonIdWithCache(UUID lessonId);
    List<AttendanceViewResponseDTO> findAttendanceViewByLessonId(UUID lessonId);
    List<AttendanceViewResponseDTO> findAttendanceViewByLessonIdWithCache(UUID lessonId);
    List<Attendance> createAll(Lesson lessonEntity, List<CreateAttendanceRequestDTO> list);
}
