/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.cognizant.devops.platformservice.rest.querycaching.service;

import java.io.Serializable;
import java.time.Duration;

public class EhcacheValue<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient  T jsonObject;

    private long timeToLive;

    public EhcacheValue(T theObject, Duration theDuration) {
        jsonObject = theObject;
        timeToLive = theDuration.getSeconds();
    }

    public Duration getTimeToLiveDuration() {
        return Duration.ofSeconds(timeToLive);
    }

    public T getObject() {
        return jsonObject;
    }

    public void setObject(T theObject) {
        jsonObject = theObject;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long theTimeToLive) {
        timeToLive = theTimeToLive;
    }

}

