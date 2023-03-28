package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("DataFlowIssue")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplTest {

    private String employeeUrl;
    private String reportingStructureIdUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        reportingStructureIdUrl = "http://localhost:" + port + "/reporting-structure/{employeeId}";
    }

    @Test
    public void testEmptyStructure() {
        // Employee creation
        Employee testEmployee = createEmployee("Tester");

        //ReportingStructure checks
        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, testEmployee.getEmployeeId()).getBody();
        assertNotNull(readReportingStructure.getEmployee());
        assertEquals(testEmployee.getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());
        assertEquals(0, readReportingStructure.getNumberOfReports());
    }

    @Test
    public void testFlatStructure() {
        // Level Two employee creation
        Employee employeeLevelTwoA = createEmployee("Level Two A");
        Employee employeeLevelTwoB = createEmployee("Level Two B");
        Employee employeeLevelTwoC = createEmployee("Level Two C");
        Employee employeeLevelTwoD = createEmployee("Level Two D");

        // Level One employee creation
        Employee employeeLevelOne = createEmployee("Level One", employeeLevelTwoA, employeeLevelTwoB, employeeLevelTwoC, employeeLevelTwoD);

        //ReportingStructure creation
        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, employeeLevelOne.getEmployeeId()).getBody();

        //validate ReportingStructure employee
        assertNotNull(readReportingStructure.getEmployee());
        assertEquals(employeeLevelOne.getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());

        //validate direct report counts and ReportingStructure number of reports
        assertEquals(4, employeeLevelOne.getDirectReports().size());
        assertEquals(4, readReportingStructure.getNumberOfReports()); // 2 level-two + 3 level-three = 5
    }

    @Test
    public void testNestedStructure() {
        // Level Three employee creation
        Employee employeeLevelThreeA = createEmployee("Level Three A");
        Employee employeeLevelThreeB = createEmployee("Level Three B");
        Employee employeeLevelThreeC = createEmployee("Level Three C");

        // Level Two employee creation
        Employee employeeLevelTwoA = createEmployee("Level Two A", employeeLevelThreeA, employeeLevelThreeB);
        Employee employeeLevelTwoB = createEmployee("Level Two B", employeeLevelThreeC);

        // Level One employee creation
        Employee employeeLevelOne = createEmployee("Level One", employeeLevelTwoA, employeeLevelTwoB);

        //ReportingStructure creation
        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, employeeLevelOne.getEmployeeId()).getBody();

        //validate ReportingStructure employee
        assertNotNull(readReportingStructure.getEmployee());
        assertEquals(employeeLevelOne.getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());

        //validate direct report counts and ReportingStructure number of reports
        assertEquals(2, employeeLevelTwoA.getDirectReports().size());
        assertEquals(1, employeeLevelTwoB.getDirectReports().size());
        assertEquals(2, employeeLevelOne.getDirectReports().size());

        assertEquals(5, readReportingStructure.getNumberOfReports()); // 2 level-two + 3 level-three = 5
    }

    @Test
    public void testDuplicatesInStructure() {
        // Level Three employee creation
        Employee employeeLevelThreeA = createEmployee("Level Three A");
        Employee employeeLevelThreeB = createEmployee("Level Three B");

        // Level Two employee creation. (give both level two employees the same level three employees)
        Employee employeeLevelTwoA = createEmployee("Level Two A", employeeLevelThreeA, employeeLevelThreeB);
        Employee employeeLevelTwoB = createEmployee("Level Two B", employeeLevelThreeA, employeeLevelThreeB);

        // Level One employee creation
        Employee employeeLevelOne = createEmployee("Level One", employeeLevelTwoA, employeeLevelTwoB);

        //ReportingStructure creation
        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, employeeLevelOne.getEmployeeId()).getBody();

        //validate ReportingStructure employee
        assertNotNull(readReportingStructure.getEmployee());
        assertEquals(employeeLevelOne.getEmployeeId(), readReportingStructure.getEmployee().getEmployeeId());

        //validate direct report counts and ReportingStructure number of reports
        assertEquals(2, employeeLevelTwoA.getDirectReports().size());
        assertEquals(2, employeeLevelTwoB.getDirectReports().size());
        assertEquals(2, employeeLevelOne.getDirectReports().size());

        assertEquals(4, readReportingStructure.getNumberOfReports()); // 2 level-two + 2 UNIQUE level-three = 5
    }


    //helper functions
    private Employee createEmployee(String name, Employee... directReports ) {
        Employee employee = new Employee();
        employee.setFirstName(name);

        if( directReports.length > 0 ) {
            List<Employee> directReportsList = new ArrayList<>(directReports.length);
            Collections.addAll(directReportsList, directReports);
            employee.setDirectReports(directReportsList);
        }

        return restTemplate.postForEntity(employeeUrl, employee, Employee.class).getBody();
    }
}
