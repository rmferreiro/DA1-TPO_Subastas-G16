package ar.edu.uade.grupo16.subastas;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("HASH_IS: " + encoder.encode("Password123!"));
    }
}
