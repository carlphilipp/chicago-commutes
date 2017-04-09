/**
 * Copyright 2017 Carl-Philipp Harmant
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.exception;

import android.support.annotation.NonNull;

/**
 * Parser exception
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class ParserException extends TrackerException {

    /**
     * Serializable
     **/
    private static final long serialVersionUID = 1L;

    /**
     * The constructor
     *
     * @param e       the exception
     */
    public ParserException(@NonNull final Exception e) {
        super(TrackerException.ERROR, e);
    }

    public ParserException(@NonNull final String text) {
        super(text);
    }
}
