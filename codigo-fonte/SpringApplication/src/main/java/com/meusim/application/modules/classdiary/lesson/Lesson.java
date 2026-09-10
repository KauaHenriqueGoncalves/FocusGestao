package com.meusim.application.modules.classdiary.lesson;

import com.meusim.application.modules.academic.classroom.Classroom;
import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lesson.enums.LessonStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "lesson",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_class_schedule_lesson_date", columnNames = {"class_schedule_id_snap", "lesson_date"})
        },
        indexes = {
                @Index(name = "idx_lesson_classroom_date", columnList = "classroom_id, lesson_date")
        }
)
public final class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate lessonDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LessonStatus status;

    @Column(name = "responsible_id_snap", nullable = false)
    private UUID responsibleId; // <- collaboratorId || schoolAdminId

    @Column(name = "responsible_username_snap", nullable = false, length = 100)
    private String responsibleUsername;

    @Column(name = "responsible_role_snap", nullable = false, length = 30)
    private String responsibleRole; // <- SCOPE_collaborator || SCOPE_school_admin

    @Column(name = "classroom_name_snap", nullable = false, length = 60)
    private String classroomName;

    @Column(name = "subject_name_snap", nullable = false, length = 50)
    private String subjectName;

    @Column(name = "class_schedule_id_snap", nullable = false)
    private UUID scheduleId;

    @Column(name = "weekday_snap")
    private int weekday;

    @Column(name = "start_time_snap")
    private LocalTime startTime;

    @Column(name = "end_time_snap")
    private LocalTime endTime;

    @Column(name = "content", nullable = false, length = 500, columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Attendance> attendances = new ArrayList<>();

    public Lesson() {
    }

    public Lesson(UUID id,
                  Classroom classroom,
                  LocalDate lessonDate,
                  LessonStatus status,
                  UUID responsibleId,
                  String responsibleUsername,
                  String responsibleRole,
                  String classroomName,
                  String subjectName,
                  UUID scheduleId,
                  int weekday,
                  LocalTime startTime,
                  LocalTime endTime,
                  String description) {
        this.id = id;
        this.classroom = classroom;
        this.lessonDate = lessonDate;
        this.status = status;
        this.responsibleId = responsibleId;
        this.responsibleUsername = responsibleUsername;
        this.responsibleRole = responsibleRole;
        this.classroomName = classroomName;
        this.subjectName = subjectName;
        this.scheduleId = scheduleId;
        this.weekday = weekday;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getLessonDate() {
        return lessonDate;
    }

    public void setLessonDate(LocalDate lessonDate) {
        this.lessonDate = lessonDate;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public LessonStatus getStatus() {
        return status;
    }

    public void setStatus(LessonStatus status) {
        this.status = status;
    }

    public UUID getResponsibleId() {
        return responsibleId;
    }

    public void setResponsibleId(UUID responsibleId) {
        this.responsibleId = responsibleId;
    }

    public String getResponsibleUsername() {
        return responsibleUsername;
    }

    public void setResponsibleUsername(String responsibleUsername) {
        this.responsibleUsername = responsibleUsername;
    }

    public String getResponsibleRole() {
        return responsibleRole;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String content) {
        this.description = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Lesson lesson = (Lesson) o;
        return Objects.equals(id, lesson.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", classroom=" + classroom +
                ", lessonDate=" + lessonDate +
                ", status=" + status +
                ", responsibleId=" + responsibleId +
                ", responsibleUsername='" + responsibleUsername + '\'' +
                ", responsibleRole='" + responsibleRole + '\'' +
                ", classroomName='" + classroomName + '\'' +
                ", subjectName='" + subjectName + '\'' +
                ", scheduleId=" + scheduleId +
                ", weekday=" + weekday +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
