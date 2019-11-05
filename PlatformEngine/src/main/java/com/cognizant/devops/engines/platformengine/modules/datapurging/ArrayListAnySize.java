/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.engines.platformengine.modules.datapurging;

import java.util.ArrayList;


public class ArrayListAnySize<E> extends ArrayList<E>{

	/**
	 * This class is to override add method of ArrayList
	 * which allows to add null values at those indexes incrementally
	 * when index is greater than size()of the ArrayList
	 */
	private static final long serialVersionUID = -5228973292447276083L;

	@Override
    public void add(int index, E element){
        if(index >= 0 && index <= size()){
            super.add(index, element);
            return;
        }
        int insertNulls = index - size();
        for(int i = 0; i < insertNulls; i++){
            super.add(null);
        }
        super.add(element);        
    }
}
