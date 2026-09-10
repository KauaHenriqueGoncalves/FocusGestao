package com.meusim.application.modules.classdiary.lessonnote;

import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import com.meusim.application.modules.classdiary.lessonnote.enums.NoteType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "lesson_note",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attendance_id_student_id_snap", columnNames = {"attendance_id", "student_id_snap"})
        }
)
public final class LessonNote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(name = "student_id_snap", nullable = false)
    private UUID studentId;

    @Column(name = "student_name_snap", nullable = false)
    private String studentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NoteType type;

    @Column(name = "subtype", nullable = false, length = 30)
    private String subType;

    @Column(name = "content", nullable = false, length = 500, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    public LessonNote() {
    }

    public LessonNote(UUID id,
                      Attendance attendance,
                      UUID studentId,
                      String studentName,
                      NoteType type,
                      String subType,
                      String content) {
        this.id = id;
        this.attendance = attendance;
        this.studentId = studentId;
        this.studentName = studentName;
        this.type = type;
        this.subType = subType;
        this.content = content;
    }

    public static LessonNote initFrom(Attendance entity, CreateLessonNoteRequestDTO dto) {
        return new LessonNote(
                null,
                entity,
                entity.getStudentId(),
                entity.getStudentName(),
                dto.type(),
                dto.subtype(),
                dto.content()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public NoteType getType() {
        return type;
    }

    public void setType(NoteType type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
        LessonNote that = (LessonNote) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "LessonNote{" +
                "id=" + id +
                ", attendance=" + attendance +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", type=" + type +
                ", subType='" + subType + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
