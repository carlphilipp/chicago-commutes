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

package fr.cph.chicago.entity;

/**
 * Service entity
 * 
 * @author Carl-Philipp Harmant
 * @version 1
 */
public class Service {
	/** The service type **/
	private String type;
	/** The description type **/
	private String typeDescription;
	/** The name **/
	private String name;
	/** The id **/
	private String id;
	/** The back color **/
	private String backColor;
	/** The text color **/
	private String textColor;
	/** The url **/
	private String url;

	/**
	 * 
	 * @return
	 */
	public final String getType() {
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public final void setType(String type) {
		this.type = type;
	}

	/**
	 * 
	 * @return
	 */
	public final String getTypeDescription() {
		return typeDescription;
	}

	/**
	 * 
	 * @param typeDescription
	 */
	public final void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	/**
	 * 
	 * @return
	 */
	public final String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return
	 */
	public final String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public final void setId(String id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 */
	public final String getBackColor() {
		return backColor;
	}

	/**
	 * 
	 * @param backColor
	 */
	public final void setBackColor(String backColor) {
		this.backColor = backColor;
	}

	/**
	 * 
	 * @return
	 */
	public final String getTextColor() {
		return textColor;
	}

	/**
	 * 
	 * @param textColor
	 */
	public final void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	/**
	 * 
	 * @return
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * 
	 * @param url
	 */
	public final void setUrl(String url) {
		this.url = url;
	}

}
