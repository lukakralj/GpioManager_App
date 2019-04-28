package com.lukakralj.smarthomeapp.backend;

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

import org.json.JSONObject;

import javax.crypto.Cipher;

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
        try {
            byte[] keyDecoded = Base64.decode(newKey, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyDecoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            serverKey = factory.generatePublic(keySpec);
        }
        catch (Exception e) {
            Logger.log(e.toString(), Level.ERROR);
            serverKey = null;
        }
    }

    /**
     * Encrypts the message using the servers public key.
     *
     * @param msg Message to encrypt.
     * @return Base64 encoded string or null if unsuccessful.
     */
    public String encrypt(JSONObject msg) {
        try {
            String str = msg.toString();
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, serverKey);
            byte[] encrypted = cipher.doFinal(str.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }
        catch (Exception e) {
            Logger.log(e.toString(), Level.ERROR);
            return null;
        }
    }

    /**
     * Decrypts the message using this user's private key.
     *
     * @param msgEncrypted Message to encrypt.
     * @return Decoded string or null if unsuccessful.
     */
    public JSONObject decrypt(String msgEncrypted) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(Base64.decode(msgEncrypted, Base64.DEFAULT));
            String str = new String(decrypted);
            return new JSONObject(str);
        }
        catch (Exception e) {
            Logger.log(e.toString(), Level.ERROR);
            return null;
        }
    }

}
