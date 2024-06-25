package org.employee;

import org.bson.Document;

import java.util.List;

public class EmployeeFacetResult {
    private long totalCount;
    private Object employees;

    public EmployeeFacetResult(long totalCount, List<Document> employees) {
        this.totalCount = totalCount;
        this.employees = employees;
    }
    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public List<Document> getEmployees() {
        return (List<Document>) employees;
    }

    public void setEmployees(List<Document> employees) {
        this.employees = employees;
    }
}
