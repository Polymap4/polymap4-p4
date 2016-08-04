/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.archive;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.Mandatory;

/**
 * Copy files into a (temporarey) directory. Handles *.zip, *.tar, *.gz. Flattens the
 * file hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArchiveReader
        extends Configurable {

    private static Log log = LogFactory.getLog( ArchiveReader.class );
    
    /** Defaults to a automatically created temp dir. */
    @Mandatory
    public Config2<ArchiveReader,File>      targetDir;
    
    @Mandatory
    @DefaultBoolean(false)
    public Config2<ArchiveReader,Boolean>   overwrite;
    
    /** Charset for ZIP. Defaults to UTF8. */
    @Mandatory
    public Config2<ArchiveReader,Charset>   charset;
    
    private IProgressMonitor                monitor;
    
    private List<File>                      results = new ArrayList();
    
    
    public ArchiveReader() {
        charset.set( Charset.forName( "UTF8" ) );
        try {
            targetDir.set( Files.createTempDirectory( "P4-" ).toFile() );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    

    public boolean canHandle( File f, @SuppressWarnings("hiding") IProgressMonitor monitor ) {
        String ext = FilenameUtils.getExtension( f.getName() ).toLowerCase();
        return ext.equals( "zip" ) || ext.equals( "tar" ) || ext.equals( "gz" ) || ext.equals( "kmz" );
        // XXX check content / magic number if extension failed
    }
    
    
    /**
     * 
     *
     * @return List of read files.
     * @throws RuntimeException
     */
    public List<File> run( File f, @SuppressWarnings("hiding") IProgressMonitor monitor ) throws Exception {
        this.monitor = monitor;
        try (
            InputStream in = new BufferedInputStream(  new FileInputStream( f ) ); 
        ){
            handle( f.getName(), null, in );
            return results;
        }
    }


    protected File targetDir() {
        return targetDir.get();
    }


    protected void handle( String name, String contentType, InputStream in ) throws Exception {
        if (monitor.isCanceled()) {
            return;
        }
        contentType = contentType == null ? "" : contentType;
        if (name.toLowerCase().endsWith( ".zip" ) || name.toLowerCase().endsWith( ".kmz" ) || contentType.equalsIgnoreCase( "application/zip" )) {
            handleZip( name, in );
        }
        else if (name.toLowerCase().endsWith( ".tar" ) || contentType.equalsIgnoreCase( "application/tar" )) {
            handleTar( name, in );
        }
        else if (name.toLowerCase().endsWith( "gz" ) || name.toLowerCase().endsWith( "gzip" ) 
                || contentType.equalsIgnoreCase( "application/gzip" )) {
            handleGzip( name, in );
        }
        else {
            handleFile( name, in );
        }
    }
    
    
    protected void handleGzip( String name, InputStream in ) throws Exception {
        log.info( "    GZIP: " + name );
        try (GZIPInputStream gzip = new GZIPInputStream( in )) {
            String nextName = null;
            if (name.toLowerCase().endsWith( ".gz" )) {
                nextName = name.substring( 0, name.length() - 3 );
            }
            else if (name.toLowerCase().endsWith( ".tgz" )) {
                nextName = name.substring( 0, name.length() - 3 ) + "tar";
            }
            else {
                nextName = name.substring( 0, name.length() - 2 );            
            }
            handle( nextName, null, gzip );
        }
    }


    protected void handleFile( String name, InputStream in ) throws Exception {
        log.info( "    FILE: " + name );
        File target = new File( targetDir(), FilenameUtils.getName( name ) );
        
        if (!overwrite.get() && target.exists()) {
            throw new RuntimeException( "File already exists: " + target );
        }
        try (
            OutputStream out = new FileOutputStream( target );
        ){
            IOUtils.copy( in, out );
        }
        results.add( target );
    }
    
    
    protected void handleZip( String name, InputStream in ) throws Exception {
        log.info( "    ZIP: " + name );
        try {
            ZipInputStream zip = new ZipInputStream( in, charset.get() );
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    handle( entry.getName(), null, zip );
                }
            }
        }
        catch (Exception e) {
            if (e instanceof IllegalArgumentException || "MALFORMED".equals( e.getMessage() )) {
                throw new IOException( "Wrong charset: " + charset.get().displayName(), e );
            }
            else {
                throw e;
            }
        }
    }


    protected void handleTar( String name, InputStream in ) throws Exception {
        log.info( "    TAR: " + name );
        try (
            TarArchiveInputStream tar = new TarArchiveInputStream( in, charset.get().name() )
        ){
            ArchiveEntry entry = null;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                }
                else {
                    handle( entry.getName(), null, tar );
                }
            }
        }
    }

}
