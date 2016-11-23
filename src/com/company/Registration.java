package com.company;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Registration {

    public static boolean RegistrationUser(String mail, String pass) {

        System.out.println("Registration: Start registration user");

        String resultHash;
        String params[];

        try {
            resultHash = PasswordHash.createHash(pass);
            params = resultHash.split(":");
            if (DataBase.insertUser(mail, params[1], params[2])) {
                System.out.println("Registration: End registration user");
                return true;
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.out.println("Registration error: error registration");
            return false;
        }

        return false;
    }
}
