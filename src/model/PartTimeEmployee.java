package model;
import java.util.Scanner;


public class PartTimeEmployee extends Employee {

  // Overriding the inputDetails method to handle Part-Time status
  @Override

  public void inputDetails(Scanner sc) {
    
    // Automatically set the employee type to Part-Time
    setEmptype("Part-Time");

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

      System.out.print("Enter Hourly Rate (e.g., 150): ₱");
      this.setBaseRate(sc.nextDouble());

    sc.nextLine();
  }
}
