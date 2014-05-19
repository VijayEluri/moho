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

public class GenericDialect implements MediaDialect {

  @Override
  public void setBeepOnConferenceEnter(Parameters parameters, Boolean value) {
  }

  @Override
  public void setBeepOnConferenceExit(Parameters parameters, Boolean value) {
  }

  @Override
  public void setSpeechInputMode(Parameters parameters, InputMode value) {
  }

  @Override
  public void setSpeechLanguage(Parameters parameters, String value) {
  }

  @Override
  public void setSpeechTermChar(Parameters parameters, Character value) {
  }

  @Override
  public void setTextToSpeechVoice(Parameters parameters, String value) {
  }

  @Override
  public void setDtmfHotwordEnabled(Parameters parameters, Boolean value) {
  }

  @Override
  public void setDtmfTypeaheadEnabled(Parameters parameters, Boolean value) {
  }

  @Override
  public void setConfidence(Parameters parameters, float value) {
  }

  @Override
  public void setTextToSpeechLanguage(Parameters parameters, String value) {
  }

  @Override
  public void setSpeechIncompleteTimeout(Parameters parameters, long peechIncompleteTimeout) {
  }

  @Override
  public void setSpeechCompleteTimeout(Parameters parameters, long peechCompleteTimeout) {
  }

  @Override
  public void setMixerName(Parameters params, String name) {
  }

  @Override
  public void setCallRecordFileFormat(Parameters params, Value value) {

  }

  @Override
  public void setCallRecordAudioCodec(Parameters params, Value value) {

  }

  @Override
  public void startCallRecord(NetworkConnection nc, URI recordURI, RTC[] rtc, Parameters optargs,
      CallRecordListener listener) throws MsControlException {

  }

  @Override
  public void stopCallRecord(NetworkConnection nc) {

  }

  @Override
  public int getCallRecordDuration(ResourceEvent event) {
    return 0;
  }

  @Override
  public void pauseCallRecord(NetworkConnection nc) {

  }

  @Override
  public void resumeCallRecord(NetworkConnection nc) {

  }

  @Override
  public boolean isPromptCompleteEvent(RecorderEvent event) {
    return false;
  }

  @Override
  public boolean isPromptCompleteEvent(SignalDetectorEvent event) {
    return false;
  }

  @Override
  public void enableRecorderPromptCompleteEvent(Parameters params, boolean enable) {

  }

  @Override
  public void enableDetectorPromptCompleteEvent(Parameters params, boolean enable) {

  }

  @Override
  public void setDtmfPassThrough(NetworkConnection nc, boolean passThrough) {

  }

  @Override
  public Map<String, String> getSISlots(SignalDetectorEvent event) {
    return null;
  }

  @Override
  public Value getSignalConstants(Signal signal) {
    return null;
  }

  @Override
  public void setAutoReset(Parameters params, boolean autoreset) {
  }

  @Override
  public void setEnergyParameters(Parameters params, EnergyParameters energy) {
  }

  @Override
  public void setBeepParameters(Parameters params, BeepParameters beep) {
  }

  @Override
  public Cause getInputCompleteEventCause(Qualifier qualifier) {
    return null;
  }

  @Override
  public void setIgnorePromptFailure(Parameters params, boolean ignore) {

  }

  @Override
  public void init() {
    
  }

  @Override
  public void destroy() {
    
  }
}
