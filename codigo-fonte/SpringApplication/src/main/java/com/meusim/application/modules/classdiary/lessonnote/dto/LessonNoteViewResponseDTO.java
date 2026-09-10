package com.meusim.application.modules.classdiary.lessonnote.dto;

import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.enums.NoteType;
import java.util.UUID;

public record LessonNoteViewResponseDTO(
    UUID id,
    NoteType type,
    String subType,
    String content
) {
    public static LessonNoteViewResponseDTO of(LessonNote ln) {
        if (ln == null) {
            return null;
        }
        return new LessonNoteViewResponseDTO(
                ln.getId(),
                ln.getType(),
                ln.getSubType(),
                ln.getContent()
        );
    }
}
