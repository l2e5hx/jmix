/*
 * Copyright (c) 2020 Haulmont.
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

package fragment

import fragment.screen.EventListenerTestFragment
import fragment.screen.FragmentXmlListenerTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Fragment
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import java.util.function.Consumer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class FragmentEventsTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["fragment"])
    }

    def "open screen with event listener on fragment controller"() {
        showTestMainScreen()

        def handler = Mock(Consumer)

        when:
        def screen = screens.create(FragmentXmlListenerTestScreen)
        screen.handler = handler
        screen.show()

        then:
        screen.fragmentWithEvent != null

        def fragment = screen.getWindow().getComponent(0) as Fragment
        fragment != null
        def controller = fragment.frameOwner as EventListenerTestFragment
        controller != null

        when:
        controller.hello()

        then:
        1 * handler.accept(_)
    }
}
