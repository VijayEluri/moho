package com.voxeo.moho.media.dialect;

import java.net.URI;
import java.util.Map;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.Parameters;
import javax.media.mscontrol.Qualifier;
import javax.media.mscontrol.Value;
import javax.media.mscontrol.mediagroup.RecorderEvent;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.RTC;
import javax.media.mscontrol.resource.ResourceEvent;

import com.voxeo.moho.event.InputCompleteEvent.Cause;
import com.voxeo.moho.media.BeepParameters;
import com.voxeo.moho.media.EnergyParameters;
import com.voxeo.moho.media.InputMode;
import com.voxeo.moho.media.input.SignalGrammar.Signal;

public interface MediaDialect {

  void setSpeechLanguage(Parameters parameters, String value);

  void setSpeechTermChar(Parameters parameters, Character value);

  void setSpeechInputMode(Parameters parameters, InputMode value);

  void setTextToSpeechVoice(Parameters parameters, String value);

  void setTextToSpeechLanguage(Parameters parameters, String value);

  void setBeepOnConferenceEnter(Parameters parameters, Boolean value);

  void setBeepOnConferenceExit(Parameters parameters, Boolean value);

  void setDtmfHotwordEnabled(Parameters parameters, Boolean value);

  void setDtmfTypeaheadEnabled(Parameters parameters, Boolean value);

  void setConfidence(Parameters parameters, float value);

  void setSpeechIncompleteTimeout(Parameters parameters, long peechIncompleteTimeout);

  void setSpeechCompleteTimeout(Parameters parameters, long peechCompleteTimeout);

  void setMixerName(Parameters params, String name);
  
  //call record related
  void setCallRecordFileFormat(Parameters params, Value value);
  
  void setCallRecordAudioCodec(Parameters params, Value value);
  
  void startCallRecord(NetworkConnection nc, URI recordURI, RTC[] rtc, Parameters optargs, CallRecordListener listener) throws MsControlException;
  
  void stopCallRecord(NetworkConnection nc);
  
  void pauseCallRecord(NetworkConnection nc);
  
  void resumeCallRecord(NetworkConnection nc);

  int getCallRecordDuration(ResourceEvent event);

  //call record related over
  
  void enableRecorderPromptCompleteEvent(Parameters params, boolean enable);
  
  void enableDetectorPromptCompleteEvent(Parameters params, boolean enable);

  boolean isPromptCompleteEvent(RecorderEvent event);
  
  boolean isPromptCompleteEvent(SignalDetectorEvent event);

  void setDtmfPassThrough(NetworkConnection nc, boolean passThrough);
  
  Map<String, String> getSISlots(SignalDetectorEvent event);
  
  void setAutoReset(Parameters params, boolean autoreset);

  // translate Signal into 309 extended VoxeoSignalConstants
  Value getSignalConstants(Signal signal);

  void setEnergyParameters(Parameters params, EnergyParameters energy);

  void setBeepParameters(Parameters params, BeepParameters beep);
  
  Cause getInputCompleteEventCause(Qualifier qualifier);
  
  void setIgnorePromptFailure(Parameters params, boolean ignore);
}
