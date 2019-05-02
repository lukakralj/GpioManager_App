package com.lukakralj.smarthomeapp.backend;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import android.util.Base64;
import com.lukakralj.smarthomeapp.backend.logger.Level;
import com.lukakralj.smarthomeapp.backend.logger.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;

public class Crypto {

    private static Crypto instance;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey serverKey;

    private Crypto() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    /**
     *
     * @return Instance of Crypto.
     */
    public static Crypto getInstance() {
        if (instance == null) {
            try {
                instance = new Crypto();
            }
            catch (Exception e) {
                Logger.log(e.getMessage(), Level.ERROR);
e.printStackTrace();
e.printStackTrace();
                throw new RuntimeException(e.getCause());
            }
        }
        return instance;
    }

    /**
     * Reset the instance, including RSA keys.
     *
     * @return A new instance of Crypto.
     */
    public static Crypto resetInstance() {
        instance = null;
        return getInstance();
    }

    /**
     *
     * @return Public key for this user.
     */
    public String getPublicKey() {
        return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
    }

    /**
     * Set the server's public key to be used in encryption.
     *
     * @param newKey Base64 encoded string.
     */
    public void setServerPublicKey(String newKey) {
        Logger.log("server key: " + newKey);
        try {
            byte[] keyDecoded = Base64.decode(newKey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyDecoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            serverKey = factory.generatePublic(keySpec);
        }
        catch (Exception e) {
            Logger.log(e.getMessage(), Level.ERROR);
            serverKey = null;
            e.printStackTrace();
        }
    }

    /**
     * Encrypts the message using the servers public key.
     *
     * @param msg Message to rsaEncrypt.
     * @return Base64 encoded string or null if unsuccessful.
     */
    public String rsaEncrypt(JSONObject msg) {
        try {
            String str = msg.toString();
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] encrypted = cipher.doFinal(str.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }
        catch (Exception e) {
            Logger.log(e.getMessage(), Level.ERROR);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decrypts the message using this user's private key.
     *
     * @param msgEncrypted Message to rsaEncrypt.
     * @return Decoded string or null if unsuccessful.
     */
    public JSONObject rsaDecrypt(String msgEncrypted) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.decode(msgEncrypted, Base64.DEFAULT));
            String str = new String(decrypted);
            return new JSONObject(str);
        }
        catch (Exception e) {
            Logger.log(e.getMessage(), Level.ERROR);
            e.printStackTrace();
            return null;
        }
    }

    public static String aesEncrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeToString(encrypted, Base64.DEFAULT));

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String aesDecrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /*public static void main(String[] args) {
        String key = "Bar12345Bar12345"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV

        System.out.println(aesDecrypt(key, initVector,
                aesDecrypt(key, initVector, "Hello World")));
    }*/

}
