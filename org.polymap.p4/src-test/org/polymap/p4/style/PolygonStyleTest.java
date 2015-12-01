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
public class PolygonStyleTest
        extends AbstractSLDTest {

    @Test
    public void test01_simple_polygon() throws Exception {
        assertRoundtrip( "3-polygon/01-simple_polygon.sld" );
    }


    @Test
    public void test02_simple_polygon_with_stroke() throws Exception {
        assertRoundtrip( "3-polygon/02-simple_polygon_with_stroke.sld" );
    }


    @Test
    public void test03_graphic_fill() throws Exception {
        assertRoundtrip( "3-polygon/03-graphic_fill.sld" );
    }


    @Test
    public void test04_hatching_fill() throws Exception {
        assertRoundtrip( "3-polygon/04-hatching_fill.sld" );
    }


    @Test
    public void test05_polygon_with_default_label() throws Exception {
        assertRoundtrip( "3-polygon/05-polygon_with_default_label.sld" );
    }


    @Test
    public void test06_polygon_with_styled_label() throws Exception {
        assertRoundtrip( "3-polygon/06-polygon_with_styled_label.sld" );
    }


    @Test
    public void test07_label_halo() throws Exception {
        assertRoundtrip( "3-polygon/07-label_halo.sld" );
    }
}
