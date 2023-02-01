/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.testassist.junit;

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.InitParameters;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.VaadinServletContextInitializer;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.sys.ViewControllersConfiguration;
import io.jmix.flowui.testassist.support.TestServletContext;
import io.jmix.flowui.testassist.support.TestSpringServlet;
import io.jmix.flowui.testassist.support.TestVaadinRequest;
import io.jmix.flowui.testassist.support.TestVaadinSession;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nullable;
import javax.servlet.ServletException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField;
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;

/**
 * Extension starts Vaadin Flow before each test and configures view packages and authentication.
 * <p>
 * For instance:
 * <pre>
 * &#64;ExtendWith(SpringExtension.class)
 * &#64;SpringBootTest
 * public class FlowJunitTest {
 *
 *     &#64;RegisterExtension
 *     private FlowuiTestExtension extension = new FlowuiTestExtension()
 *             .withTestAuthenticator(new TestAppAuthenticator());
 *
 *     &#64;Autowired
 *     private ViewNavigators viewNavigators;
 *
 *     &#64;Test
 *     public void testOrderView() {
 *         viewNavigators.view(OrderListView.class)
 *                 .navigate();
 *     }
 * }
 * </pre>
 * For annotation based approach use {@link FlowuiTest} annotation to configure the extension:
 * <pre>
 * &#64;FlowuiTest(authenticator = TestAppAuthenticator.class)
 * &#64;ExtendWith({SpringExtension.class, FlowuiTestExtension.class})
 * &#64;SpringBootTest
 * public class FlowJunitTest {
 *
 *     &#64;Autowired
 *     private ViewNavigators viewNavigators;
 *
 *     &#64;Test
 *     public void testOrderView() {
 *         viewNavigators.view(OrderListView.class)
 *                 .navigate();
 *     }
 * }
 * </pre>
 */
public class FlowuiTestExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(FlowuiTestExtension.class);

    private static final String APP_ID = "testFlowuiAppId";

    public static final String VIEW_PACKAGES = "viewPackages";

    protected String[] viewBasePackages;

    // saving session and UI to avoid it be GC'ed
    protected VaadinSession vaadinSession;
    protected UI ui;

    protected TestAuthenticator testAuthenticator;

    /**
     * @return view base packages or {@code null} if not set
     */
    @Nullable
    public String[] getViewBasePackages() {
        return viewBasePackages;
    }

    /**
     * Sets view base packages. Views under these packages will be available in test.
     * <p>
     * Note that depending on the test's configuration all application views may be available.
     *
     * @param viewBasePackages view base packages
     * @return current instance of extension
     */
    public FlowuiTestExtension withViewBasePackages(@Nullable String... viewBasePackages) {
        this.viewBasePackages = viewBasePackages;
        return this;
    }

    /**
     * @return authenticator or {@code null} if not set
     */
    @Nullable
    public TestAuthenticator getTestAuthenticator() {
        return testAuthenticator;
    }

    /**
     * Sets authentication management provider that will be used in tests before/after each test.
     * <p>
     * Provided authenticator will override a bean implementing {@link TestAuthenticator} for the test class.
     *
     * @param testAuthenticator authenticator to set
     * @return current instance of extension
     * @see TestAuthenticator
     */
    public FlowuiTestExtension withTestAuthenticator(@Nullable TestAuthenticator testAuthenticator) {
        this.testAuthenticator = testAuthenticator;
        return this;
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (testAuthenticator == null) {
            testAuthenticator = getTestAuthenticatorFromAnnotation(context);
        }
        if (testAuthenticator == null) {
            testAuthenticator = getApplicationContext(context).getBeanProvider(TestAuthenticator.class)
                    .getIfAvailable();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        setupAuthentication(context);
        setupVaadin(context);
        registerViewBasePackages(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        removeAuthentication(context);

        // Do not remove View configurations from ViewRegistry,
        // because they are initialized only once.
    }

    protected void setupVaadin(ExtensionContext context) {
        ApplicationContext applicationContext = getApplicationContext(context);

        SpringServlet springServlet = new TestSpringServlet(applicationContext, true);

        VaadinServletContextInitializer contextInitializer =
                applicationContext.getBean(VaadinServletContextInitializer.class, applicationContext);
        try {
            TestServletContext servletContext = new TestServletContext();
            servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
            servletContext.setInitParameter(InitParameters.SERVLET_PARAMETER_ENABLE_DEV_SERVER, "false");

            MockServletConfig mockServletConfig = new MockServletConfig(servletContext);

            // fill servlet context with listeners from VaadinServletContextInitializer
            contextInitializer.onStartup(mockServletConfig.getServletContext());

            servletContext.fireServletContextInitialized();

            springServlet.init(mockServletConfig);
        } catch (ServletException e) {
            throw new IllegalStateException(String.format("Cannot init %s", TestSpringServlet.class.getName()), e);
        }

        VaadinService.setCurrent(springServlet.getService());

        vaadinSession = new TestVaadinSession(springServlet.getService());
        VaadinSession.setCurrent(vaadinSession);

        TestVaadinRequest request = new TestVaadinRequest(springServlet.getService());
        CurrentInstance.set(VaadinRequest.class, request);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ui = new UI();
        ui.getInternals().setSession(vaadinSession);
        ui.doInit(request, 1, APP_ID);
        UI.setCurrent(ui);
    }

    protected void registerViewBasePackages(ExtensionContext context) {
        String[] storedViewBasePackages = getStore(context).get(VIEW_PACKAGES, String[].class);
        if (ArrayUtils.isNotEmpty(storedViewBasePackages)) {
            return;
        }

        String[] viewBasePackages = {};
        if (ArrayUtils.isNotEmpty(this.viewBasePackages)) {
            viewBasePackages = this.viewBasePackages;
        } else {
            Optional<FlowuiTest> annotationOpt =
                    AnnotationSupport.findAnnotation(context.getTestClass(), FlowuiTest.class);
            if (annotationOpt.isPresent()) {
                viewBasePackages = annotationOpt.get().viewBasePackages();
            }
        }

        if (ArrayUtils.isEmpty(viewBasePackages)) {
            return;
        }

        ApplicationContext applicationContext = getApplicationContext(context);

        AnnotationScanMetadataReaderFactory metadataReaderFactory =
                applicationContext.getBean(AnnotationScanMetadataReaderFactory.class);

        ViewControllersConfiguration configuration =
                new ViewControllersConfiguration(applicationContext, metadataReaderFactory);
        applicationContext.getAutowireCapableBeanFactory()
                .autowireBean(configuration);
        configuration.setBasePackages(Arrays.asList(viewBasePackages));

        try {
            Field configurationsField = getDeclaredField(ViewRegistry.class,
                    "configurations", true);

            ViewRegistry viewRegistry = getApplicationContext(context).getBean(ViewRegistry.class);

            //noinspection unchecked
            Collection<ViewControllersConfiguration> configurations =
                    (Collection<ViewControllersConfiguration>) configurationsField.get(viewRegistry);

            List<ViewControllersConfiguration> modifiedConfigurations = new ArrayList<>(configurations);
            modifiedConfigurations.add(configuration);

            configurationsField.set(viewRegistry, modifiedConfigurations);

            getDeclaredField(ViewRegistry.class, "initialized", true)
                    .set(viewRegistry, false);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot register view packages", e);
        }

        getStore(context).put(VIEW_PACKAGES, viewBasePackages);

        registerViewRoutes(viewBasePackages, context);
    }

    protected void registerViewRoutes(String[] viewBasePackages, ExtensionContext context) {
        if (ArrayUtils.isEmpty(viewBasePackages)) {
            return;
        }

        ApplicationContext applicationContext = getApplicationContext(context);

        List<ViewInfo> viewInfos = applicationContext.getBean(ViewRegistry.class)
                .getViewInfos().stream()
                .filter(info -> isClassInPackages(info.getControllerClass().getPackageName(), viewBasePackages))
                .collect(Collectors.toList());

        for (ViewInfo view : viewInfos) {
            Class<? extends View> controllerClass = view.getControllerClass();
            Route route = controllerClass.getAnnotation(Route.class);
            if (route == null) {
                return;
            }

            RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
            if (Strings.isNullOrEmpty(route.value())
                    || routeConfiguration.isPathAvailable(route.value())) {
                return;
            }

            if (route.layout() == UI.class) {
                routeConfiguration.setRoute(route.value(), controllerClass);
            } else {
                routeConfiguration.setRoute(route.value(), controllerClass, route.layout());
            }
        }
    }

    protected boolean isClassInPackages(String classPackage, String[] viewBasePackages) {
        return Arrays.stream(viewBasePackages).anyMatch(classPackage::startsWith);
    }

    protected void setupAuthentication(ExtensionContext context) {
        if (testAuthenticator != null) {
            testAuthenticator.setupAuthentication(getApplicationContext(context));
        } else {
            getApplicationContext(context).getBean(SystemAuthenticator.class).begin();
        }
    }

    protected void removeAuthentication(ExtensionContext context) {
        if (testAuthenticator != null) {
            testAuthenticator.removeAuthentication(getApplicationContext(context));
        } else {
            getApplicationContext(context).getBean(SystemAuthenticator.class).end();
        }
    }

    @Nullable
    protected TestAuthenticator getTestAuthenticatorFromAnnotation(ExtensionContext context) {
        Optional<FlowuiTest> annotationOpt =
                AnnotationSupport.findAnnotation(context.getTestClass(), FlowuiTest.class);
        if (annotationOpt.isEmpty()) {
            return null;
        }

        Class<? extends TestAuthenticator> authenticatorClass = annotationOpt.get().authenticator();
        if (FlowuiTest.NoopAuthenticator.class.isAssignableFrom(authenticatorClass)) {
            return null;
        }

        try {
            Constructor<? extends TestAuthenticator> constructor = authenticatorClass.getConstructor();
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Cannot instantiate " +
                    TestAuthenticator.class.getSimpleName(), e);
        }
    }

    protected ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(NAMESPACE);
    }

    protected ApplicationContext getApplicationContext(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context);
    }
}
