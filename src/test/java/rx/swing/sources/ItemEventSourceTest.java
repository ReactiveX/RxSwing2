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

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.ItemEvent;

import javax.swing.AbstractButton;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import rx.observables.SwingObservable;

public class ItemEventSourceTest
{
    @Test
    public void testObservingItemEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = ItemEventSource.fromItemEventsOf(button).subscribe(action,
                        error, complete);

                verify(action, never()).accept(Matchers.<ItemEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));

                button.testSelection();
                verify(action, times(2)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, times(2)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredBySelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception {
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = SwingObservable.fromItemSelectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<ItemEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, never()).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, never()).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredByDeSelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception {
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = SwingObservable.fromItemDeselectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<ItemEvent> any());
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, never()).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, never()).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).accept(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(Matchers.<Throwable> any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    private Matcher<ItemEvent> itemEventMatcher(final int eventType)
    {
        return new ArgumentMatcher<ItemEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (argument.getClass() !=  ItemEvent.class)
                    return false;
                
                return ((ItemEvent) argument).getStateChange() == eventType;
            }
        };
    }
}
