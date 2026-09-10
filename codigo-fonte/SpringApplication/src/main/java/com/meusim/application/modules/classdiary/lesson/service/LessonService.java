package com.meusim.application.modules.classdiary.lesson.service;

import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.lesson.Lesson;
import com.meusim.application.modules.classdiary.lesson.dto.AgendaDayResponseDTO;
import com.meusim.application.modules.classdiary.lesson.dto.CreateLessonRequestDTO;
import com.meusim.application.modules.classdiary.lesson.dto.GetToCreateLessonRequestDTO;
import com.meusim.application.modules.classdiary.lesson.dto.LabelToCreateLessonResponseDTO;
import com.meusim.application.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.UUID;

public interface LessonService {
    Page<Lesson> pageByClassroomId(UUID classroomId, int page, int size);
    PageResponse<Lesson> pageByClassroomIdWithCache(UUID classroomId, int page, int size);
    List<AgendaDayResponseDTO> findAgendaByMonth(UUID classroomId, int year, int month);
    List<AgendaDayResponseDTO> findAgendaByMonthWithCache(UUID classroomId, int year, int month);
    LabelToCreateLessonResponseDTO getLabelToCreateLesson(UUID classroomId, GetToCreateLessonRequestDTO dto);
    LabelToCreateLessonResponseDTO getLabelToCreateLessonWithCache(UUID classroomId, GetToCreateLessonRequestDTO dto);
    Lesson findById(UUID id);
    Lesson findByIdWithCache(UUID id);
    List<AttendanceViewResponseDTO> findAllAttendancesViewsByLessonId(UUID lessonId);
    Lesson create(CreateLessonRequestDTO dto);

}
