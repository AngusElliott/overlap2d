/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.controller.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.view.stage.Sandbox;
import com.uwsoft.editor.Overlap2DFacade;
import com.uwsoft.editor.factory.ItemFactory;
import com.uwsoft.editor.view.ui.FollowersUIMediator;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.components.ParentNodeComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.utils.runtime.ComponentCloner;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.utils.runtime.EntityUtils;
import com.uwsoft.editor.view.ui.box.UILayerBoxMediator;

/**
 * Created by azakhary on 4/28/2015.
 */
public class PasteItemsCommand extends EntityModifyRevertableCommand {

    private Array<Integer> pastedEntityIds = new Array<>();

    @Override
    public void doAction() {
        Object[] payload = (Object[]) Sandbox.getInstance().retrieveFromClipboard();

        if(payload == null) {
            cancel();
            return;
        }

        Vector2 cameraPrevPosition = (Vector2) payload[0];
        Vector2 cameraCurrPosition = new Vector2(Sandbox.getInstance().getCamera().position.x,Sandbox.getInstance().getCamera().position.y);

        Vector2 diff = cameraCurrPosition.sub(cameraPrevPosition);

        Set<Entity> newEntitiesList = new HashSet<>();

        HashMap<Integer, Collection<Component>> backup = (HashMap<Integer, Collection<Component>>) payload[1];
        for (Collection<Component> components : backup.values()) {
            Entity entity = new Entity();
            for(Component component: components) {
                entity.add(ComponentCloner.get(component));
            }

            sandbox.getEngine().addEntity(entity);
            int uniqueId = sandbox.getSceneControl().sceneLoader.entityFactory.postProcessEntity(entity);

            ParentNodeComponent parentNodeComponent = ComponentRetriever.get(entity, ParentNodeComponent.class);
            parentNodeComponent.parentEntity = sandbox.getCurrentViewingEntity();
            NodeComponent nodeComponent =  parentNodeComponent.parentEntity.getComponent(NodeComponent.class);
            nodeComponent.addChild(entity);

            TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
            transformComponent.x += diff.x;
            transformComponent.y += diff.y;

            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
            UILayerBoxMediator layerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
            mainItemComponent.layer = layerBoxMediator.getCurrentSelectedLayerName();

            Overlap2DFacade.getInstance().sendNotification(ItemFactory.NEW_ITEM_ADDED, entity);
            newEntitiesList.add(entity);

            pastedEntityIds.add(uniqueId);

            EntityUtils.reInstantiateChildren(entity);
        }

        sandbox.getSelector().setSelections(newEntitiesList, true);
    }

    @Override
    public void undoAction() {
        FollowersUIMediator followersUIMediator = Overlap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for (Integer entityId : pastedEntityIds) {
            Entity entity = EntityUtils.getByUniqueId(entityId);
            followersUIMediator.removeFollower(entity);
            sandbox.getEngine().removeEntity(entity);
        }
    }
}
