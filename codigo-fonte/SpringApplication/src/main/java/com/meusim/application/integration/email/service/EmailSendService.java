package com.meusim.application.integration.email.service;

import com.meusim.application.integration.email.dto.SendEmailLessonLegalGuardianDTO;
import com.meusim.application.integration.email.dto.SendEmailSubscriptionPaid;
import java.util.List;
import java.util.UUID;

public interface EmailSendService {
    void sendConfirmAccountEmail(String to, String name, String schoolCode, String link);
    void sendSubscriptionPaidEmails(List<String> emails, UUID schoolId, SendEmailSubscriptionPaid info);
    void sendLessonEmailsToLegalGuardians(List<SendEmailLessonLegalGuardianDTO> emails);
}
