package com.meusim.application.modules.classdiary.lesson.event;

import java.util.UUID;

public record CreateLessonToEmailEvent(
        UUID lessonId
) { }
