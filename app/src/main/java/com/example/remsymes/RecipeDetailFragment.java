package com.example.remsymes;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.remsymes.databinding.FragmentRecipeDetailBinding;

public class RecipeDetailFragment extends Fragment {

    private FragmentRecipeDetailBinding binding;
    private RecipeDatabaseHelper dbHelper;
    private Recipe currentRecipe;
    private long recipeId = 1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = RecipeDatabaseHelper.getInstance(requireContext());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(RecipeDetailFragment.this).navigateUp()
        );

        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", 1);
        }

        loadRecipeData();

        // Setup toolbar menu for Edit and Delete actions
        binding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        // Interactive star rating click listeners
        binding.ivStar1.setOnClickListener(v -> rateRecipe(1.0));
        binding.ivStar2.setOnClickListener(v -> rateRecipe(2.0));
        binding.ivStar3.setOnClickListener(v -> rateRecipe(3.0));
        binding.ivStar4.setOnClickListener(v -> rateRecipe(4.0));
        binding.ivStar5.setOnClickListener(v -> rateRecipe(5.0));

        // Favorite button toggle listener
        binding.btnFavorite.setOnClickListener(v -> {
            if (currentRecipe == null) return;
            currentRecipe.setFavorite(!currentRecipe.isFavorite());
            dbHelper.updateRecipe(currentRecipe);
            updateFavoriteUI(currentRecipe.isFavorite());

            String msg = currentRecipe.isFavorite() ? "Added to favorites" : "Removed from favorites";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipeData();
    }

    private void loadRecipeData() {
        currentRecipe = dbHelper.getRecipeById(recipeId);
        if (currentRecipe == null) {
            currentRecipe = dbHelper.getRecipeById(1);
        }

        if (currentRecipe != null) {
            populateRecipeData(currentRecipe);
        }
    }

    private void populateRecipeData(Recipe recipe) {
        binding.tvRecipeTitle.setText(recipe.getName());
        binding.tvCategory.setText(recipe.getCategory() != null && !recipe.getCategory().isEmpty() ? recipe.getCategory() : "Uncategorized");

        if (recipe.getPrepTime() != null && !recipe.getPrepTime().isEmpty()) {
            binding.tvTime.setText(recipe.getPrepTime());
        } else {
            binding.tvTime.setText("-");
        }

        if (recipe.getPortions() != null && !recipe.getPortions().isEmpty()) {
            binding.tvPortions.setText(recipe.getPortions());
        } else {
            binding.tvPortions.setText("-");
        }

        // Ingredients section
        if (recipe.getIngredients() != null && !recipe.getIngredients().trim().isEmpty()) {
            binding.tvIngredientsHeading.setVisibility(View.VISIBLE);
            binding.cardIngredients.setVisibility(View.VISIBLE);
            binding.tvIngredientsContent.setText(recipe.getIngredients().trim());
        } else {
            binding.tvIngredientsHeading.setVisibility(View.GONE);
            binding.cardIngredients.setVisibility(View.GONE);
        }

        // Instructions section
        if (recipe.getInstructions() != null && !recipe.getInstructions().trim().isEmpty()) {
            binding.tvInstructionsHeading.setVisibility(View.VISIBLE);
            binding.cardInstructions.setVisibility(View.VISIBLE);
            binding.tvInstructionsContent.setText(recipe.getInstructions().trim());
        } else {
            binding.tvInstructionsHeading.setVisibility(View.GONE);
            binding.cardInstructions.setVisibility(View.GONE);
        }

        // Source Link section
        if (recipe.getSourceLink() != null && !recipe.getSourceLink().trim().isEmpty()) {
            binding.tvSourceLinkHeading.setVisibility(View.VISIBLE);
            binding.cardSourceLink.setVisibility(View.VISIBLE);
            binding.tvSourceLinkContent.setText(recipe.getSourceLink().trim());
        } else {
            binding.tvSourceLinkHeading.setVisibility(View.GONE);
            binding.cardSourceLink.setVisibility(View.GONE);
        }

        // Raw Scraped Text section
        if (recipe.getScrapedText() != null && !recipe.getScrapedText().trim().isEmpty()) {
            binding.tvScrapedTextHeading.setVisibility(View.VISIBLE);
            binding.cardScrapedText.setVisibility(View.VISIBLE);
            binding.tvScrapedTextContent.setText(recipe.getScrapedText().trim());
        } else {
            binding.tvScrapedTextHeading.setVisibility(View.GONE);
            binding.cardScrapedText.setVisibility(View.GONE);
        }

        updateStarRatingUI(recipe.getRating());
        updateFavoriteUI(recipe.isFavorite());

        if (recipe.getImageUri() != null && !recipe.getImageUri().isEmpty()) {
            if (recipe.getImageUri().startsWith("content://") || recipe.getImageUri().startsWith("file://")) {
                try {
                    binding.ivRecipePicture.setImageURI(Uri.parse(recipe.getImageUri()));
                } catch (Exception e) {
                    binding.ivRecipePicture.setImageResource(R.drawable.recipe_sample_image);
                }
            } else if ("recipe_sample_dessert".equals(recipe.getImageUri())) {
                binding.ivRecipePicture.setImageResource(R.drawable.recipe_sample_dessert);
            } else if ("recipe_sample_main".equals(recipe.getImageUri())) {
                binding.ivRecipePicture.setImageResource(R.drawable.recipe_sample_main);
            } else if ("recipe_sample_breakfast".equals(recipe.getImageUri())) {
                binding.ivRecipePicture.setImageResource(R.drawable.recipe_sample_breakfast);
            } else {
                binding.ivRecipePicture.setImageResource(R.drawable.recipe_sample_image);
            }
        }
    }

    private void rateRecipe(double rating) {
        if (currentRecipe == null) return;
        currentRecipe.setRating(rating);
        dbHelper.updateRecipe(currentRecipe);
        updateStarRatingUI(rating);
        Toast.makeText(requireContext(), "Rated " + String.format("%.1f", rating) + " ★", Toast.LENGTH_SHORT).show();
    }

    private void updateStarRatingUI(double rating) {
        int filledStars = (int) Math.round(rating);

        binding.ivStar1.setImageResource(filledStars >= 1 ? R.drawable.ic_star : R.drawable.ic_star_outline);
        binding.ivStar2.setImageResource(filledStars >= 2 ? R.drawable.ic_star : R.drawable.ic_star_outline);
        binding.ivStar3.setImageResource(filledStars >= 3 ? R.drawable.ic_star : R.drawable.ic_star_outline);
        binding.ivStar4.setImageResource(filledStars >= 4 ? R.drawable.ic_star : R.drawable.ic_star_outline);
        binding.ivStar5.setImageResource(filledStars >= 5 ? R.drawable.ic_star : R.drawable.ic_star_outline);

        if (rating > 0) {
            binding.tvStarRatingNum.setText(String.format("%.1f", rating));
        } else {
            binding.tvStarRatingNum.setText("0.0");
        }
    }

    private void updateFavoriteUI(boolean isFavorite) {
        if (isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite);
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit_recipe) {
            if (currentRecipe != null) {
                Bundle args = new Bundle();
                args.putLong("recipeId", currentRecipe.getId());
                NavHostFragment.findNavController(RecipeDetailFragment.this)
                        .navigate(R.id.action_RecipeDetailFragment_to_EditRecipeFragment, args);
            }
            return true;
        } else if (itemId == R.id.action_delete_recipe) {
            if (currentRecipe != null) {
                dbHelper.deleteRecipe(currentRecipe.getId());
                Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(RecipeDetailFragment.this).popBackStack();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}