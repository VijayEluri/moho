/**
 * Copyright 2010-2011 Voxeo Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.voxeo.moho.media;

import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.event.InputCompleteEvent;

/**
 * Input is a {@link java.util.Future Future} that holds the result of an 
 * {@link com.voxeo.moho.media.input.InputCommand InputCommand}. 
 * 
 * @author wchen
 *
 */
public interface Input<T extends EventSource> extends MediaOperation<T, InputCompleteEvent<T>> {

}
