package alexkotsc.wyred.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by AlexKotsc on 21-05-2015.
 */
public class KeyUtil {
    public static PublicKey publicKeyFromString(String s) throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException {

        byte[] keyBytes = Base64.decode(s.getBytes("utf-8"),0);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);

        return key;
    }

    public static String publicKeyToString(PublicKey pk){
        return Base64.encodeToString(pk.getEncoded(), Base64.DEFAULT);
    }
}
