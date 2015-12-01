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
public class LineStyleTest
        extends AbstractSLDTest {

    @Test
    public void test01_simple_line() throws Exception {
        assertRoundtrip( "2-line/01-simple_line.sld" );
    }


    @Test
    public void test02_line_with_border() throws Exception {
        assertRoundtrip( "2-line/02-line_with_border.sld" );
    }


    @Test
    public void test03_dashed_line() throws Exception {
        assertRoundtrip( "2-line/03-dashed_line.sld" );
    }


    @Test
    public void test04_dash_symbol() throws Exception {
        assertRoundtrip( "2-line/04-dash_symbol.sld" );
    }


    @Test
    public void test05_dash_symbol_and_space() throws Exception {
        assertRoundtrip( "2-line/05-dash_symbol_and_space.sld" );
    }


    @Test
    public void test06_railroad_hatching() throws Exception {
        assertRoundtrip( "2-line/06-railroad_hatching.sld" );
    }


    @Test
    public void test07_line_with_default_label() throws Exception {
        assertRoundtrip( "2-line/07-line_with_default_label.sld" );
    }


    @Test
    public void test08_optimized_label_placement() throws Exception {
        assertRoundtrip( "2-line/08-optimized_label_placement.sld" );
    }


    @Test
    public void test09_optimized_and_styled_label() throws Exception {
        assertRoundtrip( "2-line/09-optimized_and_styled_label.sld" );
    }


    @Test
    public void test10_label_following_line() throws Exception {
        assertRoundtrip( "2-line/10-label_following_line.sld" );
    }
}
