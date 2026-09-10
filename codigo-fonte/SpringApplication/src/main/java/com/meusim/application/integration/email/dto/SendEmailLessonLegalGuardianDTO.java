package com.meusim.application.integration.email.dto;

import com.meusim.application.modules.classdiary.lesson.enums.LessonStatus;
import java.time.LocalDate;
import java.time.LocalTime;

public record SendEmailLessonLegalGuardianDTO(
        String legalGuardianName,
        String legalGuardianEmail,
        LocalDate lessonDate,
        LessonStatus status, // -se for cancelar não tem attendance
        String responsibleUsername,
        String responsibleRole,
        String classroomName,
        String subjectName,
        int weekday,
        LocalTime startTime,
        LocalTime endTime,
        String description,
        Attendance attendance,
        Note note
) {
    public record Attendance(
            String studentName,
            String status,
            String content
    ) { }

    public record Note(
            String type,
            String subType,
            String content
    ) { }
}
