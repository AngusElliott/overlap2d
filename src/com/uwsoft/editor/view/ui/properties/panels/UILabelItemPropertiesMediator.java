package com.uwsoft.editor.view.ui.properties.panels;

import com.badlogic.ashley.core.Entity;
import com.uwsoft.editor.view.stage.Sandbox;
import com.uwsoft.editor.Overlap2DFacade;
import com.uwsoft.editor.proxy.FontManager;
import com.uwsoft.editor.proxy.ResourceManager;
import com.uwsoft.editor.view.ui.properties.UIItemPropertiesMediator;
import com.uwsoft.editor.renderer.components.label.LabelComponent;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

/**
 * Created by avetiszakharyan on 4/24/15.
 */
public class UILabelItemPropertiesMediator extends UIItemPropertiesMediator<Entity, UILabelItemProperties> {

    private static final String TAG = UILabelItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private String prevText = null;

    private FontManager fontManager;

    public UILabelItemPropertiesMediator() {
        super(NAME, new UILabelItemProperties());
    }


    @Override
    public void onRegister() {
        facade = Overlap2DFacade.getInstance();
        fontManager = facade.retrieveProxy(FontManager.NAME);
        lockUpdates = true;
        viewComponent.setFontFamilyList(fontManager.getFontNamesFromMap());
        lockUpdates = false;
    }

    @Override
    protected void translateObservableDataToView(Entity item) {
        LabelComponent labelComponent = ComponentRetriever.get(item, LabelComponent.class);
        viewComponent.setFontFamily(labelComponent.fontName);
        viewComponent.setFontSize(labelComponent.fontSize);
        viewComponent.setAlignValue(labelComponent.labelAlign);
        viewComponent.setText(labelComponent.text.toString());

    }

    @Override
    protected void translateViewToItemData(Object customData) {

        final String newText = viewComponent.getText();
        if(prevText == null) this.prevText = newText;

        if (UILabelItemProperties.LABEL_TEXT_CHAR_TYPED.equals(customData)) {
            LabelComponent labelComponent = ComponentRetriever.get(observableReference, LabelComponent.class);
            labelComponent.setText(viewComponent.getText());
            return;
        }

        Object[] payload = new Object[6];
        payload[0] = observableReference;
        payload[1] = viewComponent.getFontFamily();
        payload[2] = viewComponent.getFontSize();
        payload[3] = viewComponent.getAlignValue();
        payload[4] = newText;
        payload[5] = prevText;
        sendNotification(Sandbox.ACTION_UPDATE_LABEL_DATA, payload);

        this.prevText = newText;

    }

    @Override
    protected void afterItemDataModified() {

    }
}
