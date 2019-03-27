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
package io.micronaut.views.thymeleaf;

import io.micronaut.core.beans.BeanMap;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.Writable;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.views.ViewUtils;
import io.micronaut.views.ViewsConfiguration;
import io.micronaut.views.ViewsRenderer;
import io.micronaut.views.exceptions.ViewRenderingException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Renders templates Thymeleaf Java template engine.
 *
 * @author Sergio del Amo
 * @author graemerocher
 *
 * @see <a href="https://www.thymeleaf.org">https://www.thymeleaf.org</a>
 * @since 1.0
 */
@Produces(MediaType.TEXT_HTML)
@Singleton
public class ThymeleafViewsRenderer implements ViewsRenderer {

    protected final AbstractConfigurableTemplateResolver templateResolver;
    protected final TemplateEngine engine;
    protected ResourceLoader resourceLoader;

    /**
     * @param viewsConfiguration Views Configuration
     * @param thConfiguration    Thymeleaf template renderer configuration
     * @param resourceLoader     The resource loader
     */
    @Deprecated
    public ThymeleafViewsRenderer(ViewsConfiguration viewsConfiguration,
                                  ThymeleafViewsRendererConfiguration thConfiguration,
                                  ClassPathResourceLoader resourceLoader) {
        this.templateResolver = initializeTemplateResolver(viewsConfiguration, thConfiguration);
        this.resourceLoader = resourceLoader;
        this.engine = initializeTemplateEngine();
    }

    /**
     * @param templateResolver   The template resolver
     * @param templateEngine     The template engine
     * @param resourceLoader     The resource loader
     */
    @Inject
    public ThymeleafViewsRenderer(AbstractConfigurableTemplateResolver templateResolver,
                                  TemplateEngine templateEngine,
                                  ClassPathResourceLoader resourceLoader) {
        this.templateResolver = templateResolver;
        this.resourceLoader = resourceLoader;
        this.engine = templateEngine;
    }

    @Override
    @Nonnull
    public Writable render(@Nonnull String viewName, @Nullable Object data) {
        ArgumentUtils.requireNonNull("viewName", viewName);
        return (writer) -> {
            IContext context = new Context(Locale.US, variables(data));
            processView(viewName, writer, context);
        };
    }

    @Override
    @Nonnull
    public Writable render(@Nonnull String viewName, @Nullable Object data,
            @Nonnull HttpRequest<?> request) {
        ArgumentUtils.requireNonNull("viewName", viewName);
        ArgumentUtils.requireNonNull("request", request);
        return (writer) -> {
            IContext context = new WebContext(request, Locale.US, variables(data));
            processView(viewName, writer, context);
        };
    }

    @Override
    public boolean exists(@Nonnull String viewName) {
        String location = viewLocation(viewName);
        return resourceLoader.getResourceAsStream(location).isPresent();
    }

    private TemplateEngine initializeTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }

    private ClassLoaderTemplateResolver initializeTemplateResolver(ViewsConfiguration viewsConfiguration,
                                                                   ThymeleafViewsRendererConfiguration thConfiguration) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        templateResolver.setPrefix(normalizeFolder(viewsConfiguration.getFolder()));
        templateResolver.setCharacterEncoding(thConfiguration.getCharacterEncoding());
        templateResolver.setTemplateMode(thConfiguration.getTemplateMode());
        templateResolver.setSuffix(thConfiguration.getSuffix());
        templateResolver.setForceSuffix(thConfiguration.getForceSuffix());
        templateResolver.setForceTemplateMode(thConfiguration.getForceTemplateMode());
        templateResolver.setCacheTTLMs(thConfiguration.getCacheTTLMs());
        templateResolver.setCheckExistence(thConfiguration.getCheckExistence());
        templateResolver.setCacheable(thConfiguration.getCacheable());
        return templateResolver;
    }

    private static Map<String, Object> variables(@Nullable Object data) {
        if (data == null) {
            return new HashMap<>();
        }
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        } else {
            return BeanMap.of(data);
        }
    }

    private String viewLocation(final String name) {
        return templateResolver.getPrefix() +
                ViewUtils.normalizeFile(name, templateResolver.getSuffix()) +
                templateResolver.getSuffix();
    }

    private void processView(String viewName, Writer writer, IContext context) {
        try {
            engine.process(viewName, context, writer);
        } catch (TemplateEngineException e) {
            throw new ViewRenderingException("Error rendering Thymeleaf view [" + viewName + "]: " + e.getMessage(), e);
        }
    }
}
