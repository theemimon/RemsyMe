package me.remsy;

import android.content.Intent;
import android.net.Uri;
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

import me.remsy.databinding.ActivityMainBinding;

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
        if (!Intent.ACTION_SEND.equals(action)) return;

        Uri imageUri = SharedImageExtractor.extractImageUri(intent);
        if (imageUri != null) {
            ImageUriPermissionHelper.persistReadPermission(getContentResolver(), imageUri);
            Toast.makeText(this, "Reading recipe from screenshot...", Toast.LENGTH_SHORT).show();
            ScreenshotOcrScraper.scrapeImage(getApplicationContext(), imageUri, new ScreenshotOcrScraper.Callback() {
                @Override
                public void onSuccess(Recipe draftRecipe) {
                    binding.getRoot().post(() -> navigateToAddRecipeDraft(draftRecipe));
                }

                @Override
                public void onError(String message) {
                    binding.getRoot().post(() ->
                            Toast.makeText(MainActivity.this, "Couldn't read text from that image", Toast.LENGTH_SHORT).show());
                }
            });
            return;
        }

        String sharedText = SharedIntentTextExtractor.extractSharedText(intent);

        if (sharedText != null && !sharedText.trim().isEmpty()) {
            String url = UrlExtractor.extractUrl(sharedText);
            if (url != null) {
                Toast.makeText(this, "Scraping web page text...", Toast.LENGTH_SHORT).show();
                WebRecipeScraper.scrapeUrl(getApplicationContext(), url, (scrapedText, draftRecipe) -> {
                    if (draftRecipe != null
                            && draftRecipe.getIngredients().isEmpty()
                            && (draftRecipe.getInstructions().startsWith("Shared URL:") || draftRecipe.getInstructions().equals(url))) {
                        Recipe fallbackDraft = RecipeParser.parseSharedText(sharedText);
                        fallbackDraft.setSourceLink(url);
                        fallbackDraft.setScrapedText(sharedText);
                        binding.getRoot().post(() -> navigateToAddRecipeDraft(fallbackDraft));
                    } else {
                        binding.getRoot().post(() -> navigateToAddRecipeDraft(draftRecipe));
                    }
                });
            } else {
                Recipe draft = RecipeParser.parseSharedText(sharedText);
                draft.setScrapedText(sharedText);
                binding.getRoot().post(() -> navigateToAddRecipeDraft(draft));
            }
        }
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
            args.putString("draftImageUri", draft.getImageUri());

            navController.navigate(R.id.AddRecipeFragment, args);
        }
    }
}