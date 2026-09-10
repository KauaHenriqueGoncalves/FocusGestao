package com.meusim.application.modules.classdiary.attendance.facade;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.attendance.dto.CreateAttendanceRequestDTO;
import com.meusim.application.modules.classdiary.attendance.service.AttendanceService;
import com.meusim.application.modules.classdiary.lesson.Lesson;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class AttendanceFacadeImpl implements AttendanceFacade {
    private final AttendanceService attendanceService;

    public AttendanceFacadeImpl(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Override
    public List<Attendance> getAllByLessonId(UUID lessonId) {
        return attendanceService.findAllByLessonIdWithCache(lessonId);
    }

    @Override
    public List<AttendanceViewResponseDTO> getAllViewByLessonId(UUID lessonId) {
        return attendanceService.findAttendanceViewByLessonIdWithCache(lessonId);
    }

    @Override
    public List<Attendance> createAll(Lesson lessonEntity, List<CreateAttendanceRequestDTO> list) {
        return attendanceService.createAll(lessonEntity, list);
    }
}
