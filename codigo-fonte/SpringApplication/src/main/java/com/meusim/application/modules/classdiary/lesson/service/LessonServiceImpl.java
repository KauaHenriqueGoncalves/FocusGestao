package com.meusim.application.modules.classdiary.lesson.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.meusim.application.auth.service.AuthenticatedUserService;
import com.meusim.application.modules.academic.classroom.Classroom;
import com.meusim.application.modules.academic.classroom.service.ClassroomService;
import com.meusim.application.modules.academic.classschedule.ClassSchedule;
import com.meusim.application.modules.academic.classschedule.enums.Weekday;
import com.meusim.application.modules.academic.classschedule.service.ClassScheduleService;
import com.meusim.application.modules.classdiary.attendance.dto.AttendanceViewResponseDTO;
import com.meusim.application.modules.classdiary.attendance.dto.CreateAttendanceRequestDTO;
import com.meusim.application.modules.classdiary.attendance.facade.AttendanceFacade;
import com.meusim.application.modules.classdiary.lesson.Lesson;
import com.meusim.application.modules.classdiary.lesson.cache.LessonCacheKeys;
import com.meusim.application.modules.classdiary.lesson.dto.AgendaDayResponseDTO;
import com.meusim.application.modules.classdiary.lesson.dto.CreateLessonRequestDTO;
import com.meusim.application.modules.classdiary.lesson.dto.GetToCreateLessonRequestDTO;
import com.meusim.application.modules.classdiary.lesson.dto.LabelToCreateLessonResponseDTO;
import com.meusim.application.modules.classdiary.lesson.enums.LessonDisplayStatus;
import com.meusim.application.modules.classdiary.lesson.enums.LessonStatus;
import com.meusim.application.modules.classdiary.lesson.event.CreateLessonToEmailEvent;
import com.meusim.application.modules.classdiary.lesson.repository.LessonRepository;
import com.meusim.application.modules.classdiary.lesson.validator.LessonValidator;
import com.meusim.application.modules.identity.base.user.dto.ResponsibleSnapshotDTO;
import com.meusim.application.modules.identity.base.user.facade.UserFacade;
import com.meusim.application.modules.school.School;
import com.meusim.application.modules.school.facade.SchoolFacade;
import com.meusim.application.shared.dto.PageResponse;
import com.meusim.application.shared.exception.BusinessException;
import com.meusim.application.shared.exception.NotFoundObjectException;
import com.meusim.application.shared.services.cache.CacheService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonServiceImpl implements LessonService {
    private static final Logger log = LoggerFactory.getLogger(LessonServiceImpl.class);
    private final AuthenticatedUserService authenticatedUserService;
    private final LessonRepository lessonRepository;
    private final LessonValidator validator;
    private final SchoolFacade schoolFacade;
    private final UserFacade userFacade;
    private final AttendanceFacade attendanceFacade;
    private final ClassroomService classroomService;
    private final ClassScheduleService scheduleService;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;

    public LessonServiceImpl(AuthenticatedUserService authenticatedUserService,
                             LessonRepository lessonRepository,
                             LessonValidator validator,
                             SchoolFacade schoolFacade,
                             UserFacade userFacade,
                             AttendanceFacade attendanceFacade,
                             ClassroomService classroomService,
                             ClassScheduleService scheduleService,
                             ApplicationEventPublisher eventPublisher,
                             CacheService cacheService) {
        this.authenticatedUserService = authenticatedUserService;
        this.lessonRepository = lessonRepository;
        this.validator = validator;
        this.schoolFacade = schoolFacade;
        this.userFacade = userFacade;
        this.attendanceFacade = attendanceFacade;
        this.classroomService = classroomService;
        this.scheduleService = scheduleService;
        this.eventPublisher = eventPublisher;
        this.cacheService = cacheService;
    }

    private AgendaDayResponseDTO buildAgendaDay(Lesson lesson,
                                                UUID scheduleId,
                                                LocalDate date,
                                                String weekdayDescription,
                                                LocalTime startTime,
                                                LocalTime endTime) {
        LessonDisplayStatus displayStatus;
        if (lesson != null) {
            displayStatus = lesson.getStatus() == LessonStatus.DONE
                    ? LessonDisplayStatus.DONE
                    : LessonDisplayStatus.CANCELED;
        } else {
            displayStatus = date.isBefore(LocalDate.now())
                    ? LessonDisplayStatus.LATE
                    : LessonDisplayStatus.PENDING;
        }
        return new AgendaDayResponseDTO(
                lesson != null ? lesson.getId() : null,
                scheduleId,
                date,
                weekdayDescription,
                startTime,
                endTime,
                displayStatus
        );
    }

    @Override
    public Page<Lesson> pageByClassroomId(UUID classroomId, int page, int size) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        School ownerSchool = schoolFacade.getEntityByOwnerIdWithCache();
        Classroom classroom = classroomService.findById(classroomId);
        log.info("Buscando página de agenda da turma pelo ID. [ownerId={}] [classroomId={}] [ownerSchoolId={}] [page={}] [size={}]",
                ownerId, classroom.getId(), ownerSchool.getId(), page, size);
        validator.ensureLessonClassroomBelongsSameSchool(ownerSchool, classroom);
        Pageable sortedPageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("lessonDate"), Sort.Order.desc("startTime")));
        Page<Lesson> lessonPage = lessonRepository.findAllByClassroomId(classroomId, sortedPageable);
        log.info("Pagina da agenda da turma pelo ID retornada com sucesso. [ownerId={}] [classroomId={}] [ownerSchoolId={}] [page={}] [size={}]",
                ownerId, classroom.getId(), ownerSchool.getId(), page, size);
        return lessonPage;
    }

    @Override
    public PageResponse<Lesson> pageByClassroomIdWithCache(UUID classroomId, int page, int size) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        String key = LessonCacheKeys.page(classroomId, page, size);
        log.info("Buscando página de agenda da turma pelo ID - with cache. [ownerId={}] [classroomId={}] [page={}] [size={}] [key={}]",
                ownerId, classroomId, page, size, key);
        Optional<PageResponse<Lesson>> cache = cacheService.get(key, new TypeReference<>(){});
        if (cache.isPresent()) {
            log.info("Pagina da agenda da turma encontrada com sucesso no cache. [ownerId={}] [classroomId={}] [page={}] [size={}]",
                    ownerId, classroomId, page, size);
            return cache.get();
        }
        Page<Lesson> lessonPage = pageByClassroomId(classroomId, page, size);
        PageResponse<Lesson> lessonPageResponse = PageResponse.from(lessonPage);
        log.info("Pagina da agenda da turma pelo ID retornada com sucesso e insirir no cache. [ownerId={}] [classroomId={}] [page={}] [size={}]",
                ownerId, classroomId, page, size);
        cacheService.set(key, lessonPageResponse, LessonCacheKeys.TTL);
        return lessonPageResponse;
    }

    @Override
    public List<AgendaDayResponseDTO> findAgendaByMonth(UUID classroomId, int year, int month) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        School ownerSchool = schoolFacade.getEntityByOwnerIdWithCache();
        log.info("Buscando a agenda do pelo mes e ano. [ownerId={}] [schoolId={}] [classroomId={}] [year={}] [month={}]",
                ownerId, ownerSchool.getId(), classroomId, year, month);
        Classroom classroom = classroomService.findById(classroomId);
        validator.ensureLessonClassroomBelongsSameSchool(ownerSchool, classroom);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        log.info("Bucando pelo periodo. [ownerId={}] [classroomId={}] [start={}] [end={}]",
                ownerId, classroomId, startDate, endDate);
        List<ClassSchedule> schedules = scheduleService.findAllByClassroomId(classroomId);
        List<Lesson> existingLessons = lessonRepository.findByClassroomIdAndLessonDateBetween(classroomId, startDate, endDate);
        log.info("Total de agenda encontradas. [ownerId={}] [classroomId={}] [size={}]",
                ownerId, classroomId, existingLessons.size());
        Map<String, Lesson> lessonByKey = existingLessons.stream()
                .collect(Collectors.toMap(
                        l -> l.getScheduleId() + "_" + l.getLessonDate(), // key
                        l -> l                                            // value
                ));
        Set<String> visitedKeys = new HashSet<>();
        ZoneId zoneId = ZoneId.systemDefault();
        List<AgendaDayResponseDTO> agendas = new ArrayList<>();

        // Passo 1: schedules ainda existentes (comportamento normal)
        for (ClassSchedule schedule : schedules) {
            LocalDate scheduleCreatedDate = schedule.getCreatedAt().atZone(zoneId).toLocalDate();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (date.getDayOfWeek().getValue() != schedule.getWeekday().getOrder()) {
                    continue;
                }
                String key = schedule.getId() + "_" + date;
                Lesson lesson = lessonByKey.get(key);
                boolean isBeforeScheduleCreation = date.isBefore(scheduleCreatedDate);
                if (lesson == null && isBeforeScheduleCreation) {
                    continue;
                }
                if (date.getYear() != LocalDate.now().getYear() && lesson == null) {
                    continue;
                }
                visitedKeys.add(key);
                agendas.add(buildAgendaDay(
                        lesson,
                        schedule.getId(),
                        date,
                        schedule.getWeekday().getDescription(),
                        schedule.getStartTime(),
                        schedule.getEndTime())
                );
            }
        }

        // Passo 2: lessons "órfãs", schedule foi excluído, mas o registro existe
        for (Lesson lesson : existingLessons) {
            String key = lesson.getScheduleId() + "_" + lesson.getLessonDate();
            if (visitedKeys.contains(key)) {
                continue;
            }
            agendas.add(buildAgendaDay(
                    lesson,
                    lesson.getScheduleId(),
                    lesson.getLessonDate(),
                    Weekday.fromOrder(lesson.getWeekday()).getDescription(),
                    lesson.getStartTime(),
                    lesson.getEndTime())
            );
        }
        agendas.sort(Comparator.comparing(AgendaDayResponseDTO::date)
                .thenComparing(AgendaDayResponseDTO::startTime));
        log.info("Total de agendas enviadas. [ownerId={}] [classroomId={}] [registred={}] [total={}]",
                ownerId, classroomId, existingLessons.size(), agendas.size());
        return agendas;
    }

    @Override
    public List<AgendaDayResponseDTO> findAgendaByMonthWithCache(UUID classroomId, int year, int month) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        String key = LessonCacheKeys.agenda(classroomId, year, month);
        log.info("Buscando a agenda do pelo mes e ano - with cache. [ownerId={}] [classroomId={}] [year={}] [month={}] [key={}]",
                ownerId, classroomId, year, month, key);
        Optional<List<AgendaDayResponseDTO>> cache = cacheService.get(key, new TypeReference<>(){});
        if (cache.isPresent()) {
            log.info("Total de agendas encontrados no cache. [ownerId={}] [classroomId={}] [total={}]",
                    ownerId, classroomId, cache.get().size());
            return cache.get();
        }
        List<AgendaDayResponseDTO> agenda = findAgendaByMonth(classroomId, year, month);
        log.info("Total de agendas encontrados e insirir no cache. [ownerId={}] [classroomId={}] [total={}]",
                ownerId, classroomId, agenda.size());
        cacheService.set(key, agenda, LessonCacheKeys.TTL);
        return agenda;
    }

    @Override
    public LabelToCreateLessonResponseDTO getLabelToCreateLesson(UUID classroomId, GetToCreateLessonRequestDTO dto) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        School ownerSchool = schoolFacade.getEntityByOwnerIdWithCache();
        log.info("Buscando o label para criar a agenda. [ownerId={}] [schoolId={}] [classroomId={}] [lessonDate={}]",
                ownerId, ownerSchool.getId(), classroomId, dto.lessonDate());
        Classroom classroom = classroomService.findById(classroomId);
        validator.ensureLessonClassroomBelongsSameSchool(ownerSchool, classroom);
        ClassSchedule schedule = scheduleService.findById(dto.scheduleId());
        validator.ensureLessonClassroomBelongsSchedule(schedule, classroom);
        LocalDate lessonDate = (dto.lessonDate() != null) ? dto.lessonDate() : LocalDate.now();
        validator.ensureLessonDateMatchesScheduleWeekday(schedule, lessonDate);
        validator.ensureDontExistsByScheduleIdAndLessonDate(schedule, lessonDate);
        if (lessonDate.getYear() != LocalDate.now().getYear()) {
            log.warn("Tentativa de buscar label com data não correspondente ao ano atual. [ownerId={}] [schoolId={}] [classroomId={}] [scheduleId={}]",
                    ownerId, ownerSchool.getId(), classroomId, dto.scheduleId());
            throw new BusinessException("A data informada não corresponde ao ano atual.");
        }
        List<CreateAttendanceRequestDTO> attendances =
                CreateAttendanceRequestDTO.labelToCreateLesson(classroom.getStudents());
        log.info("Label de presença montado a partir dos estudantes da turma. [ownerId={}] [classroomId={}] [totalStudents={}]",
                ownerId, classroomId, attendances.size());
        LabelToCreateLessonResponseDTO label = new LabelToCreateLessonResponseDTO(
                schedule.getId(),
                classroom.getId(),
                dto.lessonDate(),
                false,
                "",
                attendances
        );
        log.info("Label para criação de agenda retornado com sucesso. [ownerId={}] [classroomId={}] [scheduleId={}] [lessonDate={}] [totalAttendances={}]",
                ownerId, classroomId, schedule.getId(), lessonDate, attendances.size());
        return label;
    }

    @Override
    public LabelToCreateLessonResponseDTO getLabelToCreateLessonWithCache(UUID classroomId, GetToCreateLessonRequestDTO dto) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        String key = LessonCacheKeys.label(classroomId, dto.lessonDate());
        Optional<LabelToCreateLessonResponseDTO> cache = cacheService.get(key, new TypeReference<>(){});
        if (cache.isPresent()) {
            log.info("Label encontrado no cache para Agenda. [ownerId={}] [classroomId={}] [key={}]",
                    ownerId, classroomId, key);
            return cache.get();
        }
        LabelToCreateLessonResponseDTO label = getLabelToCreateLesson(classroomId, dto);
        cacheService.set(key, label, LessonCacheKeys.TTL);
        return label;
    }

    @Override
    public Lesson findById(UUID id) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        log.info("Buscando agenda do reforco pelo ID no banco. [ownerId={}] [lessonId={}]",
                ownerId, id);
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("A agenda do reforco não foi econtrado pelo Id no banco. [ownerId={}] [lessonId={}]",
                            ownerId, id);
                    return new NotFoundObjectException("Agenda não foi encontrada");
                });
        log.info("Agenda encontrada com sucesso pelo ID do banco. [ownerId={}] [lessonId={}]",
                ownerId, id);
        return lesson;
    }

    @Override
    public Lesson findByIdWithCache(UUID id) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        String key = LessonCacheKeys.byId(id);
        log.info("Buscando agenda do reforco pelo ID - with cache. [ownerId={}] [lessonId={}]",
                ownerId, id);
        Optional<Lesson> cache = cacheService.get(key, new TypeReference<>(){});
        if (cache.isPresent()) {
            log.info("Agenda encontrada com sucesso pelo ID no cache. [ownerId={}] [lessonId={}]",
                    ownerId, id);
            return cache.get();
        }
        Lesson lesson = findById(id);
        log.info("Agenda encontrada com sucesso pelo ID e inserir no cache. [ownerId={}] [lessonId={}]",
                ownerId, id);
        cacheService.set(key, lesson, LessonCacheKeys.TTL);
        return lesson;
    }

    @Override
    public List<AttendanceViewResponseDTO> findAllAttendancesViewsByLessonId(UUID lessonId) {
        return attendanceFacade.getAllViewByLessonId(lessonId);
    }

    @Override
    @Transactional
    public Lesson create(CreateLessonRequestDTO dto) {
        UUID ownerId = authenticatedUserService.getOwnerId();
        School school = schoolFacade.getEntityByOwnerIdWithCache();
        log.info("Criado uma nova Agenda da turma. [ownerId={}] [schoolId={}] [classroomId={}] [scheduleId={}]",
                ownerId, school.getId(), dto.classroomId(), dto.scheduleId());
        ResponsibleSnapshotDTO responsibleSnapshot = userFacade.getResponsible(ownerId);
        Classroom classroom = classroomService.findById(dto.classroomId());
        validator.ensureSchoolHasSubscription(school);
        validator.ensureLessonClassroomBelongsSameSchool(school, classroom);
        ClassSchedule schedule = scheduleService.findById(dto.scheduleId());
        validator.ensureLessonClassroomBelongsSchedule(schedule, classroom);
        LocalDate lessonDate = (dto.lessonDate() != null) ? dto.lessonDate() : LocalDate.now();
        validator.ensureLessonDateIsNotFuture(lessonDate);
        validator.ensureLessonDateMatchesScheduleWeekday(schedule, lessonDate);
        validator.ensureDontExistsByScheduleIdAndLessonDate(schedule, lessonDate);
        if (lessonDate.getYear() != LocalDate.now().getYear()) {
            log.warn("Tentativa de criar uma agenda não correspondete a data atual. [ownerId={}] [schoolId={}] [classroomId={}] [scheduleId={}]",
                    ownerId, school.getId(), dto.classroomId(), dto.scheduleId());
            throw new BusinessException("A data informada não corresponde ao ano atual.");
        }
        Lesson lesson = new Lesson();
        lesson.setClassroom(classroom);
        lesson.setLessonDate(lessonDate);
        lesson.setStatus(dto.isCanceled() ? LessonStatus.CANCELED : LessonStatus.DONE);
        lesson.setResponsibleId(responsibleSnapshot.responsibleId());
        lesson.setResponsibleUsername(responsibleSnapshot.username());
        lesson.setResponsibleRole(responsibleSnapshot.role());
        lesson.setClassroomName(classroom.getName());
        lesson.setSubjectName(classroom.getSubject().getName());
        lesson.setScheduleId(schedule.getId());
        lesson.setWeekday(schedule.getWeekday().getOrder());
        lesson.setStartTime(schedule.getStartTime());
        lesson.setEndTime(schedule.getEndTime());
        lesson.setDescription(dto.description());
        lesson = lessonRepository.save(lesson);
        log.info("Agenda criada com sucesso. [ownerId={}] [schoolId={}] [lessonId={}] [status={}]",
                ownerId, school.getId(), lesson.getId(), lesson.getStatus());
        if (lesson.getStatus() == LessonStatus.DONE) {
            attendanceFacade.createAll(lesson, dto.attendances());
            log.info("Presencas registradas para a agenda. [ownerId={}] [lessonId={}] [totalAttendances={}]",
                    ownerId, lesson.getId(), dto.attendances().size());
        } else {
            log.info("Agenda cancelada, presencas nao serao registradas. [ownerId={}] [lessonId={}]",
                    ownerId, lesson.getId());
        }
        log.info("Agenda criada e limpar os cache relacionados. [ownerId={}] [schoolId={}] [lessonId={}] [status={}]",
                ownerId, school.getId(), lesson.getId(), lesson.getStatus());
        eventPublisher.publishEvent(
                new CreateLessonToEmailEvent(lesson.getId())
        );
        cacheService.delete(LessonCacheKeys.agenda(classroom.getId(), lessonDate.getYear(), lessonDate.getMonth().getValue()));
        cacheService.evictByPattern(LessonCacheKeys.pagePattern(classroom.getId()));
        return lesson;
    }
}
