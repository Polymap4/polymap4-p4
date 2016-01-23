/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.ui;

import org.eclipse.rap.selenium.RapBot;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.thoughtworks.selenium.Selenium;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractRAPTest {
    private final static String URL = "http://127.0.0.1:8080/p4";

    protected WebDriver driver;
    protected Selenium selenium;
    protected RapBot rap;

    static {
      System.setProperty( "webdriver.firefox.bin", "/usr/bin/firefox" );
      // see http://code.google.com/p/selenium/wiki/ChromeDriver
//      System.setProperty( "webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe" );
    }

    @Before
    public void setUp() throws Exception {
      driver = new FirefoxDriver();
//      driver = new ChromeDriver();
      selenium = new com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium( driver, URL );
      driver.manage().window().setSize( new Dimension( 1024, 768 ) );
      rap = new RapBot( driver, selenium );
      rap.loadApplication( URL, false );
    }

    @After
    public void tearDown() throws Exception {
      selenium.stop();
    }
}
