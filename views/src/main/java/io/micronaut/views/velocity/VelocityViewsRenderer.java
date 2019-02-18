/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.views.velocity;

import io.micronaut.core.io.Writable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.ViewUtils;
import io.micronaut.views.ViewsConfiguration;
import io.micronaut.views.ViewsRenderer;
import io.micronaut.views.exceptions.ViewRenderingException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * Renders with templates with Apache Velocity Project.
 *
 * @author Sergio del Amo
 * @author graemerocher
 *
 * @see <a href="http://velocity.apache.org">http://velocity.apache.org</a>
 * @since 1.0
 */
@Produces(MediaType.TEXT_HTML)
@Singleton
public class VelocityViewsRenderer implements ViewsRenderer {

    protected final VelocityEngine velocityEngine;
    protected final ViewsConfiguration viewsConfiguration;
    protected final VelocityViewsRendererConfiguration velocityConfiguration;
    protected final String folder;

    /**
     * @param viewsConfiguration    Views Configuration
     * @param velocityConfiguration Velocity Configuration
     */
    @Deprecated
    VelocityViewsRenderer(ViewsConfiguration viewsConfiguration,
                          VelocityViewsRendererConfiguration velocityConfiguration) {
        this.viewsConfiguration = viewsConfiguration;
        this.velocityConfiguration = velocityConfiguration;
        this.velocityEngine = initializeVelocityEngine();
        this.folder = viewsConfiguration.getFolder();
    }

    /**
     * @param viewsConfiguration    Views Configuration
     * @param velocityConfiguration Velocity Configuration
     * @param velocityEngine        Velocity Engine
     */
    @Inject
    VelocityViewsRenderer(ViewsConfiguration viewsConfiguration,
                          VelocityViewsRendererConfiguration velocityConfiguration,
                          VelocityEngine velocityEngine) {
        this.viewsConfiguration = viewsConfiguration;
        this.velocityConfiguration = velocityConfiguration;
        this.velocityEngine = velocityEngine;
        this.folder = viewsConfiguration.getFolder();
    }

    @Override
    @Nonnull public Writable render(@Nonnull String view, @Nullable Object data) {
        ArgumentUtils.requireNonNull("view", view);
        return (writer) -> {
            Map<String, Object> context = modelOf(data);
            final VelocityContext velocityContext = new VelocityContext(context);
            String viewName = viewName(view);
            try {
                velocityEngine.mergeTemplate(viewName, StandardCharsets.UTF_8.name(), velocityContext, writer);
            } catch (ResourceNotFoundException | ParseErrorException | MethodInvocationException e) {
                throw new ViewRenderingException("Error rendering Velocity view [" + viewName + "]: " + e.getMessage(), e);
            }
        };
    }

    @Override
    public boolean exists(@Nonnull String viewName) {
        try {
            velocityEngine.getTemplate(viewName(viewName));
        } catch (ResourceNotFoundException | ParseErrorException e) {
            return false;
        }
        return true;
    }

    /**
     * Only used in the deprecated constructor.
     */
    private VelocityEngine initializeVelocityEngine() {
        final Properties p = new Properties();
        p.setProperty("resource.loader", "class");
        p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return new VelocityEngine(p);
    }

    private String viewName(final String name) {
        return folder +
                ViewUtils.normalizeFile(name, extension()) +
                "." +
                extension();
    }

    private String extension() {
        return velocityConfiguration.getDefaultExtension();
    }
}
