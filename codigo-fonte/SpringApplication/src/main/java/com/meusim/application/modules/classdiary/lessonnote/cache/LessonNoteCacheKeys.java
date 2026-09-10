package com.meusim.application.modules.classdiary.lessonnote.cache;

import java.time.Duration;
import java.util.UUID;

public final class LessonNoteCacheKeys {
    private static final String PREFIX = "lesson_note::";
    public static final Duration TTL = Duration.ofHours(72);

    private LessonNoteCacheKeys() {
    }

    public static String byAttendance(UUID attendanceId) {
        return PREFIX + attendanceId + "::byLessonAntAttendance";
    }
}
