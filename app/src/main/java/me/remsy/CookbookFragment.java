package me.remsy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import me.remsy.databinding.FragmentCookbookBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class CookbookFragment extends Fragment {

    private FragmentCookbookBinding binding;
    private RecipeDatabaseHelper dbHelper;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCookbookBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = RecipeDatabaseHelper.getInstance(requireContext());

        binding.fabAddRecipe.setOnClickListener(v ->
                NavHostFragment.findNavController(CookbookFragment.this)
                        .navigate(R.id.action_CookbookFragment_to_AddRecipeFragment)
        );

        renderCategoriesDynamic();
    }

    @Override
    public void onResume() {
        super.onResume();
        renderCategoriesDynamic();
    }

    private void renderCategoriesDynamic() {
        if (dbHelper == null || binding == null) return;

        binding.gridCategories.removeAllViews();
        List<String> categories = dbHelper.getAllCategories();

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (32 * getResources().getDisplayMetrics().density); // 16dp left + 16dp right
        int columnWidth = (screenWidth - padding) / 2;
        int renderedCount = 0;

        for (String catName : categories) {
            int count = dbHelper.getRecipeCountByCategory(catName);
            boolean isFaves = "Faves".equalsIgnoreCase(catName);

            // Hide any category that currently has 0 recipes (except "Faves" which is always visible)
            if (count == 0 && !isFaves) {
                continue;
            }

            renderedCount++;
            View tileView = getLayoutInflater().inflate(R.layout.item_category_tile, binding.gridCategories, false);

            TextView tvName = tileView.findViewById(R.id.tv_category_name);
            TextView tvCount = tileView.findViewById(R.id.tv_recipe_count);
            ImageView iv1 = tileView.findViewById(R.id.iv_preview_1);
            ImageView iv2 = tileView.findViewById(R.id.iv_preview_2);

            tvName.setText(catName);
            tvCount.setText(getString(R.string.sample_recipe_count_fmt, count));

            // Bind image previews
            List<Recipe> catRecipes = dbHelper.getRecipesByCategory(catName);
            if (!catRecipes.isEmpty()) {
                bindPreviewImage(iv1, catRecipes.get(0).getImageUri());
                if (catRecipes.size() > 1) {
                    bindPreviewImage(iv2, catRecipes.get(1).getImageUri());
                } else {
                    bindPreviewImage(iv2, catRecipes.get(0).getImageUri());
                }
            } else {
                iv1.setImageResource(R.drawable.ic_favorite);
                iv2.setImageResource(R.drawable.recipe_sample_image);
            }

            // Click listener
            tileView.setOnClickListener(v -> openCategoryRecipes(catName));

            // Long press listener to delete collection
            tileView.setOnLongClickListener(v -> showDeleteCollectionDialog(catName));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = columnWidth - (int) (12 * getResources().getDisplayMetrics().density);
            params.setMargins(0, 0, (int) (6 * getResources().getDisplayMetrics().density), (int) (12 * getResources().getDisplayMetrics().density));
            tileView.setLayoutParams(params);

            binding.gridCategories.addView(tileView);
        }

        if (renderedCount == 0) {
            binding.gridCategories.setVisibility(View.GONE);
            binding.tvEmptyCookbook.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyCookbook.setVisibility(View.GONE);
            binding.gridCategories.setVisibility(View.VISIBLE);
        }
    }

    private void bindPreviewImage(ImageView imageView, String imageUri) {
        RecipeImageBinder.bind(imageView, imageUri);
    }

    private boolean showDeleteCollectionDialog(String collectionName) {
        if ("Faves".equalsIgnoreCase(collectionName)) {
            Toast.makeText(requireContext(), R.string.msg_cannot_delete_faves, Toast.LENGTH_SHORT).show();
            return true;
        }

        String title = getString(R.string.dialog_delete_collection_title, collectionName);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(R.string.dialog_delete_collection_msg)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    dbHelper.moveCategoryRecipesToUncategorized(collectionName);
                    renderCategoriesDynamic();
                    Toast.makeText(requireContext(), R.string.msg_collection_deleted, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
        return true;
    }

    private void openCategoryRecipes(String categoryName) {
        Bundle args = new Bundle();
        args.putString("categoryName", categoryName);
        NavHostFragment.findNavController(CookbookFragment.this)
                .navigate(R.id.action_CookbookFragment_to_CategoryRecipesFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}