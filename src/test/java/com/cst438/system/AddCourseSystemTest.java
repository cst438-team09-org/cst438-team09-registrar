package com.cst438.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class StudentViewAssignmentGradeSystemTest {
    static final String CHROME_DRIVER_FILE_LOCATION = "/Users/mayraleon/Downloads/chromedriver-mac-arm64-3/chromedriver";
    static final String URL = "http://localhost:5173";   // react dev server

    WebDriver driver;
    Random random = new Random();
    WebDriverWait wait;

    @BeforeEach
    public void setUpDriver() {
        // Set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // Start the driver
        driver = new ChromeDriver(ops);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
    }

    @AfterEach
    public void quit() {
        driver.quit();
    }

    @Test
    public void testInstructorAddAssignmentAndStudentView() {
        // Instructor login
        driver.findElement(By.id("email")).sendKeys("ted@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("ted2025"); // Replace with actual password
        driver.findElement(By.id("loginButton")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement yearElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));

        // Enter year and semester to view sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Fall");
        driver.findElement(By.id("selectTermButton")).click();

        // Select view assignments for section CST599
        driver.findElement(By.xpath("//tr[td[text()='CST599']]/td/a[text()='Assignments']")).click();
        // Generate a random assignment title
        String assignmentTitle = "assignment" + random.nextInt(100000);
        driver.findElement(By.id("assignmentTitle")).sendKeys(assignmentTitle);
        driver.findElement(By.id("dueDate")).sendKeys("2025-12-01");
        driver.findElement(By.id("saveAssignmentButton")).click();

        // Handle the confirmation alert
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']")));
        WebElement yesButton = driver.findElement(
                By.xpath("//div[@class='react-confirm-alert-button-group']/button[@label='Yes']"));
        yesButton.click();

        // Verify that the new assignment shows on the assignment page
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found on the page.");

        // Logout as instructor
        driver.findElement(By.id("logoutButton")).click();

        // Student login
        driver.findElement(By.id("email")).sendKeys("samb@csumb.edu");
        driver.findElement(By.id("password")).sendKeys("sam2025");
        driver.findElement(By.id("loginButton")).click();

        // Navigate to view assignments
        driver.findElement(By.id("assignmentsLink")).click();

        // Verify that the new assignment in course CST599 appears and the assignment score is blank
        assertTrue(driver.getPageSource().contains(assignmentTitle), "Assignment title not found in student's assignments.");
        assertTrue(driver.getPageSource().contains("—"), "Assignment score is not blank.");
    }
}
