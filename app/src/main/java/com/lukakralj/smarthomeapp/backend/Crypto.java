package com.lukakralj.smarthomeapp.backend;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Crypto {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static Crypto instance;

    private Crypto() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();

        System.out.println("\n\n\n\n==============private:\n");
        System.out.println(Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT));
        System.out.println(privateKey);
        System.out.println(privateKey.getFormat());
        System.out.println("\n\n========public:\n");
        System.out.println(Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT));
        System.out.println(publicKey);
        System.out.println(publicKey.getFormat());
        System.out.println("\n\n\n");
    }

    public static Crypto getInstance() {
        if (instance == null) {
            try {
                instance = new Crypto();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getCause());
            }
        }
        return instance;
    }

    public String getPublicKey() {
        return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
    }

    public String encrypt(String msg, String serverPublicKey) throws InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        byte[] keyDecoded = Base64.decode(serverPublicKey, Base64.DEFAULT);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyDecoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey serverKey = factory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, serverKey);
        byte[] encrypted = cipher.doFinal(msg.getBytes());
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    public String decrypt(String msgEncrypted) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.decode(msgEncrypted, Base64.DEFAULT));
        return new String(decrypted);
    }

}
