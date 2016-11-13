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
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Predicate;
import rx.schedulers.SwingScheduler;

public enum HierarchyEventSource {
	; // no instances

	/**
	 * @see rx.observables.SwingObservable#fromHierachyEvents
	 */
	public static Observable<HierarchyEvent> fromHierarchyEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<HierarchyEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<HierarchyEvent> subscriber) {
				final HierarchyListener hiearchyListener = new HierarchyListener() {
					@Override
					public void hierarchyChanged(HierarchyEvent e) {
						subscriber.onNext(e);
					}
				};
				component.addHierarchyListener(hiearchyListener);
				subscriber.setDisposable(Disposables.fromAction(() -> {

					component.removeHierarchyListener(hiearchyListener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see rx.observables.SwingObservable#fromHierachyBoundsEvents
	 */
	public static Observable<HierarchyEvent> fromHierarchyBoundsEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<HierarchyEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<HierarchyEvent> subscriber) {
				final HierarchyBoundsListener hiearchyBoundsListener = new HierarchyBoundsListener() {
					@Override
					public void ancestorMoved(HierarchyEvent e) {
						subscriber.onNext(e);
					}

					@Override
					public void ancestorResized(HierarchyEvent e) {
						subscriber.onNext(e);
					}
				};
				component.addHierarchyBoundsListener(hiearchyBoundsListener);
				subscriber.setDisposable(Disposables.fromAction(() -> {

					component.removeHierarchyBoundsListener(hiearchyBoundsListener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	public static enum Predicates implements Predicate<HierarchyEvent> {
		ANCESTOR_RESIZED(HierarchyEvent.ANCESTOR_RESIZED), ANCESTOR_MOVED(HierarchyEvent.ANCESTOR_MOVED);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}

		@Override
		public boolean test(HierarchyEvent event) {
			return event.getID() == id;
		}
	}
}
