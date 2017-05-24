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

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.polymap.core.runtime.event.TypeEventFilter.isType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.data.process.FieldInfo;
import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.data.process.ui.FieldIO;
import org.polymap.core.data.process.ui.FieldViewer;
import org.polymap.core.data.process.ui.FieldViewerSite;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.ColumnDataFactory;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.DefaultToolkit;
import org.polymap.rhei.batik.toolkit.IPanelSection;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.process.BackgroundJob.State;
import org.polymap.p4.process.ProcessProgressMonitor.ProgressEvent;

/**
 * Provides UI for a given {@link BackgroundJob}. 
 *
 * @author Falko Bräutigam
 */
public class ProcessModulePanel
        extends P4Panel {

    private static final Log log = LogFactory.getLog( ProcessModulePanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "processModule" );

    static {
        FieldIO.ALL.add( LayerRasterSupplier.class );
        FieldIO.ALL.add( LayerRasterConsumer.class );
        FieldIO.ALL.add( 0, CoordinateSupplier.class );  // higher prio than Number
        FieldIO.ALL.add( BoundingBoxSupplier.class );
    }

    // instance *******************************************
    
    /** Inbound: */
    @Mandatory
    @Scope( P4Plugin.Scope )
    private Context<BackgroundJob>  bgjob;
    
    /** {@link BackgroundJob#moduleInfo()} of {@link #bgjob} */
    private ModuleInfo              moduleInfo;

    private JGTModel                module;

    private ILayer                  layer;

    private IPanelSection           outputSection;

    private IPanelSection           inputSection;

    private Composite               buttons;
    
    private Button                  startBtn;

    private Composite               parent;
    
    private List<FieldViewer>       inputFields = new ArrayList();

    private Label                   progress;

    
    @Override
    public void init() {
        super.init();
        moduleInfo = bgjob.get().moduleInfo();
        module = bgjob.get().module();
        layer = bgjob.get().layer().orElse( null );
    }

    
    @Override
    public void dispose() {
//        if (!bgjob.get().isRunning()) {
//            bgjob.get().dispose();
//        }
    }


    @Override
    public void createContents( @SuppressWarnings( "hiding" ) Composite parent ) {
        this.parent = parent;
        site().title.set( bgjob.get().moduleInfo().title() );
        
        parent.setLayout( FormLayoutFactory.defaults().spacing( 8 ).margins( 2, 8 ).create() );
        
        // input
        inputSection = createInputSection();
        FormDataFactory.on( inputSection.getControl() ).fill().noBottom().width( 500 );

        // buttons
        buttons = createButtonsSection();
        FormDataFactory.on( buttons ).fill().top( inputSection.getControl() ).noBottom();

        // output
        if (bgjob.get().state() != State.NOT_YET_STARTED) {
            outputSection = createOutputSection();
            FormDataFactory.on( outputSection.getControl() ).fill().top( buttons ).noBottom();

            if (bgjob.get().state() == State.RUNNING) {
                createProgressContents();
            }
            else {
                fillOutputFields();
                parent.layout( true, true );
            }
        }
    }


    /**
     * Creates the UI under the given parent.
     */
    protected void createProgressContents() {
        outputSection.getBody().setLayout( FormLayoutFactory.defaults().margins( 0, 30 ).create() );
        
        Label wheel = new Label( outputSection.getBody(), SWT.CENTER );
        wheel.setLayoutData( FormDataFactory.filled().noBottom().create() );
        wheel.setText( "Crunching data..." );
       // wheel.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );

        progress = new Label( outputSection.getBody(), SWT.CENTER );
        progress.setLayoutData( FormDataFactory.filled().top( wheel, 10 ).create() );
        
        EventManager.instance().subscribe( this, isType( ProgressEvent.class, ev -> { 
            return ev.getSource() == bgjob.get();
        }));
    }


    @EventHandler( scope=org.polymap.core.runtime.event.Event.Scope.JVM, display=true, delay=500 )
    protected void onProgressEvent( List<ProgressEvent> evs ) {
        if (parent.isDisposed() || progress.isDisposed()) {
            EventManager.instance().unsubscribe( this );
        }
        else {
            parent.getDisplay().asyncExec( () -> {
                ProgressEvent ev = evs.get( 0 );
                StringBuilder s = new StringBuilder( 256 ).append( 
                        Joiner.on( " " ).skipNulls().join( removeEnd( ev.taskName(), "..." ), " ...", ev.subTaskName().orElse( null ) ) );
                ev.completed().ifPresent( value -> 
                        s.append( " (" ).append( value ).append( "%)" ) );
                progress.setText( s.toString() );
            });
        }
    }


    protected IPanelSection createInputSection() {
        IPanelSection section = tk().createPanelSection( parent, "Input", SWT.BORDER );
        section.getBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 8 ).spacing( 10 ).create() );

        Label label = tk().createLabel( section.getBody(), moduleInfo.description.get().orElse( "No description." ), SWT.WRAP );
        label.setLayoutData( ColumnDataFactory.defaults().widthHint( 300 ).create() );
        label.setEnabled( false );
        
        AtomicBoolean isFirst = new AtomicBoolean( true );
        for (FieldInfo fieldInfo : moduleInfo.inputFields()) {
            // skip
            if (IJGTProgressMonitor.class.isAssignableFrom( fieldInfo.type.get() )
                    || !fieldInfo.description.get().isPresent()) {
                continue;
            }
            // separator
            if (!isFirst.getAndSet( false )) {
                Label sep = new Label( section.getBody(), SWT.SEPARATOR|SWT.HORIZONTAL );
                UIUtils.setVariant( sep, DefaultToolkit.CSS_SECTION_SEPARATOR );  // XXX
            }
            // field
            FieldViewer fieldViewer = new FieldViewer( new FieldViewerSite()
                    .moduleInfo.put( moduleInfo )
                    .module.put( module )
                    .fieldInfo.put( fieldInfo )
                    .layer.put( layer ) );
            fieldViewer.createContents( section.getBody() )
                    .setLayoutData( ColumnDataFactory.defaults().widthHint( 300 ).create() );
            inputFields.add( fieldViewer );
        }
        
        return section;
    }


    protected IPanelSection createOutputSection() {
        IPanelSection section = tk().createPanelSection( parent, "Output", SWT.BORDER );
        return section;
    }


    protected Composite createButtonsSection() {
        Composite section = tk().createComposite( parent );
        section.setLayout( RowLayoutFactory.fillDefaults().spacing( 8 ).margins( 2, 3 ).fill( true ).justify( true ).create() );
        
        startBtn = tk().createButton( section, "", SWT.PUSH );
        startBtn.setLayoutData( RowDataFactory.swtDefaults().hint( 150, SWT.DEFAULT ).create() );
        startBtn.addSelectionListener( UIUtils.selectionListener( ev -> {
            if (bgjob.get().state() != State.RUNNING) {
                startProcess();
            } else {
                stopProcess();
            }
        }));
        updateStartBtn();
        return section;
    }
    
    
    protected void updateStartBtn() {
        if (bgjob.get().state() == State.RUNNING) {
            startBtn.setText( "STOP" );
            startBtn.setImage( P4Plugin.images().svgImage( "stop-circle-outline.svg", SvgImageRegistryHelper.WHITE24 ) );
        }
        else {
            startBtn.setText( "RUN" );
            startBtn.setImage( P4Plugin.images().svgImage( "play-circle-outline.svg", SvgImageRegistryHelper.WHITE24 ) );
        }
    }
    
    
    protected void stopProcess() {
        if (bgjob.get().state() == State.RUNNING) {
            bgjob.get().cancel();
        }
        updateStartBtn();
        UIUtils.disposeChildren( outputSection.getBody() );
        Label msg = new Label( outputSection.getBody(), SWT.NONE );
        msg.setText( "Cancel requested..." );
        parent.layout( true, true );
    }
    
    
    protected void startProcess() {
        // create/clear outputSection
        if (outputSection == null) {
            outputSection = createOutputSection();
            FormDataFactory.on( outputSection.getControl() ).fill().top( buttons ).noBottom();
        }
        else {
            UIUtils.disposeChildren( outputSection.getBody() );
        }

        // start BackgroundJob
        bgjob.get().start( jobChangeListener() );
        
        // connect monitor
        createProgressContents();
        parent.layout( true, true );
        
        updateStartBtn();
    }

    
    protected IJobChangeListener jobChangeListener() {
        Display display = startBtn.getDisplay();
        return new JobChangeAdapter() {
            @Override
            public void done( IJobChangeEvent ev ) {
                if (display.isDisposed()) {
                    ev.getJob().removeJobChangeListener( this );
                }
                else {
                    display.asyncExec( () -> {
                        if (outputSection.getBody().isDisposed()) {
                            ev.getJob().removeJobChangeListener( this );
                        }
                        else {
                            updateStartBtn();

                            UIUtils.disposeChildren( outputSection.getBody() );
                            if (ev.getResult().isOK()) {
                                fillOutputFields();
                            }
                            else {
                                Throwable e = ev.getResult().getException();
                                Label msg = new Label( outputSection.getBody(), SWT.WRAP );
                                msg.setForeground( FieldViewer.errorColor() );
                                msg.setText( defaultString( e != null 
                                        ? Throwables.getRootCause( e ).getMessage() 
                                        : ev.getResult().getMessage(),
                                        "-" ) );
                                FormDataFactory.on( msg ).fill().width( 200 );
                            }
                            parent.layout( true, true );
                        }
                    });
                }
            }
        };
    }

    
    protected void fillOutputFields() {
        outputSection.getBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 8 ).spacing( 10 ).create() );

        AtomicBoolean isFirst = new AtomicBoolean( true );
        for (FieldInfo fieldInfo : moduleInfo.outputFields()) {
            if (fieldInfo.description.get().isPresent()) {
                // separator
                if (!isFirst.getAndSet( false )) {
                    Label sep = new Label( outputSection.getBody(), SWT.SEPARATOR|SWT.HORIZONTAL );
                    UIUtils.setVariant( sep, DefaultToolkit.CSS_SECTION_SEPARATOR );  // XXX
                }
                // field
                FieldViewer fieldViewer = new FieldViewer( new FieldViewerSite()
                        .moduleInfo.put( moduleInfo )
                        .module.put( module )
                        .fieldInfo.put( fieldInfo )
                        .layer.put( layer ) );
                fieldViewer.createContents( outputSection.getBody() )
                        .setLayoutData( ColumnDataFactory.defaults().widthHint( 300 ).create() );
                inputFields.add( fieldViewer );
            }
        }
    }
    
}
