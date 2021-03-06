package org.rapidoid.integrate;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.http.customize.ViewRenderer;
import org.rapidoid.io.Res;
import org.rapidoid.render.Templates;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/*
 * #%L
 * rapidoid-integrate
 * %%
 * Copyright (C) 2014 - 2016 Nikolche Mihajlovski and contributors
 * %%
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
 * #L%
 */

/**
 * <p>ViewRenderer for samskivert's JMustache. <br>
 * To use this call {@code My.viewRenderer(new JMustacheViewRenderer());} </p>
 *
 * <p>
 * By default template partials should be stored in /templates/partials folder and
 * should have the .mustache file extension. You can change this by setting a new
 * compiler with your own template loader. Example on partials:
 * </p>
 * {{>name}} somewhere will by default look for a /templates/partials/name.mustache
 * in your resources folder and load the contents into the mustache template.
 *
 *
 * <p>If you want to change any compiler configurations or add partial support do e.g.:</p>
 * <pre>{@code
 *    JMustacheViewRenderer renderer = (JMustacheViewRenderer) My.getViewRenderer();
 *    renderer.setCompiler(Mustache.compiler().defaultValue("Template not found!"));
 * }</pre>
 */
@Authors("kormakur")
@Since("5.1.8")
public class JMustacheViewRenderer extends RapidoidThing implements ViewRenderer {

    private Mustache.TemplateLoader defaultPartialLoader = new Mustache.TemplateLoader() {
        @Override
        public Reader getTemplate(String name) throws Exception {
            return Templates.resource("templates/partials/" + name + ".mustache").getReader();
        }
    };

    private Mustache.Compiler compiler = Mustache.compiler().withLoader(defaultPartialLoader);

    @Override
    public boolean render(String viewName, Object[] model, OutputStream out) throws Exception {
        Res template = Templates.resource(viewName + ".html");

        if (!template.exists()) {
            return false;
        }

        Template mustache = compiler.compile(template.getContent());
        PrintWriter writer = new PrintWriter(out);
        mustache.execute(model[model.length-1], writer);
        writer.flush();
        return true;
    }

    /**
     * Change the compiler settings e.g.
     * <pre>
     *     {@code
     *        renderer.setCompiler(Mustache.compiler().defaultValue("Template not found"));
     *     }
     * </pre>
     * @param compiler Usually Mustache.compiler() with supplied settings.
     */
    public void setCompiler(Mustache.Compiler compiler) {
        if(Mustache.compiler().loader.equals(compiler.loader)) {
            this.compiler = compiler.withLoader(defaultPartialLoader);
        }
        else {
            this.compiler = compiler;
        }
    }
}
