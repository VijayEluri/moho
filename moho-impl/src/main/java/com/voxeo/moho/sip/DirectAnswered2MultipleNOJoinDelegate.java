package com.voxeo.moho.sip;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.sdp.SessionDescription;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

import com.voxeo.moho.Participant.JoinType;
import com.voxeo.moho.event.CallCompleteEvent;
import com.voxeo.moho.event.JoinCompleteEvent;
import com.voxeo.moho.sip.SIPCall.State;

// NOTE that moho application should process redirect event.
public class DirectAnswered2MultipleNOJoinDelegate extends JoinDelegate {
  private static final Logger LOG = Logger.getLogger(DirectAnswered2MultipleNOJoinDelegate.class);

  protected List<SIPCallImpl> candidateCalls = new LinkedList<SIPCallImpl>();;

  protected boolean _suppressEarlyMedia;

  protected SipServletResponse _response;

  protected Object syncLock = new Object();

  protected DirectAnswered2MultipleNOJoinDelegate(JoinType type, Direction direction, SIPCallImpl call1,
      boolean suppressEarlyMedia, List<SIPCallImpl> others) {
    _call1 = call1;
    _suppressEarlyMedia = suppressEarlyMedia;
    _direction = direction;
    _joinType = type;
    candidateCalls.addAll(others);
  }

  @Override
  public void doJoin() throws Exception {
    super.doJoin();
    Exception exception = null;
    int exceptionCount = 0;
    for (SIPCallImpl call : candidateCalls) {
      try {
        ((SIPOutgoingCall) call).setContinueRouting(_call1);
        SIPHelper.remove100relSupport(call.getSipInitnalRequest());

        if (_suppressEarlyMedia) {
          SessionDescription mockSDP = _call1.createSendonlySDP(_call1.getRemoteSdp());
          ((SIPOutgoingCall) call).call(mockSDP.toString().getBytes("iso8859-1"), _call1.getSipSession()
              .getApplicationSession());
        }
        else {
          ((SIPOutgoingCall) call).call(null, _call1.getSipSession().getApplicationSession());
        }
      }
      catch (Exception ex) {
        exceptionCount++;
        exception = ex;
        LOG.error("Exception when trying call " + call, ex);
      }
    }

    if (exceptionCount == candidateCalls.size()) {
      throw exception;
    }
  }

  @Override
  protected void doInviteResponse(SipServletResponse res, SIPCallImpl call, Map<String, String> headers)
      throws Exception {
    try {
      synchronized (syncLock) {
        if (SIPHelper.isProvisionalResponse(res)) {
          SIPHelper.trySendPrack(res);
          return;
        }
        else if (SIPHelper.isSuccessResponse(res)) {
          if (_call1.equals(call)) {
            res.createAck().send();
            SipServletRequest ack2 = _response.createAck();

            SIPHelper.copyContent(res, ack2);
            ack2.send();
            successJoin();
          }
          else {
            if (_response != null && !call.equals(_call2)) {
              res.createAck().send();
              // receive second success response, ignore it.
              return;
            }

            if (_response == null) {
              _response = res;
              _call2 = call;
              candidateCalls.remove(call);
              disconnectCalls(candidateCalls);
              _call2.setSIPCallState(State.ANSWERED);

              if (_suppressEarlyMedia) {
                res.createAck().send();
                // re-INVITE call2 with real SDP
                _call2.reInviteRemote(null, null, null);
              }
              else {
                reInviteCall(_call1, res);
              }
            }
            else {
              _response = res;
              reInviteCall(_call1, res);
            }
          }
        }
        else if (SIPHelper.isErrorResponse(res)) {
          if (_call1.equals(call)) {
            LOG.warn("re-INVITE call1 got error response, failed join on delegate " + this);
            done(getJoinCompleteCauseByResponse(res), getExceptionByResponse(res));

            try {
              if (_response != null) {
                _response.createAck().send();
              }
            }
            catch (Exception ex) {
              LOG.debug("Exception when sending back ACK", ex);
            }
            disconnectCall(_call2, false, CallCompleteEvent.Cause.CANCEL, null);
          }
          else {
            candidateCalls.remove(call);
            if (candidateCalls.isEmpty() && _call2 == null) {
              _call2 = call;
              done(getJoinCompleteCauseByResponse(res), getExceptionByResponse(res));
            }
            disconnectCall(call, true, getCallCompleteCauseByResponse(res), getExceptionByResponse(res));
          }
        }
      }
    }
    catch (Exception ex) {
      LOG.error("Exception when joining using delegate " + this, ex);
      done(JoinCompleteEvent.Cause.ERROR, ex);
      if (_call2 != null) {
        failCall(_call2, ex);
      }
      if (!candidateCalls.isEmpty()) {
        disconnectCalls(candidateCalls);
      }
      throw ex;
    }
  }

  private void successJoin() throws MsControlException {
    _peer = _call2;
    doDisengage(_call1, JoinType.DIRECT);
    _call1.linkCall(_call2, JoinType.DIRECT, _direction);
    done(JoinCompleteEvent.Cause.JOINED, null);
  }

  private void reInviteCall(SIPCallImpl call, SipServletMessage message) throws Exception {
    call.reInviteRemote(message.getContent(), null, null);
  }
}
