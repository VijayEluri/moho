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

package com.voxeo.moho.event;

/**
 * This event is fired when a {@link com.voxeo.moho.media.Recording Recording} is completed.
 * 
 * @author wchen
 *
 */
public interface RecordCompleteEvent<T extends EventSource> extends MediaCompleteEvent<T> {
  public enum Cause {
    TIMEOUT, ERROR, SILENCE, UNKNOWN, CANCEL, INI_TIMEOUT, DISCONNECT
  }
//test
  /**
   * @return the cause of the completion.
   */
  Cause getCause();

  /**
   * @return the length of the recording in milliseconds without omitted silence.
   */
  long getDuration();
  
  /**
   * get error description text.
   * @return
   */
  String getErrorText();
  
  Exception getException();
}
