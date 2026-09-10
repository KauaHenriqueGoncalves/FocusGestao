package com.meusim.application.modules.classdiary.lessonnote.dto;

import com.meusim.application.modules.classdiary.lessonnote.enums.NoteType;
import com.meusim.application.shared.validation.NoEmoji;
import com.meusim.application.shared.validation.NoLeadingTrailingSpace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLessonNoteRequestDTO(
        @NotNull(message = "Informe o tipo")
        NoteType type,

        @NotBlank(message = "O SubTipo não pode ser vazio")
        @Size(max = 30, message = "Subtipo até 30 caracteres")
        @NoLeadingTrailingSpace
        @NoEmoji(message = "Não é permitido o recebimento de emoji")
        String subtype,

        @NotBlank(message = "O conteúdo não pode ser vazio")
        @Size(max = 500, message = "Conteudo da presença até 200 caracteres")
        @NoLeadingTrailingSpace
        @NoEmoji(message = "Não é permitido o recebimento de emoji")
        String content
) { }
