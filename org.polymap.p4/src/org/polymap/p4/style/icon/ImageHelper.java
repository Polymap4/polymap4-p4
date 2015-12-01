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
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.polymap.core.runtime.Callback;
import org.polymap.p4.P4Plugin;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper.SvgConfiguration;
import org.polymap.rhei.batik.engine.svg.ImageConfiguration;
import org.polymap.rhei.batik.engine.svg.Scale;
import org.polymap.rhei.field.ImageDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ImageHelper {

    public ImageDescription createImageDescription( final String imagePath ) {
        Consumer<Triple<Integer,Display,Callback<ImageDescriptor>>> calc = (
                Triple<Integer,Display,Callback<ImageDescriptor>> triple ) -> {
            SvgImageRegistryHelper registry = P4Plugin.images();
            registry.svgImageDescriptor( imagePath, getConfigName( registry, triple.getLeft() ), triple.getMiddle(),
                    triple.getRight() );
        };
        return new ImageDescription().localURL.put( imagePath ).imageDescriptorCalculator.put( calc );
    }


    public void createImageDescriptorsCalculator( final List<String> imagePaths ) {
        Consumer<Triple<Integer,Display,Callback<Map<String,ImageDescriptor>>>> calc = (
                Triple<Integer,Display,Callback<Map<String,ImageDescriptor>>> triple ) -> {
            SvgImageRegistryHelper registry = P4Plugin.images();
            registry.svgImageDescriptors( imagePaths, getConfigName( registry, triple.getLeft() ), triple.getMiddle(),
                    triple.getRight() );
        };
        ImageDescription.setImageDescriptorsCalculator( calc );
    }


    private String getConfigName( SvgImageRegistryHelper registry, Integer size ) {
        size = ((size / 8) + 1) * 8;
        final int newSize = size > 64 ? 128 : size;

        String name = "UNCHANGED" + newSize;
        if (!registry.existsConfig( name )) {
            registry.putConfig( name, new SvgConfiguration() {

                @Override
                protected String colorScheme() {
                    return "UNCHANGED";
                }


                @Override
                protected Scale scale() {
                    return Scale.getAsScale( Integer.valueOf( newSize ) );
                }


                @Override
                protected ImageConfiguration imageConfiguration() {
                    ImageConfiguration imageConfig = new ImageConfiguration();
                    imageConfig.setName( colorScheme() );
                    return imageConfig;
                }
            } );
        }
        return name;
    }

}
