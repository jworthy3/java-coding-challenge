package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        Employee employee = employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId());
        if( employee == null ) {
            throw new RuntimeException("Invalid employeeId:" + compensation.getEmployee().getEmployeeId());
        }

        Compensation existingCompensation = compensationRepository.findByEmployee_EmployeeIdAndEffectiveDate(employee.getEmployeeId(), compensation.getEffectiveDate());
        if( existingCompensation != null ) {
            throw new RuntimeException("Compensation for employee with id '" + compensation.getEmployee().getEmployeeId() + "' and effective date " + compensation.getEffectiveDate() + " already exists.");
        }

        return compensationRepository.insert(compensation);
    }

    @Override
    public List<Compensation> search(String employeeId) {
        LOG.debug("Searching compensation collection with employeeId [{}]", employeeId);
        
        if( employeeId != null ) {
            return compensationRepository.findAllByEmployee_EmployeeId(employeeId);
        }

        return compensationRepository.findAll();
    }

    @Override
    public void deleteAll() {
        LOG.debug("Deleting compensation collection");

        compensationRepository.deleteAll();
    }
}
