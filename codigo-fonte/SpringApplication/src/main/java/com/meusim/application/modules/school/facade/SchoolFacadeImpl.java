package com.meusim.application.modules.school.facade;

import com.meusim.application.modules.identity.profile.schooladmin.SchoolAdmin;
import com.meusim.application.modules.school.School;
import com.meusim.application.modules.school.dto.CreateSchoolRequestDTO;
import com.meusim.application.modules.school.dto.SchoolCapacityResponseDTO;
import com.meusim.application.modules.school.dto.SchoolResponseDTO;
import com.meusim.application.modules.school.dto.UpdateSchoolRequestDTO;
import com.meusim.application.modules.school.query.SchoolAdminQuery;
import com.meusim.application.modules.school.query.SchoolCapacityQuery;
import com.meusim.application.modules.school.service.SchoolService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SchoolFacadeImpl implements SchoolFacade {
    private final SchoolService schoolService;
    private final SchoolAdminQuery schoolAdminQuery;
    private final SchoolCapacityQuery schoolCapacityQuery;

    public SchoolFacadeImpl(
            SchoolService schoolService,
            SchoolAdminQuery schoolAdminQuery,
            SchoolCapacityQuery schoolCapacityQuery) {
        this.schoolService = schoolService;
        this.schoolAdminQuery = schoolAdminQuery;
        this.schoolCapacityQuery = schoolCapacityQuery;
    }

    @Override
    public School getEntityById(UUID id) {
        return schoolService.findById(id);
    }

    @Override
    public School getEntityByUserId(UUID userId) {
        return schoolService.findByUserId(userId);
    }

    @Override
    public School getEntityByOwnerId() {
        return schoolService.findByOwnerId();
    }

    @Override
    public List<SchoolAdmin> getAllSchoolAdminFromSchool() {
        School school = schoolService.findByOwnerIdWithCache();
        return schoolAdminQuery.findAllBySchoolId(school.getId());
    }

    @Override
    public School getEntityByOwnerIdWithCache() {
        return schoolService.findByOwnerIdWithCache();
    }

    @Override
    public SchoolCapacityResponseDTO getCapacity(UUID schoolId) {
        return schoolCapacityQuery.getCapacity(schoolId);
    }

    @Override
    public SchoolResponseDTO getById(UUID id) {
        School s = schoolService.findByIdWithCache(id);
        return SchoolResponseDTO.of(s);
    }

    @Override
    public SchoolResponseDTO getByOwnerId() {
        School s = schoolService.findByOwnerIdWithCache();
        return SchoolResponseDTO.of(s);
    }

    @Override
    public School create(CreateSchoolRequestDTO dto) {
        return schoolService.create(dto);
    }

    @Override
    public School update(UUID id, UpdateSchoolRequestDTO dto) {
        return schoolService.update(id, dto);
    }
}
