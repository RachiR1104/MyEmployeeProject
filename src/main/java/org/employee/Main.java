package org.employee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class Main {
    public static Employee employee;
    private static String deserializedEmployee;

    public static void main(String[] args) throws JsonProcessingException, EmployeeException {
        // Initialize MongoDB client and database
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("employeeDB");
        MongoCollection<Document> collection = database.getCollection("employees");

        EmployeeProvider employeeProvider = new EmployeeProvider();

        // Create an employee object
        Employee employee = employeeProvider.createEmployeeObject(1, "John Doe", "123 Main St", 1234567890, 12345);

        // Insert the employee into the database
        Employee createdEmployee = EmployeeProvider.createEmployee(collection, employee);

        if (createdEmployee != null) {
            System.out.println("Created Employee in DB: " + createdEmployee);

            // Update the employee
            createdEmployee.setName("John Updated");
            createdEmployee.setAddress("456 Updated St");
            createdEmployee.setContactNumber(987654321);
            createdEmployee.setPincode(54321);

            EmployeeProvider.updateEmployee(collection, createdEmployee);

            // Retrieve the updated document from the collection
            Document retrievedDoc = collection.find(eq("employeeID", createdEmployee.getEmployeeID())).first();
            if (retrievedDoc != null) {
                String retrievedJson = retrievedDoc.toJson();
                System.out.println("Retrieved Document: " + retrievedJson);

                // Deserialize the JSON back to an employee object
                retrievedDoc = collection.find(eq("employeeID", employee.getEmployeeID())).first();
                if (deserializedEmployee == null) {
                    System.err.println("Failed to deserialize the updated employee.");
                    return;
                }
                System.out.println("Deserialized Updated Employee: " + deserializedEmployee);
            } else {
                System.err.println("No document found in the collection.");
            }

            // Retrieve employee by ID
            Employee retrievedEmployee = EmployeeProvider.getEmployeeByID(collection, createdEmployee.getEmployeeID());
            if (retrievedEmployee != null) {
                System.out.println("Employee retrieved by ID: " + retrievedEmployee);
            } else {
                System.err.println("No employee found with the given ID.");
            }
        } else {
            System.err.println("Failed to create the employee in the database.");
        }
        // Delete employee by ID
        boolean isDeleted = EmployeeProvider.deleteEmployeeByID(collection, createdEmployee.getEmployeeID());
        if (isDeleted) {
            System.out.println("Employee deleted successfully.");
        } else {
            System.err.println("Failed to delete the employee.");
        }
        // Close the MongoDB client
        mongoClient.close();
    }

}

