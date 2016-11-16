/**
 * Copyright 2015 Netflix, Inc.
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import rx.schedulers.SwingScheduler;

public enum PropertyChangeEventSource { ; // no instances

    public static Observable<PropertyChangeEvent> fromPropertyChangeEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<PropertyChangeEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<PropertyChangeEvent> subscriber) {
                final PropertyChangeListener listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addPropertyChangeListener(listener);
                subscriber.setDisposable(Disposables.fromAction(() ->  {
                        component.removePropertyChangeListener(listener);
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
