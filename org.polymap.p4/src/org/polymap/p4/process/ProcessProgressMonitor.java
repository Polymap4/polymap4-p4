/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package org.polymap.p4.process;

import java.util.EventObject;
import java.util.Optional;

import org.jgrasstools.gears.libs.monitor.DummyProgressMonitor;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class ProcessProgressMonitor
        extends DummyProgressMonitor
        implements IJGTProgressMonitor {

    private static final Log log = LogFactory.getLog( ProcessProgressMonitor.class );
    
    private BackgroundJob   bgjob;
    
    private String          taskName;

    private String          subTaskName;

    private volatile int    total = UNKNOWN;

    private volatile int    worked;
    
    private Timer           updated = new Timer();
    
    private volatile boolean canceled;

    /** The display of the last {@link #createContents(Composite)}. */
    private Display         display;
    
    
    public ProcessProgressMonitor( BackgroundJob bgjob ) {
        this.bgjob = bgjob;
        taskName = "Start processing";
    }

    /**
     * 
     *
     * @param throttle
     */
    protected void update( boolean throttle ) {
        if (throttle && updated.elapsedTime() < 1000) {
            return;
        }
        updated.start();

        EventManager.instance().publish( new ProgressEvent( bgjob ) );
    }

    /**
     * Percent of work that has been completed so far, or {@link Optional#empty()}
     * if the total amount of work is {@link IJGTProgressMonitor#UNKNOWN}.
     */
    public Optional<Integer> completed() {
        return total != UNKNOWN 
                ? Optional.of( Integer.valueOf( (int)(100f / total * worked) ) )
                : Optional.empty();
    }
    
    public void reset() {
        canceled = false;
        worked = 0;
        subTaskName = null;
    }

    @Override
    public void done() {
        worked = total;
        update( false );
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled( boolean canceled ) {
        this.canceled = canceled;
    }

    @Override
    public void beginTask( String name, int totalWork ) {
        this.total = totalWork;
        setTaskName( name );
    }

    @Override
    public void setTaskName( String name ) {
        assert name != null;
        this.taskName = name;
        update( false );
    }

    @Override
    public void subTask( String name ) {
        this.subTaskName = name;
        update( true );
    }

    @Override
    public void worked( int work ) {
        worked += work;
        update( true );
    }

    
    /**
     * Fired when {@link ProcessProgressMonitor} changes its progression state. Event
     * rate is throttled by {@link ProcessProgressMonitor} to about 1/s.
     */
    public class ProgressEvent
            extends EventObject {
        
        public ProgressEvent( BackgroundJob source ) {
            super( source );
        }

        @Override
        public BackgroundJob getSource() {
            return (BackgroundJob)super.getSource();
        }

        /** 
         * See {@link ProcessProgressMonitor#completed()}. 
         */
        public Optional<Integer> completed() {
            return ProcessProgressMonitor.this.completed();
        }
        
        public String taskName() {
            return taskName;
        }
        
        public Optional<String> subTaskName() {
            return Optional.ofNullable( subTaskName );
        }
    }

}
