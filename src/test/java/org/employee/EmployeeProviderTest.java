package org.employee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.junit.jupiter.api.*;

import java.io.InvalidClassException;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeProviderTest {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private InvalidClassException exception;

    @BeforeEach
    void setUp() {
        mongoClient = MongoClients.create("mongodb+srv://rachitaravikumar1104:Ol6dWljHS5VBqVAK@clustertest0.gedrv54.mongodb.net/?retryWrites=true&w=majority&appName=ClusterTest0");
        database = mongoClient.getDatabase("com_employee");
        collection = database.getCollection("employee");
        collection.createIndex(new Document("employeeID", 1), new IndexOptions().unique(true)); // Ensure unique index
        collection.createIndex(new Document("name", "text"));
    }

    @AfterEach
    void tearDown() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Test
    @Order(1)
    void createAndValidateMultipleEmployees() throws JsonProcessingException, EmployeeException {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        // Create multiple employee objects
        Employee[] employees = new Employee[]{
                employeeProvider.createEmployeeObject(1, "John Doe", "123 Main St", 1234567890, 12345),
                employeeProvider.createEmployeeObject(2, "Sam Smith", "456 High St", 907584362, 67890),
                employeeProvider.createEmployeeObject(3, "Jane Doe", "789 Oak St", 555666777, 54321),
                employeeProvider.createEmployeeObject(4, "Alice Johnson", "321 Maple St", 444555666, 98765),
                employeeProvider.createEmployeeObject(5, "Bob Brown", "654 Pine St", 222333444, 87654),
                employeeProvider.createEmployeeObject(6, "Charlie Davis", "987 Elm St", 1112223333, 76543),
                employeeProvider.createEmployeeObject(7, "Daisy Miller", "432 Birch St", 999888777, 65432),
                employeeProvider.createEmployeeObject(8, "Eve Wilson", "765 Cedar St", 888777666, 54321),
                employeeProvider.createEmployeeObject(9, "Frank Thompson", "210 Spruce St", 777666555, 43210),
                employeeProvider.createEmployeeObject(10, "Grace Lee", "543 Walnut St", 666555444, 32109),
                employeeProvider.createEmployeeObject(11, "Emma Doe", "1234 Elm St", 222111333, 34567),
                employeeProvider.createEmployeeObject(12, "Oliver Doe", "5678 Maple St", 333222444, 45678),
                employeeProvider.createEmployeeObject(13, "Sophia Smith", "9101 Pine St", 444333555, 56789),
                employeeProvider.createEmployeeObject(14, "Liam Smith", "1213 Birch St", 555444666, 67890)
        };


        // Insert and validate each employee
        for (Employee employee : employees) {
            Employee createdEmployee = EmployeeProvider.createEmployee(collection, employee);

            assertNotNull(createdEmployee, "The created employee should not be null.");
            assertEquals(employee.getEmployeeID(), createdEmployee.getEmployeeID(), "Employee ID should match.");
            assertEquals(employee.getName(), createdEmployee.getName(), "Employee name should match.");
            assertEquals(employee.getAddress(), createdEmployee.getAddress(), "Employee address should match.");
            assertEquals(employee.getContactNumber(), createdEmployee.getContactNumber(), "Employee contact number should match.");
            assertEquals(employee.getPincode(), createdEmployee.getPincode(), "Employee pincode should match.");
        }
    }

    @Test
    @Order(2)
    void duplicateEmployeeID() throws JsonProcessingException, EmployeeException {
        EmployeeProvider employeeProvider = new EmployeeProvider();
        Employee employee = employeeProvider.createEmployeeObject(1, "John Doe", "123 Main St", 1234567890, 12345);

        // Attempt to insert the same employee again, expecting an EmployeeException
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            EmployeeProvider.createEmployee(collection, employee);
        });

        String expectedMessage = "Employee with ID " + employee.getEmployeeID() + " already exists.";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @Order(3)
    void invalidEmployeeID() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        // Invalid employee creation: missing/invalid fields
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee invalidEmployee = employeeProvider.createEmployeeObject(0, "John Doe", "123 Main St", 1234567890, 12345);
            EmployeeProvider.createEmployee(collection, invalidEmployee);
        });
        // Use assertEquals to compare the exception message
        String expectedMessage = "Employee ID is mandatory and must be greater than 0";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @Order(4)
    void createEmployeeWithNullName() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        // Invalid employee creation: missing/invalid fields
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee invalidEmployee = employeeProvider.createEmployeeObject(1, null, "123 Main St", 1234567890, 12345);
            EmployeeProvider.createEmployee(collection, invalidEmployee);
        });

        // Use assertEquals to compare the exception message
        String expectedMessage = "Employee name is mandatory";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @Order(5)
    void employeeNameWithSpecialCharacters() {
        EmployeeProvider employeeProvider = new EmployeeProvider();
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee employeeInvalid = employeeProvider.createEmployeeObject(2, "Jane@Doe", "456 Elm St", 987654321, 54321);
            EmployeeProvider.createEmployee(collection, employeeInvalid);
        });
        String expectedMessage = "Employee name must not contain special characters except spaces and underscores";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @Order(6)
    void emptyEmployeeAddress() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee invalidEmployee = employeeProvider.createEmployeeObject(1, "John Doe", "", 1234567890, 12345);
            EmployeeProvider.createEmployee(collection, invalidEmployee);
        });
        String expectedMessage = "Employee address is mandatory";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @Order(7)
    void updateEmployee() throws JsonProcessingException, EmployeeException {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        // Update the employee with valid data
        Employee updatedEmployee = employeeProvider.createEmployeeObject(1, "John Doe", "123 Main St UPDATED", 535555555, 6464664);
        Employee createdEmployee = EmployeeProvider.updateEmployee(collection, updatedEmployee);

        assertNotNull(createdEmployee, "The updated employee should not be null.");
        assertEquals(updatedEmployee.getEmployeeID(), createdEmployee.getEmployeeID(), "Employee ID should match.");
        assertEquals(updatedEmployee.getName(), createdEmployee.getName(), "Employee name should match.");
        assertEquals(updatedEmployee.getAddress(), createdEmployee.getAddress(), "Employee address should match.");
        assertEquals(updatedEmployee.getContactNumber(), createdEmployee.getContactNumber(), "Employee contact number should match.");
        assertEquals(updatedEmployee.getPincode(), createdEmployee.getPincode(), "Employee pincode should match.");
    }

    @Test
    @Order(8)
    void updateEmployeeWithInvalidID() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee invalidEmployee = employeeProvider.createEmployeeObject(0, "Jane Doe", "456 Elm St", 987654321, 54321);
            EmployeeProvider.updateEmployee(collection, invalidEmployee);
        });
        String expectedMessage = "Employee ID is mandatory and must be greater than 0";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @Order(9)
    void updateEmployeeWithEmptyAddress() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee invalidEmployee = employeeProvider.createEmployeeObject(1, "Jane Doe", "", 987654321, 54321);
            EmployeeProvider.updateEmployee(collection, invalidEmployee);
        });
        String expectedMessage = "Employee address is mandatory";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @Order(10)
    void updateEmployeeNameChange() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        Employee invalidUpdatedEmployee = employeeProvider.createEmployeeObject(1, "Jane Doe", "789 Pine St", 555555555, 11111);
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            EmployeeProvider.updateEmployee(collection, invalidUpdatedEmployee);
        });
        String expectedMessage = "Name field cannot be changed";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @Order(11)
    void updateEmployeeWithSpecialCharactersInName() throws EmployeeException, JsonProcessingException {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        Employee invalidNameEmployee = employeeProvider.createEmployeeObject(1, "Jane@Doe", "456 Elm St", 987654321, 54321);
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            EmployeeProvider.updateEmployee(collection, invalidNameEmployee);
        });
        String expectedMessage = "Employee name must not contain special characters except space and underscore";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @Order(12)
    void updateNonExistentEmployee() {
        EmployeeProvider employeeProvider = new EmployeeProvider();

        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            Employee nonExistentEmployee = employeeProvider.createEmployeeObject(222, "Non Existent", "999 Nowhere St", 1111111111, 99999);
            EmployeeProvider.updateEmployee(collection, nonExistentEmployee);
        });
        String expectedMessage = "No employee found with ID: 222";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    @Order(13)
    void getEmployeeByID() throws EmployeeException {
        // Retrieve the employee by ID
        Employee retrievedEmployee = EmployeeProvider.getEmployeeByID(collection, 1);
        assertNotNull(retrievedEmployee, "The retrieved employee should not be null.");
        assertEquals(retrievedEmployee.getEmployeeID(), 1, "Employee ID is not matching.");
        // Attempt to retrieve with invalid employeeID
        EmployeeException nullIDException = assertThrows(EmployeeException.class, () -> {
            EmployeeProvider.getEmployeeByID(collection, 0);
        });
        assertTrue(nullIDException.getMessage().contains("Employee ID is mandatory and must be greater than 0"));

        // Attempt to retrieve a non-existent employee
        Employee nonExistentEmployee = assertDoesNotThrow(() -> EmployeeProvider.getEmployeeByID(collection, 20));
        assertNull(nonExistentEmployee, "No employee should be found with the given ID.");

    }

    @Test
    @Order(14)
    void invalidEmployeeIDForRetrieval() {
        // Attempt to retrieve with invalid employeeID
        EmployeeException exception = assertThrows(EmployeeException.class, () -> {
            EmployeeProvider.getEmployeeByID(collection, 0);
        });
        assertEquals("Employee ID is mandatory and must be greater than 0", exception.getMessage());
    }

    @Test
    @Order(16)
    void getAllAndPrintEmployeesByID() throws Exception {
        // Retrieve and print all employees sorted by ID
        List<Employee> employees = EmployeeProvider.getAllAndPrintEmployees(collection);
        assertNotNull(employees, "The employee list should not be null.");
        assertEquals(14, employees.size(), "The employee list size should be 10.");
    }

    @Test
    @Order(17)
    void getAllEmployeesWithSearch() throws Exception {
        // Fetch employees with the search term "Doe", skip 0 and limit 5
        List<Employee> employeesWithSearch = EmployeeProvider.getAllEmployeesWithSearch(collection, "Doe", 0, 5);

        // Verify that the correct number of employees are retrieved
        assertNotNull(employeesWithSearch, "The employee list should not be null.");
        assertEquals(4, employeesWithSearch.size(), "The employee list size should be 2.");

        // Print the retrieved employees
        employeesWithSearch.forEach(System.out::println);

        // Fetch employees with the search term "Doe", skip 1 and limit 5
        List<Employee> employeesWithSearchPagination = EmployeeProvider.getAllEmployeesWithSearch(collection, "Doe", 1, 5);

        // Verify that only one employee is retrieved due to pagination
        assertNotNull(employeesWithSearchPagination, "The employee list should not be null.");
        assertEquals(3, employeesWithSearchPagination.size(), "The employee list size should be 1.");

    }

    @Test
    @Order(18)
    void getAllEmployeesByAggregation() throws Exception {
        // Fetch employees with the search term "Doe", skip 0 and limit 5
        List<Employee> employeesWithAggregation = EmployeeProvider.getAllEmployeesByAggregation(collection, "JOHN", 0, 5);

        // Verify that the correct number of employees are retrieved
        assertNotNull(employeesWithAggregation, "The employee list should not be null.");
        assertEquals(2, employeesWithAggregation.size(), "The employee list size should be 2.");

        // Print the retrieved employees
        employeesWithAggregation.forEach(System.out::println);

        // Fetch employees with the search term "Doe", skip 1 and limit 5
        List<Employee> employeesWithAggregationPagination = EmployeeProvider.getAllEmployeesByAggregation(collection, "Doe", 1, 5);

        // Verify that only one employee is retrieved due to pagination
        assertNotNull(employeesWithAggregationPagination, "The employee list should not be null.");
        assertEquals(3, employeesWithAggregationPagination.size(), "The employee list size should be 1.");

        // Print the retrieved employees with pagination
        employeesWithAggregationPagination.forEach(System.out::println);
    }

    @Test
    @Order(19)
    void testGetEmployeeByFacet() throws EmployeeException {
        String searchTerm = "Doe";
        int skip = 0;
        int limit = 5;

        EmployeeFacetResult result = EmployeeProvider.getEmployeeByFacet(collection, searchTerm, skip, limit);

        assertNotNull(result, "The result should not be null.");

        // Verify the total count
        assertEquals(4, result.getTotalCount(), "The total count should be 3.");

        // Verify the employees list
        List<Document> employeesList = result.getEmployees();
        assertNotNull(employeesList, "Employees list should not be null.");
        assertEquals(4, employeesList.size(), "There should be 3 employees listed.");
        assertTrue(employeesList.stream().allMatch(doc -> doc.getString("name").contains("Doe")), "All employees should have the surname 'Doe'.");

        // Print the results
        System.out.println("Total count: " + result.getTotalCount());
        System.out.println("Employees list:");
        for (Document doc : employeesList) {
            System.out.println(doc.toJson());
        }
    }

    @Test
    @Order(20)
    void searchEmployeesByAutocomplete() throws EmployeeException {
        String searchTerm = "John";
        List<Document> result = EmployeeProvider.searchEmployeesByAutocomplete(collection, searchTerm);

        assertNotNull(result, "The result should not be null.");
        assertFalse(result.isEmpty(), "The result should not be empty.");

        System.out.println("Employees list:");
        for (Document doc : result) {
            System.out.println(doc.toJson());
        }

        // Verify the employees list
        assertTrue(result.stream().allMatch(doc -> doc.getString("name").contains("John")), "All employees should have the surname 'John'.");
    }


    @Test
    @Order(21)
    void deleteAllEmployeesByID() throws EmployeeException, Exception {
        // Fetch all employees from the collection
        List<Employee> employees = EmployeeProvider.getAllEmployeesWithSearch(collection, "", 0, 0); // Fetch all employees

        // Delete each employee by ID and verify deletion
        for (Employee employee : employees) {
            boolean isDeleted = EmployeeProvider.deleteEmployeeByID(collection, employee.getEmployeeID());
            assertTrue(isDeleted, "The employee should be deleted.");

            // Verify the employee is deleted
            Employee deletedEmployee = EmployeeProvider.getEmployeeByID(collection, employee.getEmployeeID());
            assertNull(deletedEmployee, "The employee should no longer exist.");
        }

        // Verify that no employees remain in the collection
        List<Employee> remainingEmployees = EmployeeProvider.getAllEmployeesWithSearch(collection, "", 0, 0); // Fetch all employees again
        assertTrue(remainingEmployees.isEmpty(), "There should be no remaining employees in the collection.");
    }

}


