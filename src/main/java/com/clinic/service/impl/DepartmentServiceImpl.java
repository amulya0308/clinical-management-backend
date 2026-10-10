package com.clinic.service.impl;

import com.clinic.dto.request.DepartmentRequest;
import com.clinic.dto.response.DepartmentResponse;
import com.clinic.entity.Department;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DepartmentMapper;
import com.clinic.repository.DepartmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            DoctorRepository doctorRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String name = request.getName().trim();
        log.info("Creating department: {}", name);

        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Department already exists with name: " + name);
        }

        Department department = Department.builder()
                .name(name)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();
        department = departmentRepository.save(department);

        return departmentMapper.toResponse(department, 0);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        String name = request.getName().trim();
        log.info("Updating department id: {} with name: {}", id, name);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Another department already exists with name: " + name);
        }

        department.setName(name);
        department.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        department = departmentRepository.save(department);

        long doctorCount = doctorRepository.countByDepartmentId(id);
        return departmentMapper.toResponse(department, doctorCount);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Attempting to delete department id: {}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        if (doctorRepository.existsByDepartmentId(id)) {
            throw new ConflictException("Cannot delete department '" + department.getName() + "' because active doctors are assigned to it. Reassign or remove doctors first.");
        }

        departmentRepository.delete(department);
        log.info("Deleted department id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    long count = doctorRepository.countByDepartmentId(dept.getId());
                    return departmentMapper.toResponse(dept, count);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        long count = doctorRepository.countByDepartmentId(id);
        return departmentMapper.toResponse(department, count);
    }
}
