package com.voxeo.moho.util;

import java.util.UUID;

import com.voxeo.moho.ApplicationContextImpl;
import com.voxeo.moho.remotejoin.RemoteParticipant;
import com.voxeo.moho.spi.ExecutionContext;

public class IDGenerator {

  public static String generateId(ExecutionContext context) {

    return generateId(context, RemoteParticipant.RemoteParticipant_TYPE_CALL);
  }

  public static String generateId(ExecutionContext context, String type, String uid) {
    if (context != null) {
      if (uid == null) {
        uid = String.valueOf(Math.abs(new com.eaio.uuid.UUID().getTime()));
      }
      String rawid = ((ApplicationContextImpl) context).generateID(type, uid);

      return ParticipantIDParser.encode(rawid);
    }
    else {
      return UUID.randomUUID().toString();
    }
  }

  public static String generateId(ExecutionContext context, String type) {
    return generateId(context, type, null);
  }
}
