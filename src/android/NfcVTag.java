package com.kkontagion;

import android.nfc.Tag;
import android.nfc.tech.NfcV;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class NfcVTag {
  private android.nfc.tech.NfcV nfcv = null;

  public void connect(Tag tag) throws IOException {
    nfcv = android.nfc.tech.NfcV.get(tag);
    nfcv.connect();
  }

  public String read(Tag tag) throws Exception {
    if (!isConnected())
      throw new Exception();

      byte[] readCmd = new byte[] {
        (byte) 0x02, // flag
        (byte) 0x23, // READ MULTIPLE command
        (byte) 0x01, // block offset
        (byte) 0x03 // number of blocks to read
      };
      byte[] readRes = nfcv.transceive(readCmd);

    // Release resources
    disconnect();

    // Convert to string and return
    return cleanTransceive(readRes);
  }

  public void disconnect() throws Exception {
    nfcv.close();
    nfcv = null;
  }

  public boolean isConnected() {
    return nfcv != null && nfcv.isConnected();
  }

  private String cleanTransceive(byte[] response) {
    ArrayList<Byte> cleaned = new ArrayList<Byte>();
    for (byte b : response) {
      if (b != 0)
        cleaned.add(b);
    }

    byte[] out = new byte[cleaned.size()];
    for (int i=0; i<cleaned.size(); i++)
      out[i] = cleaned.get(i);

    return new String(out);
  }
}
