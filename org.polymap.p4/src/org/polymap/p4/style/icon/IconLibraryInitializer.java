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
public class IconLibraryInitializer
        extends AbstractImageLibraryInitializer {

    protected List<String> getNames() {
        return Lists.newArrayList( "Simple icon places", "Pin of maps", "Maki icons" );
    }


    protected void fillPaths( List<String> names, List<String> paths ) {
        addToImageLibrary(
                names.get( 0 ),
                "simple-icon-places.lst",
                "<a href=\"http://www.flaticon.com/packs/simpleicon-places/\">SimpleIcons Places</a>, designed by <a href=\"http://freepik.com\">freepik</a>",
                paths );
        addToImageLibrary(
                names.get( 1 ),
                "pin-of-maps.lst",
                "<a href=\"http://www.flaticon.com/packs/pins-of-maps/\">Pin of maps</a>, designed by <a href=\"http://freepik.com\">freepik</a>",
                paths );
        addToImageLibrary(
                names.get( 2 ),
                "mapzone_maki.lst",
                "<a href=\"https://github.com/mapbox/maki\">Maki icons</a>, designed by <a href=\"http://mapbox.com/\">Mapbox</a>",
                paths );
    }
}
