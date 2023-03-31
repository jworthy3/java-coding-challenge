package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String employeeUrl;
    private String compensationUrl;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        compensationUrl = "http://localhost:" + port + "/compensation";
    }

    @After
    public void cleanUpDatabase() {
        compensationService.deleteAll();
    }

    @Test
    public void testCreate() {
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, new Employee(), Employee.class).getBody();
        assertNotNull(createdEmployee);
        assertNotNull(createdEmployee.getEmployeeId());

        LocalDate janFirstTwoThousand = LocalDate.of(2000, Month.JANUARY, 1);
        double salary = 100.5;

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(createdEmployee);
        testCompensation.setSalary(salary);
        testCompensation.setEffectiveDate(janFirstTwoThousand);

        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();

        assertNotNull(createdCompensation);
        assertNotNull(createdCompensation.getEmployee());
        assertNotNull(createdCompensation.getEffectiveDate());

        assertEquals(createdEmployee.getEmployeeId(), createdCompensation.getEmployee().getEmployeeId());
        assertEquals(janFirstTwoThousand, createdCompensation.getEffectiveDate());
        assertEquals(salary, createdCompensation.getSalary(), 0);
    }

    @Test
    public void testCreateWithInvalidEmployeeFails() {
        Employee invalidEmployee = new Employee();
        invalidEmployee.setEmployeeId(UUID.randomUUID().toString());

        LocalDate janFirstTwoThousand = LocalDate.of(2000, Month.JANUARY, 1);
        double salary = 100.5;

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(invalidEmployee);
        testCompensation.setSalary(salary);
        testCompensation.setEffectiveDate(janFirstTwoThousand);

        ResponseEntity<Compensation> compensationResponse = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class);
                                         
        assertNotNull(compensationResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, compensationResponse.getStatusCode());
    }

    @Test
    public void testCreateWithExistingEmployeeAndDateFails() {
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, new Employee(), Employee.class).getBody();
        assertNotNull(createdEmployee);
        assertNotNull(createdEmployee.getEmployeeId());

        LocalDate janFirstTwoThousand = LocalDate.of(2000, Month.JANUARY, 1);
        double salaryOne = 100.5;
        double salaryTwo = 200.5;

        Compensation testCompensationOne = new Compensation();
        testCompensationOne.setEmployee(createdEmployee);
        testCompensationOne.setSalary(salaryOne);
        testCompensationOne.setEffectiveDate(janFirstTwoThousand);

        Compensation createdCompensationOne = restTemplate.postForEntity(compensationUrl, testCompensationOne, Compensation.class).getBody();
        assertNotNull(createdCompensationOne);

        assertEquals(createdEmployee.getEmployeeId(), createdCompensationOne.getEmployee().getEmployeeId());
        assertEquals(janFirstTwoThousand, createdCompensationOne.getEffectiveDate());
        assertEquals(salaryOne, createdCompensationOne.getSalary(), 0);

        Compensation testCompensationTwo = new Compensation();
        testCompensationTwo.setEmployee(createdEmployee);
        testCompensationTwo.setSalary(salaryTwo);
        testCompensationTwo.setEffectiveDate(janFirstTwoThousand);

        ResponseEntity<Compensation> compensationTwoResponse = restTemplate.postForEntity(compensationUrl, testCompensationTwo, Compensation.class);
        assertNotNull(compensationTwoResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, compensationTwoResponse.getStatusCode());
    }

    @Test
    public void testSearch() {
        Employee createdEmployeeOne = restTemplate.postForEntity(employeeUrl, new Employee(), Employee.class).getBody();
        assertNotNull(createdEmployeeOne);
        assertNotNull(createdEmployeeOne.getEmployeeId());

        Employee createdEmployeeTwo = restTemplate.postForEntity(employeeUrl, new Employee(), Employee.class).getBody();
        assertNotNull(createdEmployeeTwo);
        assertNotNull(createdEmployeeTwo.getEmployeeId());

        LocalDate janFirstTwoThousand = LocalDate.of(2000, Month.JANUARY, 1);
        LocalDate janFirstOneThousand = LocalDate.of(100, Month.JANUARY, 1);
        double salary = 100.5;

        Compensation testCompensationOne = new Compensation();
        testCompensationOne.setEmployee(createdEmployeeOne);
        testCompensationOne.setSalary(salary);
        testCompensationOne.setEffectiveDate(janFirstTwoThousand);

        Compensation createdCompensationOne = restTemplate.postForEntity(compensationUrl, testCompensationOne, Compensation.class).getBody();
        assertNotNull(createdCompensationOne);

        Compensation testCompensationTwo = new Compensation();
        testCompensationTwo.setEmployee(createdEmployeeOne);
        testCompensationTwo.setSalary(salary);
        testCompensationTwo.setEffectiveDate(janFirstOneThousand);

        Compensation createdCompensationTwo = restTemplate.postForEntity(compensationUrl, testCompensationTwo, Compensation.class).getBody();
        assertNotNull(createdCompensationTwo);

        Compensation testCompensationThree = new Compensation();
        testCompensationThree.setEmployee(createdEmployeeTwo);
        testCompensationThree.setSalary(salary);
        testCompensationThree.setEffectiveDate(janFirstTwoThousand);

        Compensation createdCompensationThree = restTemplate.postForEntity(compensationUrl, testCompensationThree, Compensation.class).getBody();
        assertNotNull(createdCompensationThree);


        //search
        Compensation[] searchedCompensations = restTemplate.getForEntity(compensationUrl, Compensation[].class).getBody();
        assertNotNull(searchedCompensations);
        assertEquals(3, searchedCompensations.length);

        String compensationUrlWithEmployee = UriComponentsBuilder.fromHttpUrl(compensationUrl)
                .queryParam("employeeId", "{employeeId}")
                .encode()
                .toUriString();

        Map<String, String> params = Collections.singletonMap("employeeId", createdEmployeeOne.getEmployeeId());

        searchedCompensations = restTemplate.getForEntity(compensationUrlWithEmployee, Compensation[].class, params).getBody();
        assertNotNull(searchedCompensations);
        assertEquals(2, searchedCompensations.length);
    }
}
