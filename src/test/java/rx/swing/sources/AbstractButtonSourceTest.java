/**
 * Copyright 2014 Netflix, Inc.
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
package rx.swing.sources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;

import org.junit.Test;
import org.mockito.Matchers;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class AbstractButtonSourceTest {
    @Test
    public void testObservingActionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception {
                @SuppressWarnings("unchecked")
                Consumer<ActionEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final ActionEvent event = new ActionEvent(this, 1, "command");

                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testAction() {
                        fireActionPerformed(event);
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = AbstractButtonSource.fromActionOf(button).subscribe(action,
                        error, complete);

                verify(action, never()).accept(Matchers.<ActionEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                button.testAction();
                verify(action, times(1)).accept(Matchers.<ActionEvent> any());

                button.testAction();
                verify(action, times(2)).accept(Matchers.<ActionEvent> any());

                sub.dispose();
                button.testAction();
                verify(action, times(2)).accept(Matchers.<ActionEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }
}
