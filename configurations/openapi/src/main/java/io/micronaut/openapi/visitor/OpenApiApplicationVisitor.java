/*
 * Copyright 2017-2018 original authors
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

package io.micronaut.openapi.visitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.inject.visitor.ClassElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import io.micronaut.inject.writer.GeneratedFile;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.Writer;
import java.util.Optional;

/**
 * Visits the application class.
 *
 * @author graemerocher
 * @since 1.0
 */
@Experimental
public class OpenApiApplicationVisitor extends AbstractOpenApiVisitor implements TypeElementVisitor<OpenAPIDefinition, Object> {

    private ClassElement classElement;

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        context.info("Generating OpenAPI Documentation", element);
        Optional<OpenAPI> attr = context.get(ATTR_OPENAPI, OpenAPI.class);
        OpenAPI openAPI = readOpenAPI(element, context);
        if (attr.isPresent()) {
            OpenAPI existing = attr.get();
            existing.setInfo(openAPI.getInfo());
            existing.setTags(openAPI.getTags());
            existing.setServers(openAPI.getServers());
            existing.setSecurity(openAPI.getSecurity());
            existing.setExternalDocs(openAPI.getExternalDocs());
            existing.setExtensions(openAPI.getExtensions());

        } else {
            context.put(ATTR_OPENAPI, openAPI);
        }

        if (Boolean.getBoolean(ATTR_TEST_MODE)) {
            testReference = openAPI;
        }

        this.classElement = element;
    }

    private OpenAPI readOpenAPI(ClassElement element, VisitorContext context) {
        return element.findAnnotation(OpenAPIDefinition.class).flatMap(o -> {
                    JsonNode jsonNode = toJson(o.getValues());

                    try {
                        return Optional.of(jsonMapper.treeToValue(jsonNode, OpenAPI.class));
                    } catch (JsonProcessingException e) {
                        context.warn("Error reading Swagger OpenAPI for element [" + element + "]: " + e.getMessage(), element);
                        return Optional.empty();
                    }
                }).orElse(new OpenAPI());
    }

    @Override
    public void finish(VisitorContext visitorContext) {

        if (classElement != null) {

            Optional<OpenAPI> attr = visitorContext.get(ATTR_OPENAPI, OpenAPI.class);

            attr.ifPresent(openAPI -> {
                Optional<GeneratedFile> generatedFile = visitorContext.visitMetaInfFile("swagger.yml");
                if (generatedFile.isPresent()) {
                    GeneratedFile f = generatedFile.get();
                    try {
                        Writer writer = f.openWriter();
                        yamlMapper.writeValue(writer, openAPI);
                    } catch (Exception e) {
                        visitorContext.warn("Unable to generate swagger.yml: " + e.getMessage() , classElement);
                    }
                }
            });
        }
    }
}
