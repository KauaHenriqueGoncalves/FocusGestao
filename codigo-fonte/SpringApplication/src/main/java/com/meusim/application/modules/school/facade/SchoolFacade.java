package com.meusim.application.modules.school.facade;

import com.meusim.application.modules.identity.profile.schooladmin.SchoolAdmin;
import com.meusim.application.modules.school.School;
import com.meusim.application.modules.school.dto.CreateSchoolRequestDTO;
import com.meusim.application.modules.school.dto.SchoolCapacityResponseDTO;
import com.meusim.application.modules.school.dto.SchoolResponseDTO;
import com.meusim.application.modules.school.dto.UpdateSchoolRequestDTO;

import java.util.List;
import java.util.UUID;

public interface SchoolFacade {
    School getEntityById(UUID id);
    School getEntityByUserId(UUID userId);
    School getEntityByOwnerId();
    List<SchoolAdmin> getAllSchoolAdminFromSchool();
    School getEntityByOwnerIdWithCache();
    SchoolCapacityResponseDTO getCapacity(UUID schoolId);
    SchoolResponseDTO getById(UUID id);
    SchoolResponseDTO getByOwnerId();
    School create(CreateSchoolRequestDTO dto);
    School update(UUID id, UpdateSchoolRequestDTO dto);
}
