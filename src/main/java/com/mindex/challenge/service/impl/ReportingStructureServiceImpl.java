package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {
    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure read(String employeeId) {
        LOG.debug("Creating reporting structure for employee with id [{}]", employeeId);

        Employee employee = employeeRepository.findByEmployeeId(employeeId);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeId);
        }

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee);

        if( employee.getDirectReports() != null ) {
            Set<String> uniqueReports = getUniqueReports(employee.getDirectReports());
            reportingStructure.setNumberOfReports(uniqueReports.size());
        }

        return reportingStructure;
    }

    /**
     * Gathers the ids of all reports from the provided list of direct reports and any reports under them.
     *
     * @param directReports list of direct reports for an employee.
     * @return a set containing the ids of all found reports.
     */
    private Set<String> getUniqueReports(List<Employee> directReports) {
        Set<String> unique = new HashSet<>();

        for( Employee directReport : directReports ) {

            Employee employee = employeeRepository.findByEmployeeId(directReport.getEmployeeId());

            if (employee == null) {
                throw new RuntimeException("Invalid employeeId: " + directReport.getEmployeeId());
            }

            unique.add(employee.getEmployeeId());

            if( employee.getDirectReports() != null ) {
                unique.addAll(getUniqueReports(employee.getDirectReports()));
            }
        }

        return unique;
    }
}
