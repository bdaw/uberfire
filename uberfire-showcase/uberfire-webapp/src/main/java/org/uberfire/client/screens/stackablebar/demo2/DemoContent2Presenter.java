/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.uberfire.client.screens.stackablebar.demo2;

import com.google.gwt.user.client.Window;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

/**
 *
 * @author salaboy
 */
@Dependent
@WorkbenchScreen(identifier = "Demo Content 2")
public class DemoContent2Presenter {

    public interface View
            extends UberView<DemoContent2Presenter> {
        void displayNotification( String text );
        
    }
   
    @Inject
    public View view;
    

    public DemoContent2Presenter() {
    }


    @WorkbenchPartTitle
    public String getTitle() {
        return "Demo Content 2";
    }

    @WorkbenchPartView
    public UberView<DemoContent2Presenter> getWidget() {
        return view;
    }

    
    @WorkbenchMenu
    public Menus buildMenuBar() {
        return MenuFactory
                .newTopLevelMenu( "Menu 1" ).respondsWith( new Command() {
                            @Override
                            public void execute() {
                               
                                    Window.alert( "Alert from Menu 1" );
                               
                            }
                        } )

                .endMenu()
                .newTopLevelMenu( "Menu 2" ).respondsWith( new Command() {
                            @Override
                            public void execute() {
                               
                                    Window.alert( "Alert from Menu 2" );
                               
                            }
                        } )

                .endMenu()
                
                
                
                .build();
    }
   
}
