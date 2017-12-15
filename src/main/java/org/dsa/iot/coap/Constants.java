package org.dsa.iot.coap;

import org.dsa.iot.dslink.util.json.JsonArray;
import org.dsa.iot.dslink.util.json.JsonObject;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class Constants {
    public static final String CONN = "conn";
    public static final String REMOTE_NAME = "RemoteDSA";
    public static final String MAIN_SERVER_NAME = "__DSACoapGateway";
    public static final String RID_PREFIX = "__rid_";
    public static final String REMOTE_RID_FIELD = "RemoteRid";
    public static final String BLANK_KEY = "BlankJson";
    public static final String BLANK_VAL = "Handshake";
    public static final String GIMME = "GiveMe";
    public static final String RID_ZERO_HANDLE = "Rid0Handle";
    public static final String HERE_YOU_GO = "HereYoGo";

    public static JsonObject makeCloseReponse(int rid) {
        JsonObject obj = new JsonObject();
        obj.put("rid", rid);
        obj.put("stream", "closed");
        return obj;
    }

    public static byte[] jsonToBytes(JsonObject json) {
        byte[] bytes = new byte[0];
        try {
            bytes = json.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static JsonObject extractPayload(CoapResponse response) {
        return new JsonObject(new String(response.getPayload()));
    }

    public static JsonObject createSubReq(JsonArray paths, int rid) {
        JsonObject res = new JsonObject();
        res.put("method", "subscribe");
        res.put("rid", rid);
        res.put("paths", paths);
        return res;
    }

    public static JsonObject createSidUpd(JsonArray updates) {
        JsonObject ret = new JsonObject();
        ret.put("rid", 0);
        ret.put("updates", updates);
        return ret;
    }


    public static void sortLocalVsRemote(JsonArray paths, JsonArray local, Map<String, JsonArray> remote) {
        for (Object subReq : paths) {
            JsonObject sub = (JsonObject) subReq;
            String subPath = sub.get("path");
            if (subPath != null && subPath.contains(Constants.REMOTE_NAME)) {
                String nodeName = extractNodeName(subPath);
                sub.put("path",extractRemotePath(sub.get("path")));
                if (remote.containsKey(nodeName)) remote.get(nodeName).add(sub);
                else remote.put(nodeName, new JsonArray().add(sub));
            } else {
                local.add(sub);
            }
        }
    }

    public static JsonObject extractPayload(CoapExchange exchange) {
        return new JsonObject(new String(exchange.getRequestPayload()));
    }

    public static String extractRemotePath(String path) {
        int idx = path.indexOf(REMOTE_NAME);
        if (idx >= 0) {
            String sub = path.substring(idx + REMOTE_NAME.length());
            if (sub.length() > 0) return sub;
            else return "/";
        } else {
            return null;
        }
    }

    public static String extractNodeName(String path) {
        int idx = path.indexOf(REMOTE_NAME);
        if (idx >= 0) {
            String[] split = path.substring(0, idx).split("/");
            return split[split.length - 1];
        } else {
            return null;
        }
    }
}
