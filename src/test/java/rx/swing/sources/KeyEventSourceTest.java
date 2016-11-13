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

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class KeyEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testObservingKeyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run()  throws Exception{
                @SuppressWarnings("unchecked")
                Consumer<KeyEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final KeyEvent event = mock(KeyEvent.class);

                Disposable sub = KeyEventSource.fromKeyEventsOf(comp)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<KeyEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                fireKeyEvent(event);
                verify(action, times(1)).accept(Matchers.<KeyEvent> any());

                fireKeyEvent(event);
                verify(action, times(2)).accept(Matchers.<KeyEvent> any());

                sub.dispose();
                fireKeyEvent(event);
                verify(action, times(2)).accept(Matchers.<KeyEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    @Test
    public void testObservingPressedKeys() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                @SuppressWarnings("unchecked")
                Consumer<Set<Integer>> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                Disposable sub = KeyEventSource.currentlyPressedKeysOf(comp)
                        .subscribe(action, error, complete);

                InOrder inOrder = inOrder(action);
                inOrder.verify(action).accept(
                        Collections.<Integer> emptySet());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
                inOrder.verify(action, times(1)).accept(
                        new HashSet<Integer>(asList(1)));
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                fireKeyEvent(keyEvent(2, KeyEvent.KEY_PRESSED));
                fireKeyEvent(keyEvent(KeyEvent.VK_UNDEFINED, KeyEvent.KEY_TYPED));
                inOrder.verify(action, times(1)).accept(
                        new HashSet<Integer>(asList(1, 2)));

                fireKeyEvent(keyEvent(2, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(
                        new HashSet<Integer>(asList(1)));

                fireKeyEvent(keyEvent(3, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(
                        new HashSet<Integer>(asList(1)));

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(
                        Collections.<Integer> emptySet());

                sub.dispose();

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
                inOrder.verify(action, never()).accept(
                        Matchers.<Set<Integer>> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    private KeyEvent keyEvent(int keyCode, int id) {
        return new KeyEvent(comp, id, -1L, 0, keyCode, ' ');
    }

    private void fireKeyEvent(KeyEvent event) {
        for (KeyListener listener : comp.getKeyListeners()) {
            listener.keyTyped(event);
        }
    }
}
