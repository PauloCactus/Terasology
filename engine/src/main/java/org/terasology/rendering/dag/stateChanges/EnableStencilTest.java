/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.stateChanges;

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import org.terasology.rendering.dag.StateChange;

/**
 * Instances of this class enable OpenGL's stencil testing, potentially used in a variety
 * of advanced computer graphics tricks such as stenciled shadows.
 */
public final class EnableStencilTest extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableStencilTest();

    /**
     * Constructs an instance of this StateChange. This is can be used in a node's initialise() method in
     * the form:
     *
     * addDesiredStateChange(new EnableStencilTest());
     *
     * This trigger the inclusion of an EnableStateParameterTask instance and a DisableStateParameterTask instance
     * in the rendering task list, each instance enabling/disabling respectively the GL_CULL_FACE mode. The
     * two task instance frame the execution of a node's process() method unless they are deemed redundant,
     * i.e. because the upstream or downstream node also enables face culling.
     */
    public EnableStencilTest() {
        super(GL_STENCIL_TEST);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }
}
