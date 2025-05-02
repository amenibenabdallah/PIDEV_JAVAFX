package tn.esprit.controllers;

import com.stripe.Stripe;

public class StripeConfig {

    // Ajoute ici tes clés
    private static final String STRIPE_SECRET_KEY = "sk_test_51PIetLRt6ryr9tuOnuCEa3A7KD9UiRQdTXoSKEkiUvJt02s3NYqwB7rOIuA8hNAggtrDBTrIkElA2uRrP0lqC47D00Yuv37wS4";
    private static final String STRIPE_PUBLIC_KEY = "pk_test_51PIetLRt6ryr9tuOakALb3ORAwL81fhW0qhpkEyYhW8ZHSyi8CHCvnVSKnO1d8ZZCHPD3EUROj4jM7kHctjabchR007oLu0KI3";

    public static void initialize() {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public static String getPublicKey() {
        return STRIPE_PUBLIC_KEY;
    }
}