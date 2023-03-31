package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {
    List<Compensation> findAllByEmployee_EmployeeId(String employeeId);

    Compensation findByEmployee_EmployeeIdAndEffectiveDate(String employeeId, LocalDate effectiveDate);
}
