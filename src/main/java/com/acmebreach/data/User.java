package com.acmebreach.data;

/**
 * Một người dùng giả trong cơ sở dữ liệu của AcmeCorp.
 */
public class User {

    public final String username;
    public final String plaintextForGradingOnly;
    public final boolean insideBruteForceSpace;


    public final String sha1NoSalt;


    public final String sha256NoSalt;

    
    public final String salt;
    public final String sha256Salted;

    public User(String username, String plaintextForGradingOnly, boolean insideBruteForceSpace,
                String sha1NoSalt, String sha256NoSalt, String salt, String sha256Salted) {
        this.username = username;
        this.plaintextForGradingOnly = plaintextForGradingOnly;
        this.insideBruteForceSpace = insideBruteForceSpace;
        this.sha1NoSalt = sha1NoSalt;
        this.sha256NoSalt = sha256NoSalt;
        this.salt = salt;
        this.sha256Salted = sha256Salted;
    }
}
