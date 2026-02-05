package com.example.lab5_starter;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button delButton;
    private Button editButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;

    private CollectionReference citiesRef;

    private City currentCity = null;

    private Integer currentIndex = null;

    private View currentView = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        delButton = findViewById(R.id.buttonDeleteCity);
        editButton = findViewById(R.id.buttonEditCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        //addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCity == null || currentIndex == null) {
                    return;
                }
                //This function call was a modified version of what was provided by Google Gemini on February 4th 2026,
                //"How does the delete button call interact with the firestore database, how would I set that query up?"
                citiesRef
                        .whereEqualTo("name", currentCity.getName())       // Use the name from the selected city
                        .whereEqualTo("province", currentCity.getProvince()) // Use the province from the selected city
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // This task is successful when the query completes.
                            // Now, loop through the results (usually just one) and delete each document found.
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully deleted from Firestore
                                            Log.d("Firestore", "DocumentSnapshot successfully deleted!");

                                            // 3. Reset the selection state in the UI
                                            currentCity = null;
                                            currentIndex = null;
                                            currentView = null;
                                            // The snapshot listener will automatically update the list,
                                            // so we don't need to call notifyDataSetChanged() here.
                                        })
                                        .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Firestore", "Error finding document to delete", e));
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCity == null || currentIndex == null) {
                    return;
                }
                //This function call was a modified version of what was provided by Google Gemini on February 4th 2026,
                //"How does the delete button call interact with the firestore database, how would I set that query up?"
                citiesRef
                        .whereEqualTo("name", currentCity.getName())       // Use the name from the selected city
                        .whereEqualTo("province", currentCity.getProvince()) // Use the province from the selected city
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // This task is successful when the query completes.
                            // Now, loop through the results (usually just one) and delete each document found.
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully deleted from Firestore
                                            Log.d("Firestore", "DocumentSnapshot successfully deleted!");

                                            // 3. Reset the selection state in the UI
                                            currentCity = null;
                                            currentIndex = null;
                                            currentView = null;
                                            // The snapshot listener will automatically update the list,
                                            // so we don't need to call notifyDataSetChanged() here.
                                        })
                                        .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("Firestore", "Error finding document to delete", e));

                //after a delete reset all items, so the highlight doesnt stay
                for (int i = 0; i < cityListView.getChildCount(); i++) {
                    View childView = cityListView.getChildAt(i);
                    childView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCity == null || currentIndex == null) {
                    return;
                }
                City city = cityArrayAdapter.getItem(currentIndex);
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(),"City Details");
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {

            if (currentView != null) {
                //reset old button to white if selecting same one
                currentView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            if (currentIndex != null && currentIndex == i) {
                currentCity = null;
                currentIndex = null;
                currentView = null;
                return;
            }

            currentIndex = i;
            currentCity = cityArrayAdapter.getItem(i);
            currentView = view;

            view.setBackgroundColor(Color.parseColor("#b6b6b6"));
        });

        db = FirebaseFirestore.getInstance();

        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null && !value.isEmpty()) {
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

    }

    //This function was a modified version of what was provided by Google Gemini on February 4th 2026,
    //"How can I modify the update city function similarily to what was done with the delete button?"
    @Override
    public void updateCity(City oldCity, String newName, String newProvince) {
        //1. Find the document that matches the OLD city details
        citiesRef
                .whereEqualTo("name", oldCity.getName())
                .whereEqualTo("province", oldCity.getProvince())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 2. Loop through the results (should only be one) and delete the document
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete().addOnSuccessListener(aVoid -> {
                            // 3. After the old document is successfully deleted, add the new one
                            Log.d("Firestore", "Old city document deleted successfully.");
                            City newCity = new City(newName, newProvince);
                            citiesRef.document(newCity.getName()).set(newCity)
                                    .addOnSuccessListener(aVoid1 -> Log.d("Firestore", "New city document added successfully."))
                                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding new city document", e));
                        }).addOnFailureListener(e -> Log.w("Firestore", "Error deleting old city document", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error finding document to update", e));
        // The SnapshotListener will handle updating the UI automatically.

        for (int i = 0; i < cityListView.getChildCount(); i++) {
            View childView = cityListView.getChildAt(i);
            childView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}