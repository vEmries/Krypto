package com.krypto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RecyclerView recyclerView;
    private CurrencyAdapter adapter;
    private List<Currency> currencyList;
    private float sD;

    private LinearLayout mLWelcome;
    private int animDuration=200;

    Button btn_Add;
    RadioButton radio_btc, radio_eth;
    SharedPreferences sharedPref = null;
    SharedPreferences.Editor editor = null;
    Spinner base_spinner;
    Resources res;

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        System.exit(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        // Dodatkowy button wyjścia
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        currencyList = new ArrayList<>();
        adapter = new CurrencyAdapter(this, currencyList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        btn_Add = (Button)findViewById(R.id.add_button);
        radio_btc = (RadioButton)findViewById(R.id.radio_button_btc);
        radio_eth = (RadioButton)findViewById(R.id.radio_button_eth);

        sharedPref = getSharedPreferences(getString(R.string.shared_pref_crypto), Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        loadArray();

        // Dropdown z walutami
        base_spinner = (Spinner) findViewById(R.id.quote_currency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        base_spinner.setAdapter(adapter);

        // Poprawiony rozmiar okna z hintami
        res = getResources();
        sD = res.getDisplayMetrics().density;
        sD = res.getDisplayMetrics().density;

        // Check dla okna z hintami
        if(sharedPref.getAll().isEmpty() || sharedPref.getInt("List_size", 0)==0) {
            showHint();
        }

        // Listener dla monitorowania SharedPreferences
        sharedPref.registerOnSharedPreferenceChangeListener(this);


        // Dodawanie nowych kart z przelicznikami
        btn_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String name = base_spinner.getSelectedItem().toString().split(",")[0].replace(" ", "");
               String symbol = base_spinner.getSelectedItem().toString().split(",")[1].replace(" ", "");

                if(radio_eth.isChecked()) {
                    if(!checkIfExists(R.drawable.eth_logo, name)) {
                        addCard(radio_eth.getText().toString(), name, symbol);
                    } else {
                        Toast.makeText(getApplicationContext(), "Ta karta już istnieje!", Toast.LENGTH_LONG).show();
                    }
                } else
                    if(radio_btc.isChecked()){
                    if(!checkIfExists(R.drawable.btc_logo, name)) {
                        addCard(radio_btc.getText().toString(), name, symbol);
                    } else {
                            Toast.makeText(getApplicationContext(), "Ta karta już istnieje!", Toast.LENGTH_LONG).show();
                        }
                }
            }
        });

        // Tło -> poprawić bo się rozjeżdża
        ImageView img_cover = (ImageView) findViewById(R.id.backdrop);
        try {
            Glide.with(this).load(R.drawable.main_bg).into(img_cover);
        } catch (Exception e) {}
    }

    // Załadowanie topbara
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Funkcjonalności dla topbara
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about_id) {
           startActivity(new Intent(MainActivity.this, AboutActivity.class));
            finish();
            return true;
        }

        if (id == android.R.id.home) {
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Wyświetlanie hintów
    public void showHint(){
        ViewStub v = (ViewStub) findViewById(R.id.VSFirstTimeProcesses);
        if (v != null) {
            mLWelcome = (LinearLayout) v.inflate();
            ((TextView) mLWelcome.findViewById(R.id.TVFirstTimeText)).setText(getString(R.string.w_quick_tips));

            int bottomMargin = 0;

            if (Build.VERSION.SDK_INT >= 19)
                ((FrameLayout.LayoutParams) mLWelcome.getLayoutParams()).setMargins(0, 0, 0, (int)(35*sD) + bottomMargin);

            mLWelcome.findViewById(R.id.BHint).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLWelcome.animate().setDuration(animDuration).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((ViewManager) mLWelcome.getParent()).removeView(mLWelcome);

                        }
                    }).setStartDelay(0).alpha(0).translationYBy(-15*sD);
                }
            });

            int animDur = animDuration;
            int delayDur = 600;

            mLWelcome.animate().setStartDelay(delayDur).setDuration(animDur).alpha(1).translationYBy(15*sD);
        }
    }

    // Ukrywanie toolbara przy przewijaniu
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    // Dodawanie nowych kart z kursami
    private void addCard(String base_currency, String quote_currency_name, String quote_currency_symbol) {
        int[] base_type = new int[]{
                R.drawable.btc_logo,
                R.drawable.eth_logo};
            if (base_currency.contains("BTC")) {
                Currency a = new Currency(quote_currency_name, quote_currency_symbol, base_type[0]);
                currencyList.add(a);
                saveArray();
            } else if (base_currency.contains("ETH")) {
                Currency a = new Currency(quote_currency_name, quote_currency_symbol, base_type[1]);
                currencyList.add(a);
                saveArray();
            }

        adapter.notifyDataSetChanged();
    }

    // Zapis do SharedPreferences
    public void saveArray()
    {
        editor.putInt("List_size", currencyList.size());
        for(int i = 0; i< currencyList.size(); i++) {
                editor.remove("List_" + i);
                editor.putString("List_" + i, currencyList.get(i).getName() + "#" +currencyList.get(i).getSymbol() + "#" + currencyList.get(i).getThumbnail());
        }

        editor.commit();
    }

    // Odczyt z SharedPreferences
    public void loadArray()
    {
        currencyList.clear();
        int size = sharedPref.getInt("List_size", 0);

        for(int i=0;i<size;i++) {
            if(!sharedPref.getString("List_"+i, null).equals(null)) {
                Currency a = new Currency(sharedPref.getString("List_" + i, null).split("#")[0],
                        sharedPref.getString("List_" + i, null).split("#")[1],
                        Integer.parseInt(sharedPref.getString("List_" + i, null).split("#")[2]));
                currencyList.add(a);
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Monitorowanie zmian w SharedPreferences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadArray();
        saveArray();
        adapter.notifyDataSetChanged();
    }

    // Check dla zapisu w SharedPreferences
    public boolean checkIfExists(int thumbnail, String quote_currency)
    {
        boolean checked = false;
        int size = sharedPref.getInt("List_size", 0);

        for(int i=0;i<size;i++) {
          if(sharedPref.getString("List_"+i, null).split("#")[2].contains(String.valueOf(thumbnail))
                  && sharedPref.getString("List_"+i, null).split("#")[0].contains(quote_currency)){
           checked = true;
          }
        }

        return checked;
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }
                outRect.bottom = spacing;
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}