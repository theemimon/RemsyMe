package com.example.remsymes;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.remsymes.databinding.ActivityMainBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handleIncomingSharedIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingSharedIntent(intent);
    }

    private void handleIncomingSharedIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            String sharedText = extractSharedTextFromIntent(intent);

            if (sharedText != null && !sharedText.trim().isEmpty()) {
                String url = extractUrl(sharedText);
                if (url != null) {
                    Toast.makeText(this, "Scraping web page text...", Toast.LENGTH_SHORT).show();
                    WebRecipeScraper.scrapeUrl(url, (scrapedText, draftRecipe) ->
                            binding.getRoot().post(() -> navigateToAddRecipeDraft(draftRecipe))
                    );
                } else {
                    Recipe draft = RecipeParser.parseSharedText(sharedText);
                    draft.setScrapedText(sharedText);
                    binding.getRoot().post(() -> navigateToAddRecipeDraft(draft));
                }
            }
        }
    }

    private String extractUrl(String text) {
        if (text == null) return null;
        Pattern pattern = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    private String extractSharedTextFromIntent(Intent intent) {
        if (intent == null) return null;

        // 1. Try EXTRA_TEXT
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null && !text.trim().isEmpty()) {
            return text;
        }

        // 2. Try EXTRA_SUBJECT
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (subject != null && !subject.trim().isEmpty()) {
            return subject;
        }

        // 3. Try CharSequence EXTRA_TEXT
        CharSequence charSeq = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if (charSeq != null && !charSeq.toString().trim().isEmpty()) {
            return charSeq.toString();
        }

        // 4. Try ClipData
        ClipData clipData = intent.getClipData();
        if (clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            if (item != null) {
                if (item.getText() != null && !item.getText().toString().trim().isEmpty()) {
                    return item.getText().toString();
                }
                if (item.getUri() != null) {
                    return item.getUri().toString();
                }
            }
        }

        return null;
    }

    private void navigateToAddRecipeDraft(Recipe draft) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            Bundle args = new Bundle();
            args.putString("draftName", draft.getName());
            args.putString("draftCategory", draft.getCategory());
            args.putString("draftPrepTime", draft.getPrepTime());
            args.putString("draftPortions", draft.getPortions());
            args.putString("draftIngredients", draft.getIngredients());
            args.putString("draftInstructions", draft.getInstructions());
            args.putString("draftSourceLink", draft.getSourceLink());
            args.putString("draftScrapedText", draft.getScrapedText());

            navController.navigate(R.id.AddRecipeFragment, args);
        }
    }
}