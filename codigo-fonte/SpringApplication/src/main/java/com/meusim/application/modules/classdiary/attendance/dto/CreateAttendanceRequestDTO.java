package com.meusim.application.modules.classdiary.attendance.dto;

import com.meusim.application.modules.academic.student.Student;
import com.meusim.application.modules.classdiary.attendance.enums.AttendanceStatus;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import com.meusim.application.shared.validation.NoEmoji;
import com.meusim.application.shared.validation.NoLeadingTrailingSpace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateAttendanceRequestDTO(
        @NotNull(message = "Informe o estudante")
        UUID studentId,

        @NotBlank(message = "Informe o nome do estudante")
        @Size(max = 250, message = "O nome do estudante até 250 caracteres")
        @NoEmoji(message = "Não é permitido o recebimento de emoji")
        String studentName,

        @NotNull(message = "Informe o status")
        AttendanceStatus status,

        @NotNull(message = "A conteúdo não pode ser nulo")
        @Size(max = 500, message = "Conteudo da presença até 200 caracteres")
        @NoLeadingTrailingSpace
        @NoEmoji(message = "Não é permitido o recebimento de emoji")
        String content,

        @Valid
        CreateLessonNoteRequestDTO lessonNote
) {
    public static List<CreateAttendanceRequestDTO> labelToCreateLesson(List<Student> list) {
        return list.stream()
                .map((Student s) -> {
                    return new CreateAttendanceRequestDTO(
                            s.getId(),
                            s.getName(),
                            AttendanceStatus.PRESENT,
                            "Estudante presente! Nenhuma observação!",
                            null
                    );
                })
                .toList();
    }
}
