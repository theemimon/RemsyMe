package me.remsy;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;

import me.remsy.databinding.FragmentEditRecipeBinding;

import java.util.ArrayList;
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

        binding.btnAddIngredientField.setOnClickListener(v -> addIngredientRow("", ""));
        binding.btnAddStepField.setOnClickListener(v -> addInstructionStepRow(""));

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
            String ingredients = getIngredientsFromFields();
            String instructions = getInstructionsFromFields();
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

    private void addIngredientRow(String quantity, String name) {
        if (getContext() == null) return;
        View rowView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_ingredient_input_field, binding.layoutIngredientsContainer, false);

        TextInputEditText etQuantity = rowView.findViewById(R.id.et_ingredient_quantity);
        TextInputEditText etName = rowView.findViewById(R.id.et_ingredient_name);
        ImageButton btnRemove = rowView.findViewById(R.id.btn_remove_ingredient);

        etQuantity.setText(quantity != null ? quantity : "");
        etName.setText(name != null ? name : "");

        btnRemove.setOnClickListener(v -> binding.layoutIngredientsContainer.removeView(rowView));

        binding.layoutIngredientsContainer.addView(rowView);
    }

    private void populateIngredientFields(String rawIngredients) {
        binding.layoutIngredientsContainer.removeAllViews();
        List<Ingredient> list = Ingredient.parseList(rawIngredients);
        if (!list.isEmpty()) {
            for (Ingredient ing : list) {
                addIngredientRow(ing.getQuantity(), ing.getName());
            }
        } else {
            addIngredientRow("", "");
        }
    }

    private String getIngredientsFromFields() {
        List<Ingredient> list = new ArrayList<>();
        int childCount = binding.layoutIngredientsContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View rowView = binding.layoutIngredientsContainer.getChildAt(i);
            TextInputEditText etQuantity = rowView.findViewById(R.id.et_ingredient_quantity);
            TextInputEditText etName = rowView.findViewById(R.id.et_ingredient_name);

            String q = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
            String n = etName.getText() != null ? etName.getText().toString().trim() : "";

            if (!q.isEmpty() || !n.isEmpty()) {
                list.add(new Ingredient(q, n));
            }
        }
        return Ingredient.toStorageString(list);
    }

    private void addInstructionStepRow(String stepText) {
        if (getContext() == null) return;
        View rowView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_instruction_input_field, binding.layoutInstructionsContainer, false);

        TextInputEditText etStepInstruction = rowView.findViewById(R.id.et_step_instruction);
        ImageButton btnRemove = rowView.findViewById(R.id.btn_remove_step);

        etStepInstruction.setText(stepText != null ? stepText : "");

        btnRemove.setOnClickListener(v -> {
            binding.layoutInstructionsContainer.removeView(rowView);
            updateStepLabels();
        });

        binding.layoutInstructionsContainer.addView(rowView);
        updateStepLabels();
    }

    private void updateStepLabels() {
        int count = binding.layoutInstructionsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View rowView = binding.layoutInstructionsContainer.getChildAt(i);
            TextView tvStepLabel = rowView.findViewById(R.id.tv_step_label);
            if (tvStepLabel != null) {
                tvStepLabel.setText(getString(R.string.step_label_fmt, i + 1));
            }
        }
    }

    private void populateInstructionFields(String rawInstructions) {
        binding.layoutInstructionsContainer.removeAllViews();
        List<InstructionStep> list = InstructionStep.parseList(rawInstructions);
        if (!list.isEmpty()) {
            for (InstructionStep step : list) {
                addInstructionStepRow(step.getInstructionText());
            }
        } else {
            addInstructionStepRow("");
        }
    }

    private String getInstructionsFromFields() {
        List<InstructionStep> list = new ArrayList<>();
        int childCount = binding.layoutInstructionsContainer.getChildCount();
        int stepNum = 1;
        for (int i = 0; i < childCount; i++) {
            View rowView = binding.layoutInstructionsContainer.getChildAt(i);
            TextInputEditText etStepInstruction = rowView.findViewById(R.id.et_step_instruction);
            String text = etStepInstruction.getText() != null ? etStepInstruction.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                list.add(new InstructionStep(stepNum, text));
                stepNum++;
            }
        }
        return InstructionStep.toStorageString(list);
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
        populateIngredientFields(recipe.getIngredients());
        populateInstructionFields(recipe.getInstructions());
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
