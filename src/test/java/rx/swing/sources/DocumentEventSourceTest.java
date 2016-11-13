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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import rx.observables.SwingObservable;

public class DocumentEventSourceTest {

    @Test
    public void testObservingDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                @SuppressWarnings("unchecked")
                Consumer<DocumentEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final JEditorPane pane = new JEditorPane();
                // Document must by StyledDocument to test changeUpdate
                pane.setContentType("text/html");
                final Document doc = (HTMLDocument) pane.getDocument();

                final Disposable subscription = DocumentEventSource.fromDocumentEventsOf(doc)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<DocumentEvent>any());
                verify(error, never()).accept(Matchers.<Throwable>any());
                verify(complete, never()).run();

                // test insertUpdate
                insertStringToDocument(doc, 0, "test text");
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
                verifyNoMoreInteractions(action, error, complete);

                // test removeUpdate
                removeFromDocument(doc, 0, 5);
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.REMOVE)));
                verifyNoMoreInteractions(action, error, complete);

                // test changeUpdate
                Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                ((HTMLDocument) doc).setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.CHANGE)));
                verifyNoMoreInteractions(action, error, complete);

                // test unsubscribe
                subscription.dispose();
                insertStringToDocument(doc, 0, "this should be ignored");
                verifyNoMoreInteractions(action, error, complete);
            }

        }).awaitTerminal();
    }

    @Test
    public void testObservingFilteredDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                @SuppressWarnings("unchecked")
                Consumer<DocumentEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final Document doc = new JEditorPane().getDocument();

                // filter only INSERT, others will be ignored
                final Set<DocumentEvent.EventType> filteredTypes
                        = new HashSet<DocumentEvent.EventType>(Arrays.asList(DocumentEvent.EventType.INSERT));
                final Disposable subscription = SwingObservable.fromDocumentEvents(doc, filteredTypes)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Matchers.<DocumentEvent>any());
                verify(error, never()).accept(Matchers.<Throwable>any());
                verify(complete, never()).run();

                // test insertUpdate
                insertStringToDocument(doc, 0, "test text");
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
                verifyNoMoreInteractions(action, error, complete);

                // test removeUpdate
                removeFromDocument(doc, 0, 5);
                // removeUpdate should be ignored
                verifyNoMoreInteractions(action, error, complete);

                // test unsubscribe
                subscription.dispose();
                insertStringToDocument(doc, 0, "this should be ignored");
                verifyNoMoreInteractions(action, error, complete);
            }

        }).awaitTerminal();
    }

    private static Matcher<DocumentEvent> documentEventMatcher(final DocumentEvent.EventType eventType) {
        return new ArgumentMatcher<DocumentEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof DocumentEvent)) {
                    return false;
                }

                return ((DocumentEvent) argument).getType().equals(eventType);
            }
        };
    }

    private static void insertStringToDocument(Document doc, int offset, String text) {
        try {
            doc.insertString(offset, text, null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void removeFromDocument(Document doc, int offset, int length) {
        try {
            doc.remove(offset, length);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
