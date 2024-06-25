package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MongoDBOperations {
    public static void main(String[] args) {
        //Connecting to the local server
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("testdb");
        MongoCollection<Document> collection = database.getCollection("students");

        // Create multiple students
        List<Document> students = Arrays.asList(
                new Document("name", "John Doe").append("age", 21).append("major", "Computer Science"),
                new Document("name", "Jane Smith").append("age", 22).append("major", "Biology"),
                new Document("name", "Mike Brown").append("age", 20).append("major", "Mathematics"),
                new Document("name", "Emily White").append("age", 23).append("major", "Physics"),
                new Document("name", "David Green").append("age", 21).append("major", "Chemistry"),
                new Document("name", "Sophia Black").append("age", 22).append("major", "Engineering"),
                new Document("name", "Liam Blue").append("age", 24).append("major", "Literature"),
                new Document("name", "Olivia Purple").append("age", 20).append("major", "History"),
                new Document("name", "Noah Red").append("age", 23).append("major", "Geography"),
                new Document("name", "Ava Yellow").append("age", 21).append("major", "Philosophy"),
                new Document("name", "Isabella Pink").append("age", 22).append("major", "Sociology"),
                new Document("name", "James Orange").append("age", 20).append("major", "Psychology"),
                new Document("name", "Alexander Grey").append("age", 23).append("major", "Economics"),
                new Document("name", "Mia Brown").append("age", 21).append("major", "Business"),
                new Document("name", "Ethan Cyan").append("age", 22).append("major", "Political Science")
        );

        // Insert multiple students
        collection.insertMany(students);
        System.out.println("Students inserted");
        //Fetch the list of documents from the db
        FindIterable<Document> iterobj = collection.find();
        //print all the documents in the collection
        Iterator itr = iterobj.iterator();
        while (itr.hasNext()) {
            System.out.println(itr.next());
        }

        //updatingonedocument
        collection.updateOne(Filters.eq("name", "Emily White"), Updates.set("name", "Emily Park"));
        System.out.println("Successfully updated " + " the document");
        //deleting a record from the document
        collection.deleteOne(Filters.eq("name", "John Doe"));
        System.out.println("Successfully deleted " + " the user");
        //drop the entire collection
        //collectiojn.drop();
        //update the age of a student
        collection.updateOne(Filters.eq("name", "Jane Smith"), new Document("$set", new Document("age", 18)));

        mongoClient.close();
    }
}


