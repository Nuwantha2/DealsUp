package com.s23010901.dealsup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chooseCards extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Map<String, Boolean> selectedBanks = new HashMap<>();
    private Map<String, ImageButton> bankButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_cards);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // bank cards UI elements
        bankButtons.put("BOC", findViewById(R.id.btnBOC));
        bankButtons.put("PB", findViewById(R.id.btnPB));
        bankButtons.put("Sampath", findViewById(R.id.btnSampath));
        bankButtons.put("DFCC", findViewById(R.id.btnDFCC));
        bankButtons.put("NDB", findViewById(R.id.btnNDB));
        bankButtons.put("HNB", findViewById(R.id.btnHNB));
        bankButtons.put("Commercial", findViewById(R.id.btnCommercial));
        bankButtons.put("NTB", findViewById(R.id.btnNTB));

        for (String bank : bankButtons.keySet()) {
            ImageButton btn = bankButtons.get(bank);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleBankSelection(bank);
                }
            });
        }

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelections();
            }
        });
    }

    //change button color after selecting
    private void toggleBankSelection(String bank) {
        boolean selected = !selectedBanks.getOrDefault(bank, false);
        selectedBanks.put(bank, selected);

        ImageButton btn = bankButtons.get(bank);
        if (selected) {
            btn.setBackgroundColor(getResources().getColor(R.color.yellowSelected));
        } else {
            btn.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

    private void saveSelections() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        //save selected cards into firebase
        String uid = mAuth.getCurrentUser().getUid();

        List<String> selectedCardList = new ArrayList<>();
        for (String bank : selectedBanks.keySet()) {
            if (selectedBanks.get(bank)) {
                selectedCardList.add(bank);
            }
        }

        Map<String, Object> userCardData = new HashMap<>();
        userCardData.put("creditCards", selectedCardList);

        db.collection("users").document(uid)
                .set(userCardData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Card selection saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(chooseCards.this, Dashboard.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving card selection", Toast.LENGTH_SHORT).show();
                });
    }
}
