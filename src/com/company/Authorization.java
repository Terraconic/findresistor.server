package com.company;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Authorization {

    public static boolean AuthorizationUser(String mail, String pass) {

        String userFromDB[] = DataBase.getUserName(mail);

        try {
            if (userFromDB[0] == null) {
                System.out.println("Authorization: User not found");
                return false;
            } else {

                boolean isValidate;

                try {
                    isValidate = PasswordHash.checkPassword(pass, userFromDB[1], userFromDB[2]);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    System.out.println("Authorization error: error authorization");
                    return false;
                }

                if (isValidate) {
                    System.out.println("Authorization: User was found");
                    return true;
                } else {
                    System.out.println("Authorization: Login or password is wrong");
                    return false;
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("Authorization error: Email or password is wrong");
            return false;
        }
    }
}
