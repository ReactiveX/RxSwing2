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
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Predicate;
import rx.observables.SwingObservable;
import rx.schedulers.SwingScheduler;

public enum ComponentEventSource {
	; // no instances

	/**
	 * @see rx.observables.SwingObservable#fromComponentEvents
	 */
	public static Observable<ComponentEvent> fromComponentEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<ComponentEvent>() {

			@Override
			public void subscribe(ObservableEmitter<ComponentEvent> subscriber) throws Exception {
				final ComponentListener listener = new ComponentListener() {
					@Override
					public void componentHidden(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentMoved(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentResized(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentShown(ComponentEvent event) {
						subscriber.onNext(event);
					}
				};
				component.addComponentListener(listener);
				subscriber.setDisposable(Disposables.fromAction(() -> {
					component.removeComponentListener(listener);
				}));

			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see SwingObservable#fromResizing
	 */
	public static Observable<Dimension> fromResizing(final Component component) {
		return fromComponentEventsOf(component).filter(Predicates.RESIZED).map(event -> event.getComponent().getSize());
	}

	/**
	 * Predicates that help with filtering observables for specific component events.
	 */
	public enum Predicates implements Predicate<java.awt.event.ComponentEvent> {
		RESIZED(ComponentEvent.COMPONENT_RESIZED), HIDDEN(ComponentEvent.COMPONENT_HIDDEN), MOVED(ComponentEvent.COMPONENT_MOVED), SHOWN(
				ComponentEvent.COMPONENT_SHOWN);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}


		@Override
		public boolean test(ComponentEvent event) throws Exception {
			return event.getID() == id;
		}
	}
}
