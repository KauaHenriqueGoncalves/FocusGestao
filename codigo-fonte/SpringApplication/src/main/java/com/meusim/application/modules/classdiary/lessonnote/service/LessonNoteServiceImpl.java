package com.meusim.application.modules.classdiary.lessonnote.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meusim.application.auth.service.AuthenticatedUserService;
import com.meusim.application.modules.classdiary.attendance.Attendance;
import com.meusim.application.modules.classdiary.lessonnote.LessonNote;
import com.meusim.application.modules.classdiary.lessonnote.cache.LessonNoteCacheKeys;
import com.meusim.application.modules.classdiary.lessonnote.dto.CreateLessonNoteRequestDTO;
import com.meusim.application.modules.classdiary.lessonnote.repository.LessonNoteRepository;
import com.meusim.application.shared.services.cache.CacheService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class LessonNoteServiceImpl implements LessonNoteService {
    private static final Logger log = LoggerFactory.getLogger(LessonNoteServiceImpl.class);
    private final AuthenticatedUserService authenticatedUserService;
    private final CacheService cacheService;
    private final LessonNoteRepository lessonNoteRepository;

    public LessonNoteServiceImpl(AuthenticatedUserService authenticatedUserService,
                                 CacheService cacheService,
                                 LessonNoteRepository lessonNoteRepository) {
        this.authenticatedUserService = authenticatedUserService;
        this.cacheService = cacheService;
        this.lessonNoteRepository = lessonNoteRepository;
    }

    @Override
    public LessonNote findByAttendanceId(UUID attendanceId) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        log.info("Buscando nota de estudante para a presenca com attendanceId (pode ser null). [ownerId={}] [attendanceId={}]",
                ownerId, attendanceId);
        LessonNote lessonNote = lessonNoteRepository.findByAttendanceId(attendanceId).orElse(null);
        log.info("Nota da presenca encontrada com sucesso. [ownerId={}] [note={}]",
                ownerId, lessonNote);
        return lessonNote;
    }

    @Override
    public LessonNote findByAttendanceIdWithCache(UUID attendanceId) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        String key = LessonNoteCacheKeys.byAttendance(attendanceId);
        Optional<LessonNote> cache = cacheService.get(key, new TypeReference<>(){});
        if (cache.isPresent()) {
            log.info("Nota achada no cache. [ownerId={}] [attendanceId={}] [noteId={}]",
                    ownerId, attendanceId, cache.get().getId());
            return cache.get();
        }
        LessonNote note = findByAttendanceId(attendanceId);
        cacheService.set(key, note, LessonNoteCacheKeys.TTL);
        return note;
    }

    @Override
    @Transactional
    public LessonNote create(Attendance attendanceEntity, CreateLessonNoteRequestDTO dto) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        log.info("Criando nota para presenca. [ownerId={}] [attendanceId={}]", ownerId, attendanceEntity.getId());
        if (attendanceEntity == null || dto == null) {
            log.warn("Nota nao é armazenada quando algum dado e nula. [ownerId={}]", ownerId);
            return null;
        }
        LessonNote note = lessonNoteRepository.save(LessonNote.initFrom(attendanceEntity, dto));;
        log.info("Nota criada com sucesso. [ownerId={}] [attendanceId={}] [noteId={}]",
                ownerId, attendanceEntity.getId(), note.getId());
        return note;
    }
}
