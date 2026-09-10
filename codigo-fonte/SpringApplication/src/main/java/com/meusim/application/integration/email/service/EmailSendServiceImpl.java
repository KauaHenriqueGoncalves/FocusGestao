package com.meusim.application.integration.email.service;

import com.meusim.application.integration.email.dto.SendEmailLessonLegalGuardianDTO;
import com.meusim.application.integration.email.dto.SendEmailSubscriptionPaid;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmailSendServiceImpl implements EmailSendService {
    private static final Logger log = LoggerFactory.getLogger(EmailSendServiceImpl.class);
    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    public EmailSendServiceImpl(
            JavaMailSender mailSender,
            EmailTemplateService templateService
    ) {
        this.mailSender = mailSender;
        this.templateService = templateService;
    }

    @Override
    @Async("emailExecutor")
    public void sendConfirmAccountEmail(String to, String name, String schoolCode, String link) {
        log.info("Enviando e-mail de confirmacao de conta. [destinatario={}] [nome={}]", to, name);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            Map<String, Object> variables = Map.of(
                    "name", name,
                    "email", to,
                    "schoolCode", schoolCode,
                    "confirmationLink", link
            );
            String html = templateService.process("email/confirm-account", variables);
            helper.setTo(to);
            helper.setSubject("Confirme sua conta - Meu S.I.M");
            helper.setText(html, true);
            mailSender.send(message);

            log.info("E-mail de confirmacao de conta enviado com sucesso. [destinatario={}]", to);
        }
        catch (Exception e) {
            log.error("Falha ao enviar e-mail de confirmacao de conta. [destinatario={}] [motivo={}]",
                    to, e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendSubscriptionPaidEmails(List<String> emails, UUID schoolId, SendEmailSubscriptionPaid info) {
        for (String to : emails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                Map<String, Object> variables = Map.of(
                        "planName", info.planName(),
                        "months", info.months(),
                        "amount", info.amount(),
                        "installments", info.installments(),
                        "startDate", info.startDate(),
                        "endDate", info.endDate(),
                        "orderId", info.orderId(),
                        "providerPaymentId", info.providerPaymentId(),
                        "paidAt", info.paidAt()
                );
                String html = templateService.process("email/subscription-paid", variables);
                helper.setTo(to);
                helper.setSubject("Licença Ativada - Meu SIM");
                helper.setText(html, true);
                mailSender.send(message);
                log.info("E-mail de licença paga enviado. [destinatario={}] [schoolId={}]", to, schoolId);
            }
            catch (Exception e) {
                log.error("Falha ao enviar e-mail de licença paga. [destinatario={}] [orderId={}] [motivo={}]",
                        to, info.orderId(), e.getMessage(), e);
            }
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendLessonEmailsToLegalGuardians(List<SendEmailLessonLegalGuardianDTO> emails) {
        for (SendEmailLessonLegalGuardianDTO dto : emails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                Map<String, Object> variables = new HashMap<>();
                variables.put("legalGuardianName", dto.legalGuardianName());
                variables.put("studentName", dto.attendance() != null ? dto.attendance().studentName() : null);
                variables.put("lessonDate", dto.lessonDate());
                variables.put("status", dto.status().getName());
                variables.put("responsibleUsername", dto.responsibleUsername());
                variables.put("classroomName", dto.classroomName());
                variables.put("subjectName", dto.subjectName());
                variables.put("startTime", dto.startTime());
                variables.put("endTime", dto.endTime());
                variables.put("description", dto.description());
                variables.put("attendance", dto.attendance());
                variables.put("note", dto.note());
                String html = templateService.process("email/lesson-notification", variables);
                helper.setTo(dto.legalGuardianEmail());
                helper.setSubject(dto.status().name().equals("CANCELED")
                        ? "Aula Cancelada - Meu S.I.M"
                        : "Resumo da Aula - Meu S.I.M");
                helper.setText(html, true);
                mailSender.send(message);
                log.info("E-mail de aula enviado para responsável. [destinatario={}] [status={}]",
                        dto.legalGuardianEmail(), dto.status());
            }
            catch (Exception e) {
                log.error("Falha ao enviar e-mail de aula para responsável. [destinatario={}] [motivo={}]",
                        dto.legalGuardianEmail(), e.getMessage(), e);
            }
        }
    }
}
