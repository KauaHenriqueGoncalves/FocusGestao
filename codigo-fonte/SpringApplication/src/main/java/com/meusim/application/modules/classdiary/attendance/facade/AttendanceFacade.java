package com.meusim.application.modules.classdiary.attendance.facade;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.attendance.dto.CreateAttendanceRequestDTO;
import com.meusim.application.modules.classdiary.lesson.Lesson;
import java.util.List;
import java.util.UUID;

public interface AttendanceFacade {
    List<Attendance> getAllByLessonId(UUID lessonId);
    List<AttendanceViewResponseDTO> getAllViewByLessonId(UUID lessonId);
    List<Attendance> createAll(Lesson lessonEntity, List<CreateAttendanceRequestDTO> list);
}
