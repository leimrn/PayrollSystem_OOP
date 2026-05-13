package model;
import java.util.Scanner;

public class RegularEmployee extends Employee {
  
  // Overriding the inputDetails method to handle Probationary status
  @Override
  
  public void inputDetails(Scanner sc) {
    
    // Automatically set the employee type to Regular
    setEmptype("Regular");

    // Collecting user input using the setters from Employee.java
    System.out.print("Enter Name: ");
    setName(sc.nextLine());

    //Error handling for ID number
    while (true) {
        System.out.print("Enter ID: ");
        String input = sc.nextLine();

        try {
            
            int idValue = Integer.parseInt(input); 
            setId(idValue); 
            break;
        }
        catch (NumberFormatException e) {
            System.out.println("Invalid input! Must be a number.");
        }
    }

    System.out.print("Enter Fixed Salary or Base Rate: ");
    setBaseRate(sc.nextDouble());

    sc.nextLine(); 
  }
}
