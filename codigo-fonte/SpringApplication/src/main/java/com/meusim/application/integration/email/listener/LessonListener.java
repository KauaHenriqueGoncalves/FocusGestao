package com.meusim.application.integration.email.listener;

import com.meusim.application.auth.service.AuthenticatedUserService;
import com.meusim.application.integration.email.dto.SendEmailLessonAdminDTO;
import com.meusim.application.integration.email.dto.SendEmailLessonLegalGuardianDTO;
import com.meusim.application.integration.email.service.EmailSendService;
import com.meusim.application.modules.academic.classroom.dto.ClassroomDetailResponse;
import com.meusim.application.modules.academic.classroom.service.ClassroomService;
import com.meusim.application.modules.academic.student.dto.StudentDetailResponse;
import com.meusim.application.modules.academic.student.service.StudentService;
import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.lesson.dto.LessonDetailViewResponseDTO;
import com.meusim.application.modules.classdiary.lesson.enums.LessonStatus;
import com.meusim.application.modules.classdiary.lesson.event.CreateLessonToEmailEvent;
import com.meusim.application.modules.classdiary.lesson.facade.LessonFacade;
import com.meusim.application.modules.identity.profile.schooladmin.SchoolAdmin;
import com.meusim.application.modules.school.facade.SchoolFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.util.ArrayList;
import java.util.List;

@Component
public class LessonListener {
    private static final Logger log = LoggerFactory.getLogger(LessonListener.class);

    private final SchoolFacade schoolFacade;
    private final LessonFacade lessonFacade;
    private final StudentService studentService;
    private final ClassroomService classroomService;
    private final AuthenticatedUserService authenticatedUserService;
    private final EmailSendService emailSendService;

    public LessonListener(
            SchoolFacade schoolFacade,
            LessonFacade lessonFacade,
            StudentService studentService,
            ClassroomService classroomService,
            AuthenticatedUserService authenticatedUserService,
            EmailSendService emailSendService
    ) {
        this.schoolFacade = schoolFacade;
        this.lessonFacade = lessonFacade;
        this.studentService = studentService;
        this.classroomService = classroomService;
        this.authenticatedUserService = authenticatedUserService;
        this.emailSendService = emailSendService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlerLessonSentEmail(CreateLessonToEmailEvent event) {
        LessonDetailViewResponseDTO lessonDetail = lessonFacade.getById(event.lessonId());
        List<SendEmailLessonLegalGuardianDTO> legalGuardianEmails =
                lessonDetail.status() == LessonStatus.CANCELED
                        ? buildLegalGuardianEmailsForCanceled(lessonDetail)
                        : buildLegalGuardianEmailsForAttendance(lessonDetail);
        if (legalGuardianEmails.isEmpty()) {
            log.warn("Nenhum responsável encontrado para envio de email. [lessonId={}]", event.lessonId());
        } else {
            emailSendService.sendLessonEmailsToLegalGuardians(legalGuardianEmails);
        }
        List<SchoolAdmin> admins = schoolFacade.getAllSchoolAdminFromSchool();
        if (admins.isEmpty()) {
            log.warn("Nenhum admin encontrado para envio de email da agenda. [lessonId={}]", event.lessonId());
        } else {
            List<String> adminEmails = admins.stream().map(a -> a.getUser().getEmail()).toList();
            SendEmailLessonAdminDTO adminDto = buildAdminEmailDto(lessonDetail);
            emailSendService.sendLessonEmailToAdmins(adminEmails, adminDto);
        }
        //emailSendService.sendLessonEmailToResponsible(lessonDetail);
        log.info("Emails da agenda processados. [lessonId={}] [status={}] [totalResponsaveis={}] [totalAdmins={}]",
                event.lessonId(), lessonDetail.status(), legalGuardianEmails.size(), admins.size());
    }

    private SendEmailLessonAdminDTO buildAdminEmailDto(LessonDetailViewResponseDTO lessonDetail) {
        List<SendEmailLessonAdminDTO.AttendanceItem> items = lessonDetail.attendances().stream()
                .map(attendance -> {
                    SendEmailLessonLegalGuardianDTO.Attendance attendanceDto =
                            new SendEmailLessonLegalGuardianDTO.Attendance(
                                    attendance.studentName(),
                                    attendance.status(),
                                    attendance.content()
                            );
                    SendEmailLessonLegalGuardianDTO.Note noteDto = attendance.lessonNote() == null
                            ? null
                            : new SendEmailLessonLegalGuardianDTO.Note(
                            attendance.lessonNote().type().getName(),
                            attendance.lessonNote().subType(),
                            attendance.lessonNote().content()
                    );
                    return new SendEmailLessonAdminDTO.AttendanceItem(attendanceDto, noteDto);
                })
                .toList();
        return new SendEmailLessonAdminDTO(
                lessonDetail.lessonDate(),
                lessonDetail.status(),
                lessonDetail.responsibleUsername(),
                lessonDetail.classroomName(),
                lessonDetail.subjectName(),
                lessonDetail.startTime(),
                lessonDetail.endTime(),
                lessonDetail.description(),
                items
        );
    }

    private List<SendEmailLessonLegalGuardianDTO> buildLegalGuardianEmailsForCanceled(LessonDetailViewResponseDTO lessonDetail) {
        ClassroomDetailResponse classroom = classroomService.findDetailResponseById(authenticatedUserService.getOwnerId(), lessonDetail.classroomId());
        return classroom.students().stream()
                .map(studentSummary -> {
                    StudentDetailResponse student = studentService.findResponseDetailById(studentSummary.id());
                    return baseDto(lessonDetail, student, null, null);
                })
                .toList();
    }

    private List<SendEmailLessonLegalGuardianDTO> buildLegalGuardianEmailsForAttendance(LessonDetailViewResponseDTO lessonDetail) {
        List<SendEmailLessonLegalGuardianDTO> result = new ArrayList<>();
        for (AttendanceViewResponseDTO attendance : lessonDetail.attendances()) {
            StudentDetailResponse student = studentService.findResponseDetailById(attendance.studentId());
            SendEmailLessonLegalGuardianDTO.Attendance attendanceDto =
                    new SendEmailLessonLegalGuardianDTO.Attendance(
                            attendance.studentName(),
                            attendance.status(),
                            attendance.content()
                    );
            SendEmailLessonLegalGuardianDTO.Note noteDto = attendance.lessonNote() == null
                    ? null
                    : new SendEmailLessonLegalGuardianDTO.Note(
                    attendance.lessonNote().type().getName(),
                    attendance.lessonNote().subType(),
                    attendance.lessonNote().content()
            );
            result.add(baseDto(lessonDetail, student, attendanceDto, noteDto));
        }
        return result;
    }

    private SendEmailLessonLegalGuardianDTO baseDto(
            LessonDetailViewResponseDTO lessonDetail,
            StudentDetailResponse student,
            SendEmailLessonLegalGuardianDTO.Attendance attendance,
            SendEmailLessonLegalGuardianDTO.Note note
    ) {
        return new SendEmailLessonLegalGuardianDTO(
                student.legalGuardianResponse().username(),
                student.legalGuardianResponse().email(),
                lessonDetail.lessonDate(),
                lessonDetail.status(),
                lessonDetail.responsibleUsername(),
                lessonDetail.responsibleRole(),
                lessonDetail.classroomName(),
                lessonDetail.subjectName(),
                lessonDetail.weekday(),
                lessonDetail.startTime(),
                lessonDetail.endTime(),
                lessonDetail.description(),
                attendance,
                note
        );
    }
}
