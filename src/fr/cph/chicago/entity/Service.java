/**
 * Copyright 2016 Carl-Philipp Harmant
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

package fr.cph.chicago.entity;

import lombok.Data;

/**
 * Service entity
 *
 * @author Carl-Philipp Harmant
 * @version 1
 */
@Data
public class Service {
    /**
     * The service type
     **/
    private String type;
    /**
     * The description type
     **/
    private String typeDescription;
    /**
     * The name
     **/
    private String name;
    /**
     * The id
     **/
    private String id;
    /**
     * The back color
     **/
    private String backColor;
    /**
     * The text color
     **/
    private String textColor;
    /**
     * The url
     **/
    private String url;
}
