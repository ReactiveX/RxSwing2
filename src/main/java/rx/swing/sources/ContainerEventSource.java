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

import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Predicate;
import rx.schedulers.SwingScheduler;

public enum ContainerEventSource {
	; // no instances

	/**
	 * @see rx.observables.SwingObservable#fromContainerEvents
	 */
	public static Observable<ContainerEvent> fromContainerEventsOf(final Container container) {
		return Observable.create(new ObservableOnSubscribe<ContainerEvent>() {

			@Override
			public void subscribe(ObservableEmitter<ContainerEvent> subscriber) throws Exception {
				final ContainerListener listener = new ContainerListener() {
					@Override
					public void componentRemoved(ContainerEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentAdded(ContainerEvent event) {
						subscriber.onNext(event);
					}
				};
				container.addContainerListener(listener);
				subscriber.setDisposable(Disposables.fromAction(() -> {

					container.removeContainerListener(listener);

				}));

			}
		}).subscribeOn(SwingScheduler.getInstance()).observeOn(SwingScheduler.getInstance());
	}

	public static enum Predicates implements Predicate<ContainerEvent> {
		COMPONENT_ADDED(ContainerEvent.COMPONENT_ADDED), COMPONENT_REMOVED(ContainerEvent.COMPONENT_REMOVED);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}

		@Override
		public boolean test(ContainerEvent event) {
			return event.getID() == id;
		}
	}
}
