package com.kkontagion;

import android.nfc.Tag;
import android.nfc.tech.NfcV;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class NfcV {
  private android.nfc.tech.NfcV nfcv = null;

  private void connect(Tag tag) throws IOException {
    nfcv = android.nfc.tech.get(tag);
    nfcv.connect();
  }

  public String read(Tag tag) throws Exception {
    connect(tag);
    if (nfcv == null || !nfcv.isConnected())
      throw new Exception();

    byte[] readCmd = new byte[] {
      (byte) 0x00, // flags
      (byte) 0x23, // READ MULTIPLE command
      (byte) 0x00, // offset - first block
      (byte) 0x08 // number of blocks to read
    };
    byte[] readRes = nfcv.transceive(readCmd);
    // Chop off the initial 0x00 byte
    readRes = Arrays.copyOfRange(readRes, 1, 32);

    // Release resources
    disconnect();

    // Convert to string and return
    return new String(readRes);
  }

  private void disconnect() throws Exception {
    nfcv.close();
    nfcv = null;
  }
}
