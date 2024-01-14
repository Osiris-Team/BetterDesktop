package com.osiris.betterdesktop.utils;

import com.author.core.utils.UtilsNative;
import org.junit.jupiter.api.Test;

import java.io.File;

class UtilsNativeTest {
    @Test
    void autostartFolder() throws Exception {
        File f = new File(System.getProperty("user.dir")+"/"+System.currentTimeMillis()+".txt");
        f.createNewFile();
        new UtilsNative().addToAutoStartFolder(f);
        File autostartLink = new UtilsNative().getFromAutoStartFolder(f);
        if(!autostartLink.exists())
            throw new Exception("failed to add to autostart: "+f+" failed to create: "+autostartLink);
        autostartLink.delete();
        f.delete();
    }
}
