import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class RegistrationSystem
{
    // Stores all students
    ArrayList<Student> students;
    // Stores all courses (key = course ID)
    HashMap<String, Course> courses;
    // Lottery pool (temporarily used during course assignment)
    LinkedList<Student> lottery;
}
