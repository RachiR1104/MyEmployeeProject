package org.employee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class EmployeeProvider {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9 _]");

    public EmployeeProvider() {
    }

    //method to createEmployeeObject
    public Employee createEmployeeObject(int employeeID, String name, String address, int contactNumber, int pincode) {
        Employee employee = new Employee();
        employee.setEmployeeID(employeeID);
        employee.setName(name);
        employee.setAddress(address);
        employee.setContactNumber(contactNumber);
        employee.setPincode(pincode);
        return employee;
    }

    // Method to create an employee in the database with exception handling for duplicate IDs and name validation
    public static Employee createEmployee(MongoCollection<Document> collection, Employee employee) throws JsonProcessingException, EmployeeException {
        if (employee == null) {
            throw new EmployeeException("Employee object is null");
        }

        // Check mandatory fields using Apache Commons
        if (employee.getEmployeeID() <= 0) {
            throw new EmployeeException("Employee ID is mandatory and must be greater than 0");
        }
        if (StringUtils.isBlank(employee.getName())) {
            throw new EmployeeException("Employee name is mandatory");
        }
        if (SPECIAL_CHAR_PATTERN.matcher(employee.getName()).find()) {
            throw new EmployeeException("Employee name must not contain special characters except spaces and underscores");
        }
        if (StringUtils.isBlank(employee.getAddress())) {
            throw new EmployeeException("Employee address is mandatory");
        }

        // Serialize the employee to JSON
        Document doc = Document.parse(objectMapper.writeValueAsString(employee));
        try {
            collection.insertOne(doc);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) { // Duplicate key error code
                throw new EmployeeException("Employee with ID " + employee.getEmployeeID() + " already exists.");
            }
            throw e; // Re-throw if it's a different error
        }

        // Retrieve the document from the collection
        Document retrievedDoc = collection.find(eq("employeeID", employee.getEmployeeID())).first();
        if (retrievedDoc != null) {
            // Deserialize the JSON back to an employee object
            Employee deserializedEmployee = objectMapper.readValue(retrievedDoc.toJson(), Employee.class);
            if (deserializedEmployee == null) {
                System.err.println("Failed to deserialize the employee.");
                return null;
            }
            return deserializedEmployee;
        } else {
            System.err.println("No document found in the collection.");
            return null;
        }
    }


    // Method to update an employee in the database
    public static Employee updateEmployee(MongoCollection<Document> collection, Employee updatedEmployee) throws JsonProcessingException, EmployeeException {
        if (updatedEmployee == null) {
            throw new EmployeeException("Updated employee object is null");
        }

        // Check mandatory fields using Apache Commons
        if (updatedEmployee.getEmployeeID() <= 0) {
            throw new EmployeeException("Employee ID is mandatory and must be greater than 0");
        }
        if (StringUtils.isBlank(updatedEmployee.getName())) {
            throw new EmployeeException("Employee name is mandatory");
        }
        if (SPECIAL_CHAR_PATTERN.matcher(updatedEmployee.getName()).find()) {
            throw new EmployeeException("Employee name must not contain special characters except space and underscore");
        }
        if (StringUtils.isBlank(updatedEmployee.getAddress())) {
            throw new EmployeeException("Employee address is mandatory");
        }

        // Check if employeeID exists and get the current employee document
        Document existingEmployeeDoc = collection.find(eq("employeeID", updatedEmployee.getEmployeeID())).first();
        if (existingEmployeeDoc == null) {
            throw new EmployeeException("No employee found with ID: " + updatedEmployee.getEmployeeID());
        }

        // Deserialize the existing employee to compare names
        Employee existingEmployee = objectMapper.readValue(existingEmployeeDoc.toJson(), Employee.class);
        if (!existingEmployee.getName().equals(updatedEmployee.getName())) {
            throw new EmployeeException("Name field cannot be changed");
        }

        // Create the update document without the name field
        Bson updateOperation = combine(
                set("address", updatedEmployee.getAddress()),
                set("contactNumber", updatedEmployee.getContactNumber()),
                set("pincode", updatedEmployee.getPincode())
        );

        // Update the employee in the collection
        long updateCount = collection.updateOne(eq("employeeID", updatedEmployee.getEmployeeID()), updateOperation).getModifiedCount();
        if (updateCount > 0) {
            System.out.println("Updated Employee with ID: " + updatedEmployee.getEmployeeID());
        } else {
            System.err.println("No employee found with ID: " + updatedEmployee.getEmployeeID() + " to update.");
        }

        // Retrieve the updated document from the collection
        Document retrievedDoc = collection.find(eq("employeeID", updatedEmployee.getEmployeeID())).first();
        if (updateCount > 0 && retrievedDoc != null) {
            // Deserialize the JSON back to an employee object
            Employee deserializedEmployee = objectMapper.readValue(retrievedDoc.toJson(), Employee.class);
            if (deserializedEmployee == null) {
                System.err.println("Failed to deserialize the employee.");
                return null;
            }
            return deserializedEmployee;
        }
        return null;
    }

    public static Employee getEmployeeByID(MongoCollection<Document> collection, int employeeID) throws EmployeeException {
        if (employeeID <= 0) {
            throw new EmployeeException("Employee ID is mandatory and must be greater than 0");
        }

        try {
            Document retrievedDoc = collection.find(eq("employeeID", employeeID)).first();
            if (retrievedDoc != null) {
                // Deserialize the JSON back to an employee object
                Employee deserializedEmployee = objectMapper.readValue(retrievedDoc.toJson(), Employee.class);
                if (deserializedEmployee != null) {
                    return deserializedEmployee;
                } else {
                    throw new EmployeeException("Failed to deserialize the employee.");
                }
            } else {
                System.err.println("No document found in the collection with Employee ID: " + employeeID);
                return null;
            }
        } catch (JsonProcessingException e) {
            System.err.println("JSON processing error: " + e.getMessage());
            throw new EmployeeException("Error processing JSON");
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            throw new EmployeeException("An unexpected error occurred");
        }
    }

    public static boolean deleteEmployeeByID(MongoCollection<Document> collection, int employeeID) throws EmployeeException {
        if (employeeID <= 0) {
            throw new EmployeeException("Employee ID is mandatory and must be greater than 0");
        }
        long deleteCount = collection.deleteOne(eq("employeeID", employeeID)).getDeletedCount();
        if (deleteCount > 0) {
            System.out.println("Deleted Employee with ID: " + employeeID);
            return true;
        } else {
            System.err.println("No employee found with ID: " + employeeID + " to delete.");
            return false;
        }
    }

    public static List<Employee> getAllAndPrintEmployees(MongoCollection<Document> collection) throws Exception {
        List<Employee> employees = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().sort(ascending("employeeID")).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Employee employee = objectMapper.readValue(doc.toJson(), Employee.class);
                employees.add(employee);
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            throw new Exception(e);
        }

        // Print all employees
        for (Employee employee : employees) {
            System.out.println(employee);
        }

        return employees;
    }

    public static List<Employee> getAllEmployeesWithSearch(MongoCollection<Document> collection, String searchTerm, int skip, int limit) throws Exception {
        List<Employee> employees = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);

        try (MongoCursor<Document> cursor = collection.find(regex("name", pattern))
                .skip(skip)
                .limit(limit)
                .sort(ascending("employeeID"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Employee employee = objectMapper.readValue(doc.toJson(), Employee.class);
                employees.add(employee);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving employees: " + e.getMessage());
            throw new Exception("Error processing employees", e);
        }

        // Print all employees
        for (Employee employee : employees) {
            System.out.println(employee);
        }

        return employees;
    }

    public static EmployeeFacetResult getEmployeeByFacet(MongoCollection<Document> collection, String searchTerm, int skip, int limit) throws EmployeeException {
        List<? extends Bson> pipeline = Arrays.asList(
                new Document()
                        .append("$match", new Document()
                                .append("name", new BsonRegularExpression(searchTerm, "i"))
                        ),
                new Document()
                        .append("$facet", new Document()
                                .append("totalCount", Arrays.asList(
                                        new Document().append("$count", "count")
                                ))
                                .append("paginatedResults", Arrays.asList(
                                        new Document().append("$skip", skip),
                                        new Document().append("$limit", limit)
                                ))
                        )
        );

        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            if (cursor.hasNext()) {
                Document result = cursor.next();

                // Retrieve the total count
                List<Document> totalCountDocs = (List<Document>) result.get("totalCount");
                long totalCount = totalCountDocs.isEmpty() ? 0 : totalCountDocs.get(0).getInteger("count");

                // Retrieve the paginated results
                List<Document> employeesList = (List<Document>) result.get("paginatedResults");

                return new EmployeeFacetResult(totalCount, employeesList);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving employees: " + e.getMessage());
            throw new EmployeeException("Error processing employees", e);
        }

        return new EmployeeFacetResult(0, new ArrayList<>());
    }


    public static List<Document> searchEmployeesByAutocomplete(MongoCollection<Document> collection, String searchTerm) throws EmployeeException {
        List<Bson> pipeline = Arrays.asList(
                new Document()
                        .append("$search", new Document()
                                .append("autocomplete", new Document()
                                        .append("query", searchTerm)
                                        .append("path", "name")
                                        .append("fuzzy", new Document()
                                                .append("maxEdits", 1)
                                                .append("prefixLength", 2)
                                                .append("maxExpansions", 50)
                                        )
                                )
                                .append("index", "autocomplete_index")
                        ),
                new Document()
                        .append("$limit", 10)
        );

        List<Document> results = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                results.add(cursor.next());
            }
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving employees: " + e.getMessage());
            throw new EmployeeException("Error processing employees", e);
        }

        return results;
    }


    public static List<Employee> getAllEmployeesByAggregation(MongoCollection<Document> collection, String searchTerm, int skip, int limit) throws Exception {
        List<Employee> employees = new ArrayList<>();
        Pattern pattern = Pattern.compile(searchTerm, Pattern.CASE_INSENSITIVE);
        List<Bson> pipeline = new ArrayList<>();

        // Match stage to filter documents by search term
        if (searchTerm != null && !searchTerm.isEmpty()) {
            pipeline.add(Aggregates.match(regex("name", pattern)));
        }

        // Sort stage
        pipeline.add(Aggregates.sort(ascending("employeeID")));

        // Skip stage for pagination
        if (skip > 0) {
            pipeline.add(Aggregates.skip(skip));
        }

        // Limit stage for pagination
        if (limit > 0) {
            pipeline.add(Aggregates.limit(limit));
        }

        try (MongoCursor<Document> cursor = collection.aggregate(pipeline).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Employee employee = objectMapper.readValue(doc.toJson(), Employee.class);
                employees.add(employee);
            }
        } catch (Exception e) {
            System.err.println("An error occurred while retrieving employees: " + e.getMessage());
            throw new Exception("Error processing employees", e);
        }

        // Print all employees
        for (Employee employee : employees) {
            System.out.println(employee);
        }

        return employees;
    }


}



