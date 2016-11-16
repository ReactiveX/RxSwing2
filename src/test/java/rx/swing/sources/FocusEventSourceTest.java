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

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;

import org.junit.Test;
import org.mockito.Matchers;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class FocusEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testObservingFocusEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                
            	@SuppressWarnings("unchecked")
                Consumer<FocusEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final FocusEvent event = mock(FocusEvent.class);

                Disposable sub = FocusEventSource.fromFocusEventsOf(comp)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<FocusEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                fireFocusEvent(event);
                verify(action, times(1)).accept(Matchers.<FocusEvent> any());

                fireFocusEvent(event);
                verify(action, times(2)).accept(Matchers.<FocusEvent> any());

                sub.dispose();
                fireFocusEvent(event);
                verify(action, times(2)).accept(Matchers.<FocusEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    private void fireFocusEvent(FocusEvent event) {
        for (FocusListener listener : comp.getFocusListeners()) {
            listener.focusGained(event);
        }
    }
}
