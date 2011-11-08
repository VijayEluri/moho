package com.voxeo.moho.remote.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.media.mscontrol.mediagroup.MediaGroup;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import com.rayo.client.XmppException;
import com.rayo.client.xmpp.stanza.IQ;
import com.rayo.client.xmpp.stanza.Presence;
import com.rayo.core.verb.Choices;
import com.rayo.core.verb.Record;
import com.rayo.core.verb.Ssml;
import com.rayo.core.verb.VerbRef;
import com.voxeo.moho.MediaException;
import com.voxeo.moho.MediaService;
import com.voxeo.moho.event.EventSource;
import com.voxeo.moho.media.Input;
import com.voxeo.moho.media.Output;
import com.voxeo.moho.media.Prompt;
import com.voxeo.moho.media.Recording;
import com.voxeo.moho.media.input.Grammar;
import com.voxeo.moho.media.input.InputCommand;
import com.voxeo.moho.media.input.SimpleGrammar;
import com.voxeo.moho.media.output.AudibleResource;
import com.voxeo.moho.media.output.AudioURIResource;
import com.voxeo.moho.media.output.OutputCommand;
import com.voxeo.moho.media.output.TextToSpeechResource;
import com.voxeo.moho.media.record.RecordCommand;
import com.voxeo.moho.remote.impl.media.InputImpl;
import com.voxeo.moho.remote.impl.media.OutputImpl;
import com.voxeo.moho.remote.impl.media.PromptImpl;
import com.voxeo.moho.remote.impl.media.RecordingImpl;

public abstract class MediaServiceSupport<T extends EventSource> extends ParticipantImpl implements MediaService<T> {
  
  protected static final Logger LOG = Logger.getLogger(MediaServiceSupport.class);

  protected MohoRemoteImpl _mohoRemote;
  
  protected Map<String, JointImpl> _joints = new ConcurrentHashMap<String, JointImpl>();

  protected Map<String, UnJointImpl> _unjoints = new ConcurrentHashMap<String, UnJointImpl>();
  
  protected Map<String, RayoListener> _componentListeners = new ConcurrentHashMap<String, RayoListener>();
  
  public MediaServiceSupport(MohoRemoteImpl mohoRemote) {
    _mohoRemote = mohoRemote;
    _dispatcher.setExecutor(_mohoRemote.getExecutor(), true);
  }
  
  public MohoRemoteImpl getMohoRemote() {
    return _mohoRemote;
  }
  
  public void removeComponentListener(String id) {
    _componentListeners.remove(id);
  }

  public void addComponentListener(String id, RayoListener listener) {
    _componentListeners.put(id, listener);
  }
  
  @Override
  public Output<T> output(String text) throws MediaException {
    OutputImpl<T> output = null;
    try {
      VerbRef verbRef = _mohoRemote.getRayoClient().output(text, this.getId());

      output = new OutputImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return output;
  }

  @Override
  public Output<T> output(URI media) throws MediaException {
    OutputImpl<T> output = null;
    try {
      VerbRef verbRef = _mohoRemote.getRayoClient().output(media, this.getId());

      output = new OutputImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return output;
  }

  @Override
  public Output<T> output(OutputCommand output) throws MediaException {
    OutputImpl<T> outputFuture = null;
    try {
      com.rayo.core.verb.Output rayoOutput = new com.rayo.core.verb.Output();
      if (output.getStartingOffset() > 0) {
        rayoOutput.setStartOffset(Duration.standardSeconds(output.getStartingOffset() / 1000));
      }
      if (output.isStartInPausedMode()) {
        rayoOutput.setStartPaused(true);
      }
      if (output.getRepeatInterval() > 0) {
        rayoOutput.setRepeatInterval(Duration.standardSeconds(output.getRepeatInterval() / 1000));
      }
      if (output.getRepeatTimes() > 0) {
        rayoOutput.setRepeatTimes(output.getRepeatTimes());
      }
      if (output.getMaxtime() > 0) {
        rayoOutput.setMaxTime(Duration.standardSeconds(output.getMaxtime() / 1000));
      }
      if (output.getVoiceName() != null) {
        rayoOutput.setVoice(output.getVoiceName());
      }
      VerbRef verbRef = null;
      OutputCommand next = null;
      if (output.getAudibleResources() != null && output.getAudibleResources().length > 0) {
        AudibleResource ar = output.getAudibleResources()[0];
        if (ar instanceof TextToSpeechResource) {
          rayoOutput.setPrompt(new Ssml(((TextToSpeechResource) ar).getText()));
        }
        else  {
          verbRef = _mohoRemote.getRayoClient().output(ar.toURI(), this.getId());
        }
        
        if (output.getAudibleResources().length > 1) {
          next = (OutputCommand) output.clone();
          next.setAudibleResource(Arrays.copyOfRange(output.getAudibleResources(), 1, output.getAudibleResources().length));
        }
      }
      if (verbRef == null) {
        verbRef = _mohoRemote.getRayoClient().output(rayoOutput, this.getId());
      }

      outputFuture = new OutputImpl<T>(verbRef, next, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return outputFuture;
  }

  @Override
  public Prompt<T> prompt(String text, String grammar, int repeat) throws MediaException {
    final OutputCommand output = text == null ? null : new OutputCommand(new TextToSpeechResource(text));
    final InputCommand input = grammar == null ? null : new InputCommand(new SimpleGrammar(grammar));
    return prompt(output, input, repeat);
  }

  @Override
  public Prompt<T> prompt(URI media, String grammar, int repeat) throws MediaException {
    final OutputCommand output = media == null ? null : new OutputCommand(new AudioURIResource(media));
    final InputCommand input = grammar == null ? null : new InputCommand(new SimpleGrammar(grammar));
    return prompt(output, input, repeat);
  }

  @Override
  public Prompt<T> prompt(OutputCommand output, InputCommand input, int repeat) throws MediaException {
    PromptImpl<T> prompt = new PromptImpl<T>(_mohoRemote.getExecutor());
    if (output != null) {
      for (int i = 0; i < repeat + 1; i++) {
        prompt.setOutput(output(output));
      }
    }
    if (input != null) {
      prompt.setInput(input(input));
    }

    return prompt;
  }

  @Override
  public Input<T> input(String grammar) throws MediaException {
    InputImpl<T> input = null;
    try {
      Choices choice = new Choices();
      choice.setContent(grammar);
      choice.setContentType(Choices.VOXEO_GRAMMAR);

      List<Choices> list = new ArrayList<Choices>(1);
      list.add(choice);
      com.rayo.core.verb.Input command = new com.rayo.core.verb.Input();
      command.setCallId(this.getId());
      command.setGrammars(list);

      VerbRef verbRef = _mohoRemote.getRayoClient().input(command, this.getId());
      input = new InputImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return input;
  }

  @Override
  public Input<T> input(InputCommand inputCommand) throws MediaException {
    InputImpl<T> input = null;
    try {
      Grammar[] grammars = inputCommand.getGrammars();
      List<Choices> list = new ArrayList<Choices>(grammars.length);
      for (Grammar grammar : grammars) {
        Choices choice = new Choices();
        if (grammar.getText() != null) {
          choice.setContent(grammar.getText());
          choice.setContentType(Choices.VOXEO_GRAMMAR);
        }
        else {
          choice.setUri(grammar.getUri());
        }
        list.add(choice);
      }

      com.rayo.core.verb.Input command = new com.rayo.core.verb.Input();
      command.setCallId(this.getId());
      command.setGrammars(list);
      if (inputCommand.getInitialTimeout() > 0) {
        command.setInitialTimeout(Duration.standardSeconds(inputCommand.getInitialTimeout() / 1000));
      }
      if (inputCommand.getInterDigitsTimeout() > 0) {
        command.setInterDigitTimeout(Duration.standardSeconds(inputCommand.getInterDigitsTimeout()));
      }
      if (inputCommand.getTerminator() != null) {
        command.setTerminator(inputCommand.getTerminator());
      }
      if (inputCommand.getMinConfidence() > 0) {
        command.setMinConfidence(inputCommand.getMinConfidence());
      }
      if (inputCommand.getRecognizer() != null) {
        command.setRecognizer(inputCommand.getRecognizer());
      }
      if (inputCommand.getInputMode() != null) {
        if (inputCommand.getInputMode() == com.voxeo.moho.media.InputMode.DTMF) {
          command.setMode(com.rayo.core.verb.InputMode.DTMF);
        }
        else if (inputCommand.getInputMode() == com.voxeo.moho.media.InputMode.SPEECH) {
          command.setMode(com.rayo.core.verb.InputMode.VOICE);
        }
        else {
          command.setMode(com.rayo.core.verb.InputMode.ANY);
        }
      }
      if (inputCommand.getSensitivity() > 0) {
        command.setSensitivity(inputCommand.getSensitivity());
      }

      VerbRef verbRef = _mohoRemote.getRayoClient().input(command, this.getId());
      input = new InputImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return input;
  }

  @Override
  public Recording<T> record(URI recordURI) throws MediaException {
    Recording<T> recording = null;
    try {
      Record record = new Record();
      record.setTo(recordURI);
      record.setCallId(this.getId());
      VerbRef verbRef = _mohoRemote.getRayoClient().record(record, this.getId());

      recording = new RecordingImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return recording;
  }

  @Override
  public Recording<T> record(RecordCommand command) throws MediaException {
    Recording<T> recording = null;
    try {
      Record record = new Record();
      record.setTo(command.getRecordURI());
      record.setCallId(this.getId());

      if (command.getFinalTimeout() > 0) {
        record.setFinalTimeout(Duration.standardSeconds(command.getFinalTimeout() / 1000));
      }
      if (command.getFileFormat() != null) {
        record.setFormat(command.getFileFormat().toString());
      }
      if (command.getMaxDuration() > 0) {
        record.setMaxDuration(Duration.standardSeconds(command.getMaxDuration() / 1000));
      }
      if (command.isStartBeep()) {
        record.setStartBeep(true);
      }
      if (command.isStartInPausedMode()) {
        record.setStartPaused(true);
      }
      if (command.getInitialTimeout() > 0) {
        record.setInitialTimeout(Duration.standardSeconds(command.getInitialTimeout() / 1000));
      }
      VerbRef verbRef = _mohoRemote.getRayoClient().record(record, this.getId());

      recording = new RecordingImpl<T>(verbRef, this, (T) this);
    }
    catch (XmppException e) {
      LOG.error("", e);
      throw new MediaException(e);
    }
    return recording;
  }
  
  @Override
  public void onRayoEvent(JID from, Presence presence) {
    RayoListener listener = _componentListeners.get(from.getResource());
    if (listener != null) {
      listener.onRayoEvent(from, presence);
    }
  }
  
  @Override
  public void onRayoCommandResult(JID from, IQ iq) {
    if (from.getResource() != null) {
      RayoListener listener = _componentListeners.get(from.getResource());
      if (listener != null) {
        listener.onRayoCommandResult(from, iq);
      }
    }
    else {

    }
  }

  @Override
  public MediaGroup getMediaGroup() {
    throw new UnsupportedOperationException(Constants.unsupported_operation);
  }

}
