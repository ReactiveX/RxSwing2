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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import rx.schedulers.SwingScheduler;

public enum AbstractButtonSource { ; // no instances

    /**
     * @see rx.observables.SwingObservable#fromButtonAction
     */
    public static Observable<ActionEvent> fromActionOf(final AbstractButton button) {
    	
        return Observable.create(new ObservableOnSubscribe<ActionEvent>() {
          

			@Override
			public void subscribe(final ObservableEmitter<ActionEvent> subscriber) throws Exception {
				 final ActionListener listener = new ActionListener() {
	                    @Override
	                    public void actionPerformed(ActionEvent e) {
	                        subscriber.onNext(e);
	                    }
	                };
	                button.addActionListener(listener);
	                subscriber.setDisposable(Disposables.fromAction(() -> {
	                	button.removeActionListener(listener);
	                }));
	                
	               
				
			}
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
