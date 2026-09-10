package com.meusim.application.modules.classdiary.attendance.enums;

public enum AttendanceStatus {
    PRESENT("presente"),
    ABSENT("ausente"),
    JUSTIFIED("justificado");

    private final String name;

    AttendanceStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
