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

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import rx.schedulers.SwingScheduler;

public enum ListSelectionEventSource { ; // no instances

	/**
	 * @see rx.observables.SwingObservable#fromListSelectionEvents(ListSelectionModel)
	 */
	public static Observable<ListSelectionEvent> fromListSelectionEventsOf(final ListSelectionModel listSelectionModel) {
		return Observable.create(new ObservableOnSubscribe<ListSelectionEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<ListSelectionEvent> subscriber) {
				final ListSelectionListener listener = new ListSelectionListener() {
					@Override
					public void valueChanged(final ListSelectionEvent event) {
						subscriber.onNext(event);
					}

				};
				listSelectionModel.addListSelectionListener(listener);
				subscriber.setDisposable(Disposables.fromAction(() ->  {
				
						listSelectionModel.removeListSelectionListener(listener);
				
				}));
			}
		}).subscribeOn(SwingScheduler.getInstance())
				.unsubscribeOn(SwingScheduler.getInstance());
	}
}
