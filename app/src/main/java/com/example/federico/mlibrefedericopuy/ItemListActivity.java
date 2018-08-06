package com.example.federico.mlibrefedericopuy;

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ScrollView;

import com.example.federico.mlibrefedericopuy.adapters.ProductListAdapter;
import com.example.federico.mlibrefedericopuy.model.Description;
import com.example.federico.mlibrefedericopuy.model.Product;
import com.example.federico.mlibrefedericopuy.network.NetworkUtils;
import com.example.federico.mlibrefedericopuy.utils.Constants;
import com.example.federico.mlibrefedericopuy.viewmodel.ProductInfoViewModel;
import com.example.federico.mlibrefedericopuy.viewmodel.SearchResultsViewModel;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity implements ProductListAdapter.ProductClickListener {

    @BindView(R.id.item_list)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    ProductListAdapter adapter;
    SearchResultsViewModel searchResultsViewModel;
    private boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.item_detail_container) != null) {
            isTablet = true;
        }

        searchResultsViewModel = ViewModelProviders.of(this).get(SearchResultsViewModel.class);
        searchResultsViewModel.setAppController(AppController.create(this));
        searchResultsViewModel.init();

        adapter = new ProductListAdapter(this);

        recyclerView.addItemDecoration(new DividerItemDecoration(ItemListActivity.this,
                DividerItemDecoration.VERTICAL));

        searchResultsViewModel.getProductLiveData().observe(this, pagedList -> {
            adapter.submitList(pagedList);
            recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public void onProductClicked(final View view) {

        final Product product = (Product) view.getTag();

        goToNextActivity(product);

    }

    void goToNextActivity(Product product) {

        Gson productGson = new Gson();
        String productJson = productGson.toJson(product);

        if (isTablet) {
            Bundle arguments = new Bundle();
            arguments.putString(Constants.PRODUCT_FRAGMENT_JSON, productJson);

            ItemDetailFragment fragment = new ItemDetailFragment();
            fragment.setArguments(arguments);
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(ItemListActivity.this, ItemDetailActivity.class);
            intent.putExtra(Constants.PRODUCT_INTENT_JSON, productJson);
            startActivity(intent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if (!(s.equals(""))) {

                    searchResultsViewModel.setQuery(s);
                    searchResultsViewModel.invalidateDataSource();

                }
                return false;
            }
        });

        return true;
    }
    //todo restore instance state
}
