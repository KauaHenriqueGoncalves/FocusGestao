package com.meusim.application.modules.classdiary.attendance.dto;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.dto.LessonNoteViewResponseDTO;
import java.util.List;
import java.util.UUID;

public record AttendanceViewResponseDTO(
        UUID id,
        UUID studentId,
        String studentName,
        String status,
        String content,
        LessonNoteViewResponseDTO lessonNote
) {
    public static AttendanceViewResponseDTO of(Attendance a) {
        return new AttendanceViewResponseDTO(
                a.getId(),
                a.getStudentId(),
                a.getStudentName(),
                a.getStatus().getName(),
                a.getContent(),
                null
        );
    }

    public static AttendanceViewResponseDTO of(Attendance a, LessonNote ln) {
        return new AttendanceViewResponseDTO(
                a.getId(),
                a.getStudentId(),
                a.getStudentName(),
                a.getStatus().getName(),
                a.getContent(),
                LessonNoteViewResponseDTO.of(ln)
        );
    }

    public static List<AttendanceViewResponseDTO> of(List<Attendance> list) {
        return list.stream().map(AttendanceViewResponseDTO::of).toList();
    }
}
