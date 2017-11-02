package com.kkontagion;

import android.nfc.Tag;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MifareUltralight {
    private android.nfc.tech.MifareUltralight mifare = null;

    public void connect(Tag tag) throws IOException {
        mifare = android.nfc.tech.MifareUltralight.get(tag);
        mifare.connect();
    }

    public boolean isConnected() {
        return mifare != null && mifare.isConnected();
    }

    public void disconnect() throws Exception {
        mifare.close();
        mifare = null;
    }

    public boolean unlockWithPin(int pin) throws Exception {
        final byte[] pinAsByteArray = intToByteArray(pin);
        byte[] response = mifare.transceive(new byte[]{
                (byte) 0x1B, // PWD_AUTH
                pinAsByteArray[0], pinAsByteArray[1], pinAsByteArray[2], pinAsByteArray[3]
        });
        if ((response != null) && (response.length >= 2)) {
            // byte[] pack = Arrays.copyOf(response, 2);
            return true;
        }
        return false;
    }

    public byte[] read(int pageOffset) throws Exception {
        if (mifare == null || !mifare.isConnected()) {
            throw new Exception();
        }
        return mifare.readPages(pageOffset);
    }

    public void write(int pageOffset, byte[] data) throws Exception {
        if (mifare == null || !mifare.isConnected()) {
            throw new Exception();
        }
        mifare.writePage(pageOffset, data);
    }

    private byte[] intToByteArray(int number) {
        return ByteBuffer.allocate(4).putInt(number).array();
    }
}
