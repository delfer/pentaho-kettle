/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebSpoonTest {
  private WebDriver driver;
  private Actions actions;
  private String baseUrl;
  private WebElement element;
  private WebDriverWait wait;

  @Before
  public void setUp() throws Exception {
    boolean isHeadless = Boolean.parseBoolean( System.getProperty( "java.awt.headless", "true" ) );
    ChromeOptions options = new ChromeOptions();
    if ( isHeadless ) {
      options.addArguments( "headless" );
    }
    options.addArguments( "--window-size=1280,800" );
    driver = new ChromeDriver( options );
    actions = new Actions( driver );
    wait = new WebDriverWait( driver, 5 );
    baseUrl = System.getProperty( "test.baseurl", "http://localhost:8080/spoon" );
    driver.get( baseUrl );
    driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

    // Login with username and password
    if ( driver.findElements( By.xpath( "//input[@name = 'username']" ) ).size() != 0 ) {
      driver.findElement( By.xpath( "//input[@name = 'username']" ) ).sendKeys( "user" );
      driver.findElement( By.xpath( "//input[@name = 'password']" ) ).sendKeys( "password" );
      clickElement( "//input[@name = 'submit']" );
    }
  }

  @Test
  public void testAppLoading() throws Exception {
    assertEquals( driver.getTitle(), "Spoon" );
  }

  @Test
  public void testNewTransformation() throws Exception {
    // Create a new transformation
    clickElement( "//div[text() = 'File']" );
    clickElement( "//div[text() = 'New']" );
    clickElement( "//div[text() = 'Transformation']" );

    // Drag & drop a step
    clickElement( "//div[text() = 'Input']" );
    element = driver.findElement( By.xpath( "//div[text() = 'Generate Rows']" ) );
    actions.clickAndHold( element ).moveByOffset( 300, 0 ).release().build().perform();

    // Open a step dialog
    clickElement( "//div[@test-id = 'tree_exploreSolution']" );
    clickElement( "//div[@test-id = 'tree_expandAll']" );
    doubleClickElement( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Generate Rows']" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Never stop generating rows']" ) ).size() );
  }

  /*
   * testModifiedJavaScriptValue1 and 2 collectively demonstrate multi-session use.
   */
  @Test
  public void testModifiedJavaScriptValue1() throws Exception {
    createNewTrans();
    drawStep( "Modified Java Script Value" );
    openDialog( "Modified Java Script Value" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Script Values / Mod']" ) ).size() );
  }

  @Test
  public void testModifiedJavaScriptValue2() throws Exception {
    createNewTrans();
    drawStep( "Modified Java Script Value" );
    openDialog( "Modified Java Script Value" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Script Values / Mod']" ) ).size() );
  }

  @Test
  public void testOpenSaveMenus() throws Exception {
    clickElement( "//div[text() = 'File']" );
    assertTrue( isMenuItemDisabled( "//div[text() = 'Open...']" ) );
    clickElement( "//div[text() = 'File']" ); // Close the menu

    createNewTrans();
    clickElement( "//div[text() = 'File']" );
    assertTrue( isMenuItemDisabled( "//div[text() = 'Save']" ) );
    assertFalse( isMenuItemDisabled( "//div[text() = 'Save as (VFS)...']" ) );
  }

  @Test
  public void testDatabaseConnectionDialog() throws Exception {
    // Create a new transformation
    clickElement( "//div[text() = 'File']" );
    clickElement( "//div[text() = 'New']" );
    clickElement( "//div[text() = 'Transformation']" );

    // Filter a step
    driver.findElement( By.xpath( "//input[@test-id = 'selectionFilter']" ) ).sendKeys( "table" );

    // Draw a step
    doubleClickElement( "//div[text() = 'Table input']" );

    // Open a step dialog
    clickElement( "//div[@test-id = 'tree_exploreSolution']" );
    clickElement( "//div[@test-id = 'tree_expandAll']" );
    doubleClickElement( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Table input']" );

    /* TODO
     * Cancel button does not become clickable unless thread.sleep and window.setSize.
     * The wait duration might depend on an environment.
     */
    clickElement( "//div[text() = 'Edit...']" );
    Thread.sleep( 1000 );
    clickElement( "//div[text() = 'OK']" );
    Thread.sleep( 1000 );
    clickElement( "//div[text() = 'Edit...']" );
    Thread.sleep( 1000 );
    clickElement( "//div[text() = 'OK']" );
    Thread.sleep( 1000 );
    assertEquals( "5", driver.switchTo().activeElement().getAttribute( "tabindex" ) );
  }

  @Test
  public void testContextMenu() throws Exception {
    // Create a new transformation
    createNewTrans();
    drawStep( "Table input" );

    // Open the View tab
    clickElement( "//div[@test-id = 'tree_exploreSolution']" );
    clickElement( "//div[@test-id = 'tree_expandAll']" );

    // Right-click on a step
    rightClickElement( "//div[@test-id = 'tree_Steps']/../..//div[text() = 'Table input']" );

    assertEquals( 1, driver.findElements( By.xpath( "//div[text() = 'Duplicate']" ) ).size() );
  }

  private void createNewTrans() {
    // Create a new transformation
    clickElement( "//div[text() = 'File']" );
    clickElement( "//div[text() = 'New']" );
    clickElement( "//div[text() = 'Transformation']" );
    wait.until( ExpectedConditions.presenceOfElementLocated( By.xpath( "//div[text() = 'Transformation 1']" ) ) );
  }

  private void drawStep( String stepName ) throws InterruptedException {
    // Filter a step
    element = wait.until( ExpectedConditions.visibilityOfElementLocated( By.xpath( "//input[@test-id = 'selectionFilter']" ) ) );
    element.sendKeys( stepName );

    // Draw a step
    doubleClickElement( "//div[text() = '" + stepName + "']" );
  }

  private void openDialog( String stepName ) {
    // Open a step dialog
    clickElement( "//div[@test-id = 'tree_exploreSolution']" );
    clickElement( "//div[@test-id = 'tree_expandAll']" );
    doubleClickElement( "//div[@test-id = 'tree_Steps']/../..//div[text() = '" + stepName + "']" );
  }

  private void clickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    element.click();
  }

  private void doubleClickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    actions.click( element ).click( element ).build().perform();
  }

  private void rightClickElement( String xpath ) {
    element = wait.until( ExpectedConditions.elementToBeClickable( By.xpath( xpath ) ) );
    actions.contextClick( element ).build().perform();
  }

  private boolean isMenuItemDisabled( String xpath ) {
    /*
     *  Determine if a menu item is grayed out (=disabled)
     *  ExpectedConditions.elementToBeClickable does not work here because it is clickable.
     */
    String color = driver.findElement( By.xpath( xpath ) ).getCssValue("color");
    return color.equals( "rgba(189, 189, 189, 1)" );
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
  }
}
