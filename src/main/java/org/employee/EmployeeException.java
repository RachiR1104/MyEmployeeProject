package org.employee;

public class EmployeeException extends Throwable {
    public EmployeeException(String message) {
        super(message);

    }

    public EmployeeException(String error_processing_employees, Exception e) {
    }
}
