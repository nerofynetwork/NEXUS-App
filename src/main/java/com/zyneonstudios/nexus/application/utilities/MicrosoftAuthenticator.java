package com.zyneonstudios.nexus.application.utilities;

import com.starxg.keytar.Keytar;
import com.zyneonstudios.nexus.application.main.NexusApplication;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import live.nerotv.zyneon.auth.ZyneonAuth;

import java.util.Base64;
import java.util.HashMap;

public class MicrosoftAuthenticator {

    private static AuthInfos authInfos = null;

    public static void startLogin(boolean save) {
        try {
            setAuthInfos(ZyneonAuth.getAuthInfos(), save);
        } catch (Exception exception) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't fetch the Microsoft token.",exception.getMessage(), exception.getStackTrace());
        }

        if(NexusApplication.getInstance().getApplicationFrame() != null) {
            NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
        }
    }

    public static void refresh(String token, boolean save) {
        try {
            setAuthInfos(ZyneonAuth.getAuthInfos(token), save);
        } catch (Exception exception) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't refresh the Microsoft token.",exception.getMessage(), exception.getStackTrace());
        }

        if(NexusApplication.getInstance().getApplicationFrame() != null) {
            NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
        }
    }

    private static void setAuthInfos(HashMap<ZyneonAuth.AuthInfo, String> authData, boolean save) {
        authInfos = new AuthInfos(authData.get(ZyneonAuth.AuthInfo.USERNAME), authData.get(ZyneonAuth.AuthInfo.ACCESS_TOKEN), authData.get(ZyneonAuth.AuthInfo.UUID));
        NexusApplication.setAuthInfos(authInfos);
        if(save) {
            save(authData);
        }
    }

    private static void save(HashMap<ZyneonAuth.AuthInfo, String> authData) {
        try {
            String UUID = Base64.getEncoder().encodeToString(authData.get(ZyneonAuth.AuthInfo.UUID).getBytes());
            String token = Base64.getEncoder().encodeToString(authData.get(ZyneonAuth.AuthInfo.REFRESH_TOKEN).getBytes());
            Keytar.getInstance().setPassword("ZNA||00||00","0",UUID);
            Keytar.getInstance().setPassword("ZNA||01||00",UUID+"_0",token);
        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't save credentials.",e.getMessage(), e.getStackTrace());
        }
    }

    public static void logout() {
        try {
            String UUID = Base64.getEncoder().encodeToString(authInfos.getUuid().getBytes());
            Keytar.getInstance().deletePassword("ZNA||00||00","0");
            Keytar.getInstance().deletePassword("ZNA||01||00",UUID+"_0");
        } catch (Exception e) {
            NexusApplication.getLogger().printErr("NEXUS","AUTHENTICATION","Couldn't delete credentials.",e.getMessage(), e.getStackTrace());
        }

        authInfos = null;
        NexusApplication.setAuthInfos(null);

        if(NexusApplication.getInstance().getApplicationFrame() != null) {
            NexusApplication.getInstance().getApplicationFrame().getBrowser().reload();
        }
    }

    public static String getUUID() {
        if(authInfos!=null) {
            return authInfos.getUuid();
        }
        return null;
    }

    public static String getUsername() {
        if(authInfos!=null) {
            return authInfos.getUsername();
        }
        return null;
    }

    public static boolean isLoggedIn() {
        return authInfos != null;
    }
}