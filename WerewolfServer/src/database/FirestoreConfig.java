package database;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirestoreConfig {

    private static Firestore firestore;

    public static Firestore getFirestore() {
        if (firestore == null) {
            try {
                // Replace with the path to your Firebase Admin SDK JSON file
                FileInputStream serviceAccount = new FileInputStream("censored");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                firestore = FirestoreClient.getFirestore();//FirestoreOptions.getDefaultInstance().getService();
                System.out.println("Firestore initialized successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to initialize Firestore.");
            }
        }
        return firestore;
    }
}
