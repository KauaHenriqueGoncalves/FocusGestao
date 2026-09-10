package com.meusim.application.integration.email.dto;

import com.meusim.application.modules.classdiary.lesson.enums.LessonStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SendEmailLessonAdminDTO(
        LocalDate lessonDate,
        LessonStatus status,
        String responsibleUsername,
        String classroomName,
        String subjectName,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        List<AttendanceItem> attendances
) {
    public record AttendanceItem(
            SendEmailLessonLegalGuardianDTO.Attendance attendance,
            SendEmailLessonLegalGuardianDTO.Note note
    ) { }
}
