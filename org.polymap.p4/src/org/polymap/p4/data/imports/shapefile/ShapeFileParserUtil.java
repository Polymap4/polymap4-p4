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
package org.polymap.p4.data.imports.shapefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.TimeZone;
import java.util.function.Function;

import org.geotools.data.PrjFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFileParserUtil {

    public static <T> T parseShapeFile( ShpFiles shpFilesPart, Function<ShapefileReader,T> selector ) {
        ShapefileReader shapefileReader = null;
        try {
            shapefileReader = new ShapefileReader( shpFilesPart, false, false, new GeometryFactory() );
            return selector.apply( shapefileReader );
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (shapefileReader != null) {
                try {
                    shapefileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static <T> T parseDbaseFile( ShpFiles shpFilesPart, String encoding, Function<DbaseFileReader,T> selector ) {
        DbaseFileReader dbaseFileReader = null;
        try {
            dbaseFileReader = new DbaseFileReader( shpFilesPart, false, Charset.forName( encoding ),
                    TimeZone.getDefault() );
            return selector.apply( dbaseFileReader );
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (dbaseFileReader != null) {
                try {
                    dbaseFileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static <T> T parsePrjFile( File prjFile, Function<PrjFileReader,T> selector ) {
        PrjFileReader prjFileReader = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream( prjFile );
            FileChannel channel = in.getChannel();
            prjFileReader = new PrjFileReader( channel );
            return selector.apply( prjFileReader );
        }
        catch (IOException | FactoryException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (prjFileReader != null) {
                try {
                    prjFileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
