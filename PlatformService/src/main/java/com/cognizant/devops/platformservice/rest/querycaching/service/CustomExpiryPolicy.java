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

import java.time.Duration;

import java.util.function.Supplier;

import org.ehcache.expiry.ExpiryPolicy;

public class CustomExpiryPolicy<K, V extends EhcacheValue<?>> implements ExpiryPolicy<K, V> {

    public CustomExpiryPolicy() {    	
    }

    @Override
    public Duration getExpiryForCreation(K theKey, V theValue) {
       return theValue.getTimeToLiveDuration();
    }

    @Override
    public Duration getExpiryForAccess(K theKey, Supplier<? extends V> theValue) {
        return null;
    }

    @Override
    public Duration getExpiryForUpdate(K theKey, Supplier<? extends V> theOldValue, V theNewValue) {
         return theNewValue.getTimeToLiveDuration();
       }
    }
