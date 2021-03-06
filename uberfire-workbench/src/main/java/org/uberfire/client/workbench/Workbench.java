/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.client.workbench;

import java.util.Collection;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.IOCBeanManager;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchDragAndDropManager;
import org.uberfire.client.workbench.widgets.dnd.WorkbenchPickupDragController;
import org.uberfire.client.workbench.widgets.navbar.NavBar;
import org.uberfire.client.workbench.PanelManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.events.ApplicationReadyEvent;
import org.uberfire.workbench.model.PanelDefinition;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PanelDefinitionImpl;
import org.uberfire.workbench.services.WorkbenchServices;

import static org.uberfire.workbench.model.PanelType.*;

@ApplicationScoped
public class Workbench
        extends Composite
        implements RequiresResize {

    private final VerticalPanel container = new VerticalPanel();

    private final SimplePanel workbench = new SimplePanel();

    private AbsolutePanel workbenchContainer;

    @Inject
    private PanelManager panelManager;

    @Inject
    private IOCBeanManager iocManager;

    @Inject
    private WorkbenchDragAndDropManager dndManager;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private WorkbenchPickupDragController dragController;

    @Inject
    private NavBar navBar;

    @Inject
    private Caller<WorkbenchServices> wbServices;

    @PostConstruct
    public void setup() {
        if ( !Window.Location.getParameterMap().containsKey( "standalone" ) ) {
            container.add( navBar );
        }

        //Container panels for workbench
        workbenchContainer = dragController.getBoundaryPanel();
        workbenchContainer.add( workbench );
        container.add( workbenchContainer );

        initWidget( container );
    }

    @SuppressWarnings("unused")
    private void bootstrap( @Observes ApplicationReadyEvent event ) {

        //Clear environment
        workbench.clear();
        dndManager.unregisterDropControllers();

        //Add default workbench widget
        final PanelDefinition root = new PanelDefinitionImpl( ROOT_STATIC );
        panelManager.setRoot( root );
        workbench.setWidget( panelManager.getPanelView( root ) );

        //Size environment - Defer so Widgets have been rendered and hence sizes available
        Scheduler.get().scheduleDeferred( new ScheduledCommand() {

            @Override
            public void execute() {
                final int width = Window.getClientWidth();
                final int height = Window.getClientHeight();
                doResizeWorkbenchContainer( width,
                                            height );
            }

        } );

        //Lookup PerspectiveProviders and if present launch it to set-up the Workbench
        if ( !Window.Location.getParameterMap().containsKey( "standalone" ) ) {
            final PerspectiveActivity defaultPerspective = getDefaultPerspectiveActivity();
            if ( defaultPerspective != null ) {
                placeManager.goTo( new DefaultPlaceRequest( defaultPerspective.getIdentifier() ) );
            }
        }

        //Save Workbench state when Window is closed
        Window.addWindowClosingHandler( new ClosingHandler() {

            @Override
            public void onWindowClosing( ClosingEvent event ) {
                final PerspectiveDefinition perspective = panelManager.getPerspective();
                if ( perspective != null ) {
                    wbServices.call( new RemoteCallback<Void>() {
                        @Override
                        public void callback( Void response ) {
                            //Nothing to do. Window is closing.
                        }
                    } ).save( perspective );
                }
            }

        } );

        //Resizing the Window should resize everything
        Window.addResizeHandler( new ResizeHandler() {
            @Override
            public void onResize( ResizeEvent event ) {
                doResizeWorkbenchContainer( event.getWidth(),
                                            event.getHeight() );
            }
        } );

        Scheduler.get().scheduleDeferred( new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onResize();
            }
        } );

    }

    private PerspectiveActivity getDefaultPerspectiveActivity() {
        PerspectiveActivity defaultPerspective = null;
        final Collection<IOCBeanDef<PerspectiveActivity>> perspectives = iocManager.lookupBeans( PerspectiveActivity.class );
        final Iterator<IOCBeanDef<PerspectiveActivity>> perspectivesIterator = perspectives.iterator();
        while ( perspectivesIterator.hasNext() ) {
            final IOCBeanDef<PerspectiveActivity> perspective = perspectivesIterator.next();
            final PerspectiveActivity instance = perspective.getInstance();
            if ( instance.isDefault() ) {
                defaultPerspective = instance;
            } else {
                iocManager.destroyBean( instance );
            }
        }
        return defaultPerspective;
    }

    @Override
    public void onResize() {
        final int width = Window.getClientWidth();
        final int height = Window.getClientHeight();
        doResizeWorkbenchContainer( width,
                                    height );
    }

    private void doResizeWorkbenchContainer( final int width,
                                             final int height ) {
        final int navBarHeight = navBar.asWidget().getOffsetHeight();
        final int availableHeight;
        if ( !Window.Location.getParameterMap().containsKey( "standalone" ) ) {
            availableHeight = height - navBarHeight;
        } else {
            availableHeight = height;
        }

        workbenchContainer.setPixelSize( width, availableHeight );
        workbench.setPixelSize( width, availableHeight );

        final Widget w = workbench.getWidget();
        if ( w != null ) {
            if ( w instanceof RequiresResize ) {
                ( (RequiresResize) w ).onResize();
            }
        }
    }
}
