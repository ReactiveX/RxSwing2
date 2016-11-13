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

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.BiFunction;
import rx.schedulers.SwingScheduler;

public enum KeyEventSource { ; // no instances

    /**
     * @see rx.observables.SwingObservable#fromKeyEvents(Component)
     */
    public static Observable<KeyEvent> fromKeyEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<KeyEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<KeyEvent> subscriber) {
                final KeyListener listener = new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void keyReleased(KeyEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void keyTyped(KeyEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addKeyListener(listener);

                subscriber.setDisposable(Disposables.fromAction(() ->  {
                  
                        component.removeKeyListener(listener);
                    
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see rx.observables.SwingObservable#fromPressedKeys(Component)
     */
    public static Observable<Set<Integer>> currentlyPressedKeysOf(Component component) {
        class CollectKeys implements BiFunction<Set<Integer>, KeyEvent, Set<Integer>>{
           

			@Override
			public Set<Integer> apply(Set<Integer> t1, KeyEvent event) throws Exception {
				  Set<Integer> afterEvent = new HashSet<Integer>(t1);
	                switch (event.getID()) {
	                    case KeyEvent.KEY_PRESSED:
	                        afterEvent.add(event.getKeyCode());
	                        break;
	                        
	                    case KeyEvent.KEY_RELEASED:
	                        afterEvent.remove(event.getKeyCode());
	                        break;
	                      
	                    default: // nothing to do
	                }
	                return afterEvent;
			}
        }
        
        Observable<KeyEvent> filteredKeyEvents = fromKeyEventsOf(component).filter(event -> event.getID() == KeyEvent.KEY_PRESSED || event.getID() == KeyEvent.KEY_RELEASED);
        
        return filteredKeyEvents.scan(Collections.<Integer>emptySet(), new CollectKeys());
    }
    
}
