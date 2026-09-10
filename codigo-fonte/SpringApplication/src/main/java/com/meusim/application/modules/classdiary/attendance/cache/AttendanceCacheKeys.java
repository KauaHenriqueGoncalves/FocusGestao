package com.meusim.application.modules.classdiary.attendance.cache;

import java.time.Duration;
import java.util.UUID;

public final class AttendanceCacheKeys {
    private static final String PREFIX = "attendance::";
    public static final Duration TTL = Duration.ofHours(72);

    private AttendanceCacheKeys() {
    }

    public static String byLessonId(UUID lessonId) {
        return PREFIX + lessonId + "::byLessonId";
    }

    public static String dtoByLessonId(UUID lessonId) {
        return PREFIX + lessonId + "::dtoByLessonId";
    }
}
