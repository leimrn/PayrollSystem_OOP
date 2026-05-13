package model;

import java.util.Scanner;

public class Employee {
  private String name;
  private int id;
  private String emptype;
  private double baserate;

  //Getters allows to read the private data
  public String getName()
  {return name;
  }
  public int getId() {
    return id;
  }
  public String getEmptype() {
    return emptype;
  }
  public double getBaseRate() {
    return baserate;
  }
  // Paki pangalan exactly 'getBaseRate'!!!!!!!

  // Setters allow the program to update the private data
  public void setName(String name) { 
    this.name = name;
  }
  public void setId(int id) { 
    this.id = id; 
  }
  public void setEmptype(String emptype) { 
    this.emptype = emptype; 
  }
  public void setBaseRate(double baserate) { 
    this.baserate = baserate; 
  }


  // This method will be used by subclasses to handle specific user inputs
  public void inputDetails(Scanner sc) {
    //subclasses inputted information
  }

  //Prints the basic employee information
  public void display() {
    System.out.println("\n--- Employee Record ---");
    System.out.println("ID: " + id);
    System.out.println("Name: " + name);
    System.out.println("Type: " + emptype);
    System.out.println("Base Salary: " + baserate);
  }
}
