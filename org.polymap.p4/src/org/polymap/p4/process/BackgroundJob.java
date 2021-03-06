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

import java.util.Collections;
import java.util.EventObject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Throwables;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.p4.project.ProjectRepository;

/**
 * Implementation of a long running background job for data processing.
 * <p/>
 * Provides the {@link #running() static global list} of currently running jobs.
 *
 * @author Falko Bräutigam
 */
class BackgroundJob {
    
    private static Set<BackgroundJob>   jobs = ConcurrentHashMap.newKeySet();
    
    /**
     * The state of a {@link BackgroundJob}. 
     */
    public static enum State {
        /** Job has never been started. */
        NOT_YET_STARTED,
        /** Job is currently running. */
        RUNNING,
        /** Job has completed a run (successfully or not), or was canceled and stopped. */
        ENDED,
        DISPOSED
    }

    /**
     * 
     */
    public static class StateEvent
            extends EventObject {

        public State        newState;
        
        public StateEvent( BackgroundJob source, State newState ) {
            super( source );
            this.newState = newState;
        }

        @Override
        public BackgroundJob getSource() {
            return (BackgroundJob)super.getSource();
        }
    }
    
    /**
     * Immutable list of currently running {@link BackgroundJob} instances.
     */
    public static Set<BackgroundJob> running() {
        return Collections.unmodifiableSet( jobs );
    }

    // instance *******************************************
    
    private String                  layerId;
    
    private ModuleInfo              moduleInfo;

    private Object                  module;
    
    private Job                     job;
    
    private ProcessProgressMonitor  monitor;
    
    private volatile State          state; // = State.NOT_YET_STARTED;
    
    
    public BackgroundJob( ModuleInfo moduleInfo, ILayer layer ) {
        assert moduleInfo != null;
        this.layerId = layer != null ? layer.id() : null;
        this.moduleInfo = moduleInfo;
        this.module = moduleInfo.createModuleInstance();
        this.monitor = new ProcessProgressMonitor( this );
        updateState( State.NOT_YET_STARTED );
    }

    public ModuleInfo moduleInfo() {
        return moduleInfo;
    }

    public Object module() {
        return module;
    }
    
    /**
     * The layer this job was created for. 
     */
    public Optional<ILayer> layer() {
        return Optional.ofNullable( layerId != null 
                ? ProjectRepository.unitOfWork().entity( ILayer.class, layerId ) 
                : null );
    }
    
    public State state() {
        return state;
    }
    
    protected void updateState( State newState ) {
        if (this.state != State.DISPOSED) {
            this.state = newState;
            EventManager.instance().publish( new StateEvent( this, newState ) );
        }
    }
        
    /**
     * The result of the last completed run, or {@link Optional#empty()} if the job
     * has not yet completed a run.
     */
    public Optional<IStatus> status() {
        return job != null ? Optional.ofNullable( job.getResult() ) : Optional.empty();
    }
    
    /**
     * Percent of work that has been completed so far, or {@link Optional#empty()}
     * if the total amount of work is {@link IJGTProgressMonitor#UNKNOWN}.
     * 
     * @see ProcessProgressMonitor#completed()
     */
    public Optional<Integer> completed() {
        return monitor.completed();
    }
    

    public BackgroundJob start( IJobChangeListener listener ) {
        assert state != State.RUNNING;
        monitor.reset();
        job = new Job( "Processing" ) {
            @Override
            protected IStatus run( IProgressMonitor _monitor ) {
                try {
                    moduleInfo.execute( module, monitor );
                    return Status.OK_STATUS;
                }
                catch (Exception e) {
                    if (Throwables.getRootCause( e ) instanceof InterruptedException) {
                        return Status.CANCEL_STATUS;
                    }
                    else {
                        throw Throwables.propagate( e );
                    }
                }
            }
        };
        job.setPriority( Job.BUILD );
        job.addJobChangeListener( listener );
        job.addJobChangeListener( new JobChangeAdapter() {
            @Override
            public void running( IJobChangeEvent ev ) {
                updateState( State.RUNNING );
            }
            @Override
            public void done( IJobChangeEvent ev ) {
                updateState( State.ENDED );
            }
        });
        jobs.add( this );
        
        // the default Polymap executor is unbound
        // ExecutionPlanner.defaultExecutor = Polymap.executorService();
        
        job.schedule();
        return this;
    }
    
    
    public BackgroundJob cancel() {
        if (state == State.RUNNING) {
            job.cancel();  // XXX and interrupt?
            monitor.setCanceled( true );
        }
        return this;
    }
    
    public boolean isCanceled() {
        return job != null && monitor.isCanceled();
    }

    /**
     * Removes this job from the global list of running jobs. 
     */
    public void dispose() {
        jobs.remove( this );
        if (state == State.RUNNING) {
            cancel();
        }
        updateState( State.DISPOSED );
    }

}
