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
public class PointStyleTest
        extends AbstractSLDTest {

    @Test
    public void test01_simple_point() throws Exception {
        assertRoundtrip( "1-point/01-simple_point.sld" );
    }


    @Test
    public void test02_simple_point_with_stroke() throws Exception {
        assertRoundtrip( "1-point/02-simple_point_with_stroke.sld" );
    }


    @Test
    public void test03_transparent_triangle() throws Exception {
        assertRoundtrip( "1-point/03-transparent_triangle.sld" );
    }


    @Test
    public void test04_point_with_default_label() throws Exception {
        assertRoundtrip( "1-point/04-point_with_default_label.sld" );
    }


    @Test
    public void test05_point_with_styled_label() throws Exception {
        assertRoundtrip( "1-point/05-point_with_styled_label.sld" );
    }


    @Test
    public void test06_point_with_rotated_label() throws Exception {
        assertRoundtrip( "1-point/06-point_with_rotated_label.sld" );
    }


    @Test
    public void test07_rotated_square() throws Exception {
        assertRoundtrip( "1-point/07-rotated_square.sld" );
    }


    @Test
    public void test08_point_as_graphic() throws Exception {
        assertRoundtrip( "1-point/08-point_as_graphic.sld" );
    }
}
