import com.sky.ui.Rc4Codec;
import java.nio.charset.StandardCharsets;

public class Rc4CodecTest {
    public static void main(String[] args) {
        byte[] encrypted = Rc4Codec.INSTANCE.transform("Plaintext".getBytes(StandardCharsets.UTF_8), "Key".getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte value : encrypted) hex.append(String.format("%02x", value & 255));
        if (!hex.toString().equals("bbf316e8d940af0ad3")) throw new AssertionError("RC4 vector mismatch");
        if (!Rc4Codec.INSTANCE.decryptHex(hex.toString(), "Key").equals("Plaintext")) throw new AssertionError("RC4 decode mismatch");
        try {
            Rc4Codec.INSTANCE.decryptHex("invalid", "Key");
            throw new AssertionError("Invalid hex accepted");
        } catch (IllegalArgumentException expected) { }
        System.out.println("PASS: RC4 standard vector, hex decode, invalid input");
    }
}
