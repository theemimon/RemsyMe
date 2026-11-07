package me.remsy;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import me.remsy.databinding.FragmentEditRecipeBinding;

import java.util.List;

public class EditRecipeFragment extends Fragment {

    private static final String OPTION_CREATE_NEW = "+ Create New Category";

    private FragmentEditRecipeBinding binding;
    private RecipeDatabaseHelper dbHelper;
    private Recipe currentRecipe;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    ImageUriPermissionHelper.persistReadPermission(requireContext().getContentResolver(), uri);
                    selectedImageUri = uri;
                    binding.ivRecipePicturePreview.setImageURI(uri);
                }
            }
    );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentEditRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = RecipeDatabaseHelper.getInstance(requireContext());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(EditRecipeFragment.this).navigateUp()
        );

        setupCategoryDropdown();

        long recipeId = 1;
        if (getArguments() != null) {
            recipeId = getArguments().getLong("recipeId", 1);
        }

        currentRecipe = dbHelper.getRecipeById(recipeId);
        if (currentRecipe == null) {
            currentRecipe = dbHelper.getRecipeById(1);
        }

        if (currentRecipe != null) {
            prefillRecipeData(currentRecipe);
        }

        binding.cardPhotoPicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.btnSaveRecipe.setOnClickListener(v -> {
            if (currentRecipe == null) return;

            String name = binding.etRecipeName.getText() != null ? binding.etRecipeName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                binding.etRecipeName.setError("Recipe name is required");
                return;
            }

            String selectedCategory = binding.actvCategory.getText() != null ? binding.actvCategory.getText().toString().trim() : "";
            String category = selectedCategory;

            if (OPTION_CREATE_NEW.equals(selectedCategory) || selectedCategory.isEmpty()) {
                String newCategory = binding.etNewCategory.getText() != null ? binding.etNewCategory.getText().toString().trim() : "";
                if (!newCategory.isEmpty()) {
                    category = newCategory;
                } else {
                    category = "Uncategorized";
                }
            }

            String time = binding.etPrepTime.getText() != null ? binding.etPrepTime.getText().toString().trim() : "";
            String portions = binding.etPortions.getText() != null ? binding.etPortions.getText().toString().trim() : "";
            String ingredients = binding.etIngredients.getText() != null ? binding.etIngredients.getText().toString().trim() : "";
            String instructions = binding.etInstructions.getText() != null ? binding.etInstructions.getText().toString().trim() : "";
            String sourceLink = binding.etSourceLink.getText() != null ? binding.etSourceLink.getText().toString().trim() : "";
            String scrapedText = binding.etScrapedText.getText() != null ? binding.etScrapedText.getText().toString().trim() : "";

            currentRecipe.setName(name);
            currentRecipe.setCategory(category);
            currentRecipe.setPrepTime(time);
            currentRecipe.setPortions(portions);
            currentRecipe.setIngredients(ingredients);
            currentRecipe.setInstructions(instructions);
            currentRecipe.setSourceLink(sourceLink);
            currentRecipe.setScrapedText(scrapedText);

            if (selectedImageUri != null) {
                currentRecipe.setImageUri(selectedImageUri.toString());
            }

            dbHelper.updateRecipe(currentRecipe);

            Toast.makeText(requireContext(), R.string.msg_recipe_updated, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(EditRecipeFragment.this).popBackStack();
        });
    }

    private void setupCategoryDropdown() {
        List<String> categories = dbHelper.getAllCategories();
        if (!categories.contains(OPTION_CREATE_NEW)) {
            categories.add(OPTION_CREATE_NEW);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        binding.actvCategory.setAdapter(adapter);

        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selected = adapter.getItem(position);
            if (OPTION_CREATE_NEW.equals(selected)) {
                binding.tilNewCategory.setVisibility(View.VISIBLE);
            } else {
                binding.tilNewCategory.setVisibility(View.GONE);
            }
        });
    }

    private void prefillRecipeData(Recipe recipe) {
        binding.etRecipeName.setText(recipe.getName());
        if (recipe.getCategory() != null && !recipe.getCategory().isEmpty()) {
            binding.actvCategory.setText(recipe.getCategory(), false);
        }
        binding.etPrepTime.setText(recipe.getPrepTime());
        binding.etPortions.setText(recipe.getPortions());
        binding.etIngredients.setText(recipe.getIngredients());
        binding.etInstructions.setText(recipe.getInstructions());
        if (recipe.getSourceLink() != null) binding.etSourceLink.setText(recipe.getSourceLink());
        if (recipe.getScrapedText() != null) binding.etScrapedText.setText(recipe.getScrapedText());

        if (recipe.getImageUri() != null && !recipe.getImageUri().isEmpty()) {
            RecipeImageBinder.bind(binding.ivRecipePicturePreview, recipe.getImageUri());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}