/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.data.importer.kml;

import java.util.List;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ImporterFactory;

/**
 */
public class KMLImporterFactory
        implements ImporterFactory {

    @ContextIn
    protected File       file;

    @ContextIn
    protected List<File> files;


    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (isSupported( file )) {
            builder.newImporter( new KMLImporter(), file );
        }
        if (files != null) {
            for (File currentFile : files) {
                if (isSupported( currentFile )) {
                    builder.newImporter( new KMLImporter(), currentFile );
                }
            }
        }
    }


    private boolean isSupported( File f ) {
        return f != null && ("kml".equalsIgnoreCase( FilenameUtils.getExtension( f.getName() ) ));
    }
}