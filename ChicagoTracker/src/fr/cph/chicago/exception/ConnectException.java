/**
 * Copyright 2014 Carl-Philipp Harmant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.cph.chicago.exception;

/**
 * Connect exception
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class ConnectException extends TrackerException {

	/** Serializable **/
	private static final long serialVersionUID = 1L;

	/** The error string **/
	public static final String ERROR = "Can't connect, please check your connection";

	/**
	 * The constructor
	 * 
	 * @param message
	 *            the message
	 * @param e
	 *            the exception
	 */
	public ConnectException(String message, Exception e) {
		super(message, e);
	}

}
