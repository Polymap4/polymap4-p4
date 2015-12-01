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
package org.polymap.p4.style;

import org.junit.Test;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ZoomBasedStyleTest
        extends AbstractSLDTest {

    @Test
    public void test09_zoom_based_point() throws Exception {
        assertRoundtrip( "1-point/09-zoom_based_point.sld" );
    }


    @Test
    public void test11_zoom_based_line() throws Exception {
        assertRoundtrip( "2-line/11-zoom_based_line.sld" );
    }


    @Test
    public void test08_zoom_based_polygon() throws Exception {
        assertRoundtrip( "3-polygon/08-zoom_based_polygon.sld" );
    }
}
