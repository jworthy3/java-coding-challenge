package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;

import java.util.List;

public interface CompensationService {
    Compensation create(Compensation compensation);
    List<Compensation> search(String employeeId);
    void deleteAll();
}
