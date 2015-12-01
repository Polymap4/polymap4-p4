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
package org.polymap.p4.style.icon;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFigureLibraryInitializer
        extends AbstractImageLibraryInitializer {

    protected List<String> getNames() {
        return Lists.newArrayList( "Predefined" );
    }


    protected void fillPaths( List<String> names, List<String> paths ) {
        addToImageLibrary( names.get( 0 ), "well_known_shape.lst", "Predefined shapes by SLD", paths );
    }
}
