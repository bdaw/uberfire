package org.uberfire.client;

import java.util.Collection;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.client.mvp.AcceptItem;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.WorkbenchScreenActivity;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.Position;
import org.uberfire.workbench.model.menu.Menus;
import org.uberfire.workbench.model.toolbar.ToolBar;

public class JSWorkbenchScreenActivity implements WorkbenchScreenActivity {

    private PlaceManager placeManager;

    private PlaceRequest place;

    private Command callback;

    private JSNativePlugin nativePlugin;

    public JSWorkbenchScreenActivity( final JSNativePlugin nativePlugin,
                                      final PlaceManager placeManager ) {
        this.nativePlugin = nativePlugin;
        this.placeManager = placeManager;
    }

    @Override
    public void launch( final PlaceRequest place,
                        final Command callback ) {
        this.place = place;
        this.callback = callback;
    }

    @Override
    public void launch( final AcceptItem acceptPanel,
                        final PlaceRequest place,
                        final Command callback ) {
        launch( place, callback );
        onStart( place );
        acceptPanel.add( getTitle(), getTitleDecoration(), getWidget() );

        if ( nativePlugin.getType() != null && nativePlugin.getType().equalsIgnoreCase( "angularjs" ) ) {
            bind();
        }

        onReveal();
    }

    @Override
    public void onStart() {
        nativePlugin.onStart();
    }

    @Override
    public void onStart( final PlaceRequest place ) {
        nativePlugin.onStart( place );
    }

    @Override
    public boolean onMayClose() {
        return nativePlugin.onMayClose();
    }

    @Override
    public void onClose() {
        nativePlugin.onClose();
    }

    @Override
    public Position getDefaultPosition() {
        return Position.ROOT;
    }

    @Override
    public void onFocus() {
        nativePlugin.onFocus();
    }

    @Override
    public void onLostFocus() {
        nativePlugin.onLostFocus();
    }

    @Override
    public String getTitle() {
        return nativePlugin.getTitle();
    }

    @Override
    public IsWidget getTitleDecoration() {
        return null;
    }

    @Override
    public IsWidget getWidget() {
        return new HTML( nativePlugin.getElement().getInnerHTML() );
    }

    @Override
    public Menus getMenus() {
        return null;
    }

    @Override
    public ToolBar getToolBar() {
        return null;
    }

    @Override
    public void onReveal() {
        nativePlugin.onReveal();

        executeOnRevealCallback();
    }

    @Override
    public String getSignatureId() {
        return nativePlugin.getId();
    }

    @Override
    public Collection<String> getRoles() {
        return nativePlugin.getRoles();
    }

    @Override
    public Collection<String> getTraits() {
        return nativePlugin.getTraits();
    }

    private void executeOnRevealCallback() {
        if ( callback != null ) {
            callback.execute();
        }
        placeManager.executeOnRevealCallback( this.place );
    }

    // Alias registerPlugin with a global JS function.
    private native String bind() /*-{
        $wnd.angular.bootstrap($wnd.document, []);
    }-*/;

}
