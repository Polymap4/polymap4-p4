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

import static org.eclipse.rap.selenium.xpath.XPath.any;
import static org.eclipse.rap.selenium.xpath.XPath.byId;

import org.junit.Test;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImportPanelTest
        extends AbstractRAPTest {

    @Test
    public void test() {
        rap.waitForAppear(any().textElementContaining( "Import" ) );
        rap.click( any().textElementContaining( "Import" ) );
        rap.waitForAppear(byId("w23")); // Import-Panel-Title
    }
}
