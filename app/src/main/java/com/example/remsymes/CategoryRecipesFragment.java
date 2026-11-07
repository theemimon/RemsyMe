package com.example.remsymes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.remsymes.databinding.FragmentCategoryRecipesBinding;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryRecipesFragment extends Fragment {

    private static final String OPTION_CREATE_NEW = "+ Create New Category";

    private FragmentCategoryRecipesBinding binding;
    private RecipeDatabaseHelper dbHelper;
    private String currentCategoryName = "Pasta & Italian";

    private final Set<Long> selectedRecipeIds = new HashSet<>();
    private boolean isBulkSelectionMode = false;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCategoryRecipesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = RecipeDatabaseHelper.getInstance(requireContext());

        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(CategoryRecipesFragment.this).navigateUp()
        );

        if (getArguments() != null) {
            String argName = getArguments().getString("categoryName");
            if (argName != null && !argName.isEmpty()) {
                currentCategoryName = argName;
                binding.toolbar.setTitle(currentCategoryName);
            }
        }

        binding.btnBulkMove.setOnClickListener(v -> showMoveRecipesDialog());
        binding.btnBulkDelete.setOnClickListener(v -> showDeleteRecipesDialog());

        loadAndRenderRecipes();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAndRenderRecipes();
    }

    private void loadAndRenderRecipes() {
        if (dbHelper == null) return;
        List<Recipe> recipes = dbHelper.getRecipesByCategory(currentCategoryName);
        renderRecipesDynamic(recipes);
        updateBulkSelectionUI();
    }

    private void renderRecipesDynamic(List<Recipe> recipes) {
        if (binding == null) return;
        binding.gridRecipes.removeAllViews();

        if (recipes == null || recipes.isEmpty()) {
            binding.gridRecipes.setVisibility(View.GONE);
            binding.tvEmptyCategory.setVisibility(View.VISIBLE);
            return;
        }

        binding.tvEmptyCategory.setVisibility(View.GONE);
        binding.gridRecipes.setVisibility(View.VISIBLE);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (32 * getResources().getDisplayMetrics().density); // 16dp left + 16dp right
        int columnWidth = (screenWidth - padding) / 2;

        for (Recipe recipe : recipes) {
            View cardView = getLayoutInflater().inflate(R.layout.item_recipe_card, binding.gridRecipes, false);

            ImageView ivImage = cardView.findViewById(R.id.iv_recipe_image);
            TextView tvTitle = cardView.findViewById(R.id.tv_recipe_title);
            TextView tvRating = cardView.findViewById(R.id.tv_recipe_rating);
            View overlay = cardView.findViewById(R.id.view_selection_overlay);

            tvTitle.setText(recipe.getName());

            String ratingStr = recipe.getRating() + "";
            if (recipe.getPrepTime() != null && !recipe.getPrepTime().isEmpty()) {
                ratingStr += " • " + recipe.getPrepTime();
            }
            tvRating.setText(ratingStr);

            RecipeImageBinder.bind(ivImage, recipe.getImageUri());

            // Visual indication for selection (greying out)
            boolean isSelected = selectedRecipeIds.contains(recipe.getId());
            if (isSelected) {
                overlay.setVisibility(View.VISIBLE);
                cardView.setAlpha(0.55f);
            } else {
                overlay.setVisibility(View.GONE);
                cardView.setAlpha(1.0f);
            }

            // Long click enters bulk selection mode
            cardView.setOnLongClickListener(v -> {
                if (!isBulkSelectionMode) {
                    isBulkSelectionMode = true;
                    selectedRecipeIds.clear();
                    selectedRecipeIds.add(recipe.getId());
                    updateBulkSelectionUI();
                    loadAndRenderRecipes();
                }
                return true;
            });

            // Click listener handles selection toggle if in bulk mode, or opens detail
            cardView.setOnClickListener(v -> {
                if (isBulkSelectionMode) {
                    if (selectedRecipeIds.contains(recipe.getId())) {
                        selectedRecipeIds.remove(recipe.getId());
                    } else {
                        selectedRecipeIds.add(recipe.getId());
                    }

                    if (selectedRecipeIds.isEmpty()) {
                        isBulkSelectionMode = false;
                    }

                    updateBulkSelectionUI();
                    loadAndRenderRecipes();
                } else {
                    openRecipeDetail(recipe.getId());
                }
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = columnWidth - (int) (12 * getResources().getDisplayMetrics().density);
            params.setMargins(0, 0, (int) (6 * getResources().getDisplayMetrics().density), (int) (12 * getResources().getDisplayMetrics().density));
            cardView.setLayoutParams(params);

            binding.gridRecipes.addView(cardView);
        }
    }

    private void updateBulkSelectionUI() {
        if (binding == null) return;
        if (isBulkSelectionMode && !selectedRecipeIds.isEmpty()) {
            binding.cardBulkActionBar.setVisibility(View.VISIBLE);
            binding.tvSelectionCount.setText(getString(R.string.selection_count_fmt, selectedRecipeIds.size()));
        } else {
            binding.cardBulkActionBar.setVisibility(View.GONE);
            isBulkSelectionMode = false;
        }
    }

    private void showMoveRecipesDialog() {
        if (selectedRecipeIds.isEmpty()) return;

        List<String> categories = dbHelper.getAllCategories();
        if (!categories.contains(OPTION_CREATE_NEW)) {
            categories.add(OPTION_CREATE_NEW);
        }

        View customView = getLayoutInflater().inflate(R.layout.dialog_move_recipes, null);
        MaterialAutoCompleteTextView actvCategory = customView.findViewById(R.id.actv_category);
        TextInputLayout tilNewCat = customView.findViewById(R.id.til_new_category);
        TextInputEditText etNewCat = customView.findViewById(R.id.et_new_category);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        actvCategory.setAdapter(adapter);

        actvCategory.setOnItemClickListener((parent, v, position, id) -> {
            String selected = adapter.getItem(position);
            if (OPTION_CREATE_NEW.equals(selected)) {
                tilNewCat.setVisibility(View.VISIBLE);
            } else {
                tilNewCat.setVisibility(View.GONE);
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_move_title)
                .setView(customView)
                .setPositiveButton(R.string.btn_confirm_move, (dialog, which) -> {
                    String selected = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
                    String targetCategory = selected;
                    if (OPTION_CREATE_NEW.equals(selected) || selected.isEmpty()) {
                        String newCat = etNewCat.getText() != null ? etNewCat.getText().toString().trim() : "";
                        if (!newCat.isEmpty()) {
                            targetCategory = newCat;
                        } else {
                            targetCategory = "Uncategorized";
                        }
                    }

                    int count = 0;
                    for (Long recipeId : new ArrayList<>(selectedRecipeIds)) {
                        Recipe r = dbHelper.getRecipeById(recipeId);
                        if (r != null) {
                            r.setCategory(targetCategory);
                            dbHelper.updateRecipe(r);
                            count++;
                        }
                    }

                    Toast.makeText(requireContext(), getString(R.string.msg_recipes_moved, count), Toast.LENGTH_SHORT).show();
                    selectedRecipeIds.clear();
                    isBulkSelectionMode = false;
                    loadAndRenderRecipes();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void showDeleteRecipesDialog() {
        if (selectedRecipeIds.isEmpty()) return;

        int count = selectedRecipeIds.size();
        String title = getString(R.string.dialog_bulk_delete_title, count);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(R.string.dialog_bulk_delete_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    for (Long recipeId : new ArrayList<>(selectedRecipeIds)) {
                        dbHelper.deleteRecipe(recipeId);
                    }
                    Toast.makeText(requireContext(), getString(R.string.msg_recipes_deleted, count), Toast.LENGTH_SHORT).show();
                    selectedRecipeIds.clear();
                    isBulkSelectionMode = false;
                    loadAndRenderRecipes();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void openRecipeDetail(long recipeId) {
        Bundle args = new Bundle();
        args.putLong("recipeId", recipeId);
        NavHostFragment.findNavController(CategoryRecipesFragment.this)
                .navigate(R.id.action_CategoryRecipesFragment_to_RecipeDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}